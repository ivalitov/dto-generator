package laoruga.factory;

import laoruga.custom.ArrearsBusinessRule;
import laoruga.dto.Arrears;
import laoruga.dto.DtoVer1;
import laoruga.markup.*;
import laoruga.markup.rules.*;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
public class DtoBuilder {


    public static void main(String[] args) throws IllegalAccessException {
        GenerationFactory.getInstance().registerCustomGenerator(ArrearsBusinessRule.class, ArrearsGenerator.class);
        new DtoBuilder().generateDto(DtoVer1.class);
    }

    private final Map<Field, Exception> errors = new HashMap<>();
    private final Map<Field, IGenerator<?>> fieldIGeneratorMap = new LinkedHashMap<>();
    private Object dtoInstance;

    void generateDto(Class<?> dtoClass) {
        createDtoInstance(dtoClass);
        prepareGenerators();
        applyGenerators();
        System.out.println(dtoInstance);
    }

    void createDtoInstance(Class<?> dtoClass) {
        try {
            dtoInstance = dtoClass.newInstance();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    void applyGenerators() {
        int attempts = 0;
        int maxAttempts = 3;
        while (!fieldIGeneratorMap.isEmpty() && attempts < maxAttempts) {
            attempts++;
            log.debug("Attempt {} to generate field values", attempts);
            Iterator<Map.Entry<Field, IGenerator<?>>> iterator = fieldIGeneratorMap.entrySet().iterator();
            while (iterator.hasNext()) {

                Map.Entry<Field, IGenerator<?>> nextFieldAndGenerator = iterator.next();
                Field field = nextFieldAndGenerator.getKey();
                IGenerator<?> generator = nextFieldAndGenerator.getValue();
                try {
                    if (generator instanceof IObjectDependentCustomGenerator) {
                        if (!((IObjectDependentCustomGenerator<?, ?>) generator).isObjectReady()) {
                            if (attempts < maxAttempts - 1) {
                                log.debug("Object is not ready to generate dependent field value");
                                continue;
                            } else {
                                throw new RuntimeException("Generator " + generator.getClass() +
                                        " wasn't prepared in " + attempts + " attempts");
                            }
                        }
                    }
                    boolean isFieldAccessible = field.isAccessible();
                    try {
                        if (!isFieldAccessible) field.setAccessible(true);
                        field.set(dtoInstance, generator.generate());
                    } catch (IllegalAccessException e) {
                        log.error("Access error while generation value for a field: " + field, e);
                        errors.put(field, e);
                    } catch (Exception e) {
                        log.error("Error while generation value for the field: " + field, e);
                        errors.put(field, e);
                    } finally {
                        iterator.remove();
                        if (!isFieldAccessible) field.setAccessible(false);
                    }
                } catch (Exception e) {
                    log.error("Error while generation value for the field: " + field, e);
                    errors.put(field, e);
                    iterator.remove();
                }

            }
        }
        if (!fieldIGeneratorMap.isEmpty() || !errors.isEmpty()) {
            if (!fieldIGeneratorMap.isEmpty()) {
                log.error("Unexpected state. There {} unused generator(s) left. Fileds vs Generators: " +
                        fieldIGeneratorMap, fieldIGeneratorMap.size());
            }
            if (!errors.isEmpty()) {
                log.error("{} error(s) while generators execution. Fileds vs Generators: " + errors, errors.size());
            }
            throw new RuntimeException("Error while generators execution (see log above)");
        }

    }

    void prepareGenerators() {
        for (Field field : dtoInstance.getClass().getDeclaredFields()) {
            IGenerator<?> generator = prepareGenerator(field);
            if (generator != null) {
                fieldIGeneratorMap.put(field, generator);
            }
        }
        if (!errors.isEmpty()) {
            log.error("{} errors while generators preparation. Fileds vs Generators: " + errors, errors.size());
            throw new RuntimeException("Error while generators preparation (see log above)");
        }
    }

    IGenerator<?> prepareGenerator(Field field) {
        IGenerator<?> generator = null;
        try {
            generator = selectGenerator(field);
            if (generator instanceof IObjectDependentCustomGenerator) {
                try {
                    ((IObjectDependentCustomGenerator) generator).setDependentObject(dtoInstance);
                } catch (Exception e) {
                    throw e;
                }
            }
        } catch (Exception e) {
            errors.put(field, e);
        }
        return generator;
    }

    IGenerator<?> selectGenerator(Field field) {

        if (field.getType() == Double.class) {
            DecimalFieldRules decimalBounds = field.getAnnotation(DecimalFieldRules.class);
            if (decimalBounds != null) {
                return new BasicGenerators.DecimalFieldGenerator(
                        decimalBounds.maxValue(),
                        decimalBounds.minValue(),
                        decimalBounds.precision()
                );
            }
        }

        if (field.getType() == String.class) {
            StringFieldRules stringBounds = field.getAnnotation(StringFieldRules.class);
            if (stringBounds != null) {
                return new BasicGenerators.StringFieldGenerator(
                        stringBounds.maxSymbols(),
                        stringBounds.minSymbols(),
                        stringBounds.charset()
                );
            }
        }

        if (field.getType() == Long.class) {
            LongFieldRules stringBounds = field.getAnnotation(LongFieldRules.class);
            if (stringBounds != null) {
                return new BasicGenerators.IntegerFieldGenerator(
                        stringBounds.maxValue(),
                        stringBounds.minValue()
                );
            }
        }

        if (field.getType().isEnum()) {
            EnumFieldRules enumBounds = field.getAnnotation(EnumFieldRules.class);
            if (enumBounds != null) {
                return new BasicGenerators.EnumFieldGenerator(
                        enumBounds.possibleValues(),
                        enumBounds.className()
                );
            }
        }

        if (field.getType() == LocalDateTime.class) {
            LocalDateTimeFieldRules enumBounds = field.getAnnotation(LocalDateTimeFieldRules.class);
            if (enumBounds != null) {
                return new BasicGenerators.LocalDateTimeFieldGenerator(
                        enumBounds.leftShiftDays(),
                        enumBounds.rightShiftDays()
                );
            }
        }

        /*
         * Custom generator 1st ver
         */

        List<Annotation> customGenerators = Arrays.stream(field.getAnnotations())
                .filter(a -> a.annotationType().getAnnotation(CustomRules.class) != null)
                .collect(Collectors.toList());

        if (!customGenerators.isEmpty()) {
            GenerationFactory genFactory = GenerationFactory.getInstance();
            for (Annotation generationRules : customGenerators) {
                if (genFactory.isCustomGeneratorExists(generationRules.annotationType())) {
                    try {
                        Class<? extends IRulesDependentCustomGenerator<?, ? extends Annotation>> customGenerator = genFactory.getCustomGenerator(generationRules.annotationType());
                        IRulesDependentCustomGenerator customGeneratorInstance = customGenerator.newInstance();
                        customGeneratorInstance.prepareGenerator(generationRules);
                        return customGeneratorInstance;
                    } catch (InstantiationException e) {
                        e.printStackTrace();
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    }
                } else {
                    throw new RuntimeException();
                }
            }
        }

        /*
         * custom generator 2nd var
         */

        CustomGenerator customGeneratorRules = field.getAnnotation(CustomGenerator.class);

        if (customGeneratorRules != null) {
            String generatorClassName = customGeneratorRules.className();
            try {
                Class<?> aClass = Class.forName(generatorClassName);
                Object generatorInstance = aClass.newInstance();
                if (generatorInstance instanceof ICustomGenerator) {
                    ((ICustomGenerator<?>) generatorInstance).setArgs(customGeneratorRules.args());
                }
                if (generatorInstance instanceof IGenerator) {
                    return (IGenerator<?>) generatorInstance;
                } else {
                    throw new RuntimeException();
                }
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {

            } catch (InstantiationException e) {
                e.printStackTrace();
            }
        }

        return new BasicGenerators.NullGenerator();
    }

    /*
     * Custom
     */

    @NoArgsConstructor
    static class ArrearsGenerator implements IRulesDependentCustomGenerator<Arrears, ArrearsBusinessRule> {

        int arrearsCount;

        @Override
        public void prepareGenerator(ArrearsBusinessRule rules) {
            arrearsCount = rules.arrearsCount();
        }

        @Override
        public Arrears generate() {
            Arrears arrears = new Arrears();
            for (int i = 0; i < arrearsCount; i++) {
                arrears.addArrear(i);
            }
            return arrears;
        }
    }

}
