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

    private final List<Exception> errors = new ArrayList<>();
    private final Map<Field, IGenerator<?>> fieldIGeneratorMap = new LinkedHashMap<>();


    void generateDto(Class<?> dtoClass) {
        Object dtoInstance;

        try {
            dtoInstance = dtoClass.newInstance();
        } catch (Exception e) {
         throw new RuntimeException(e);
        }

        prepareGenerators(dtoInstance);
        applyGenerators(dtoInstance);

        System.out.println(dtoInstance);
    }




    void applyGenerators(Object dtoInstance) {
        int attempts = 0;
        int maxAttempts = 500;
        while (!fieldIGeneratorMap.isEmpty() && attempts < maxAttempts) {
            attempts++;
            log.debug("Attempt {} to generate field values", attempts);
            for (Map.Entry<Field, IGenerator<?>> fieldAndGenerator : fieldIGeneratorMap.entrySet()) {
                Field field = fieldAndGenerator.getKey();
                IGenerator<?> generator = fieldAndGenerator.getValue();
                if (generator instanceof IDtoDependentCustomGenerator) {
                    if (!((IDtoDependentCustomGenerator<?, ?>) generator).isObjectReady()) {
                        log.debug("Object is not ready to generate dependent field value");
                        continue;
                    }
                }
                try {
                    field.setAccessible(true);
                    field.set(dtoInstance, generator.generate());
                } catch (IllegalAccessException e) {
                    log.error("Error while generation value for a field: " + field, e);
                    fieldIGeneratorMap.remove(field);
                } finally {
                    field.setAccessible(false);
                }
            }
        }
    }

    void prepareGenerators(Object dtoInstance){
        for (Field field : dtoInstance.getClass().getDeclaredFields()) {
            IGenerator<?> generator = prepareGenerator(field, dtoInstance);
            if (generator != null) {
                fieldIGeneratorMap.put(field, generator);
            }
        }
    }

    IGenerator<?> prepareGenerator(Field field, Object dtoInstance) {
        IGenerator<?> generator = null;
        try {
            generator = selectGenerator(field);
            if (generator instanceof IDtoDependentCustomGenerator) {
                try {
                    ((IDtoDependentCustomGenerator) generator).setDependentObject(dtoInstance);
                } catch (Exception e) {
                    throw e;
                }
            }
        } catch (Exception e) {
            errors.add(e);
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
