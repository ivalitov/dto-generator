package laoruga.dtogenerator.api;

import laoruga.dtogenerator.api.exceptions.DtoGeneratorException;
import laoruga.dtogenerator.api.generators.BasicTypeGenerators;
import laoruga.dtogenerator.api.markup.generators.ICustomGenerator;
import laoruga.dtogenerator.api.markup.generators.ICustomGeneratorArgs;
import laoruga.dtogenerator.api.markup.generators.ICustomGeneratorDtoDependent;
import laoruga.dtogenerator.api.markup.generators.IGenerator;
import laoruga.dtogenerator.api.markup.remarks.IRuleRemark;
import laoruga.dtogenerator.api.markup.rules.*;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.*;

import static laoruga.dtogenerator.api.markup.remarks.RuleRemark.MIN_VALUE;
import static laoruga.dtogenerator.api.markup.remarks.RuleRemark.NULL_VALUE;

@Slf4j
public class DtoGenerator {

    private Object dtoInstance;

    private final Map<Field, Exception> errors = new HashMap<>();
    private final Map<Field, IGenerator<?>> fieldIGeneratorMap = new LinkedHashMap<>();

    private final IRuleRemark ruleRemark;
    private final Map<String, IRuleRemark> fieldRuleRemarkMap;

    protected DtoGenerator(IRuleRemark ruleRemark, Map<String, IRuleRemark> fieldRuleRemarkMap) {
        this.ruleRemark = ruleRemark;
        this.fieldRuleRemarkMap = fieldRuleRemarkMap;
    }

    public static DtoGeneratorBuilder builder() {
        return new DtoGeneratorBuilder();
    }

    public <T> T generateDto(Class<T> dtoClass) {
        createDtoInstance(dtoClass);
        prepareGenerators();
        applyGenerators();
        return (T) dtoInstance;
    }

    public <T> T generateDto(T dtoInstance) {
        this.dtoInstance = dtoInstance;
        prepareGenerators();
        applyGenerators();
        return (T) dtoInstance;
    }

    void createDtoInstance(Class<?> dtoClass) {
        try {
            if (dtoClass.getConstructors().length == 0) {
                throw new DtoGeneratorException("Failed to instantiate DTO class: '" + dtoClass + "'. " +
                        "Class don't have public constructors. It must have public no-args constructor.");
            }
            Optional<Constructor<?>> maybeNoArgsConstructor = Arrays.stream(dtoClass.getConstructors())
                    .filter(constructor -> constructor.getParameterCount() == 0)
                    .findAny();
            if (!maybeNoArgsConstructor.isPresent()) {
                throw new DtoGeneratorException("Failed to instantiate DTO class: '" + dtoClass + "'. " +
                        "Class must have public no-args constructor.");
            }
            Constructor<?> constructor = maybeNoArgsConstructor.get();
            boolean isAccessible = constructor.isAccessible();
            constructor.setAccessible(true);
            dtoInstance = constructor.newInstance();
            constructor.setAccessible(isAccessible);
        } catch (InstantiationException ie) {
            throw new DtoGeneratorException("Failed to instantiate DTO class: '" + dtoClass + "'. " +
                    "Maybe no-args constructor was not found.", ie);
        } catch (Exception e) {
            throw new DtoGeneratorException("Failed to instantiate DTO class: '" + dtoClass + "'.", e);
        }
    }

    void applyGenerators() {
        int attempts = 0;
        int maxAttempts = 100;
        while (!fieldIGeneratorMap.isEmpty() && attempts < maxAttempts) {
            attempts++;
            log.debug("Attempt {} to generate field values", attempts);
            Iterator<Map.Entry<Field, IGenerator<?>>> iterator = fieldIGeneratorMap.entrySet().iterator();
            while (iterator.hasNext()) {

                Map.Entry<Field, IGenerator<?>> nextFieldAndGenerator = iterator.next();
                Field field = nextFieldAndGenerator.getKey();
                IGenerator<?> generator = nextFieldAndGenerator.getValue();
                try {
                    if (generator instanceof ICustomGeneratorDtoDependent) {
                        if (!((ICustomGeneratorDtoDependent<?, ?>) generator).isDtoReady()) {
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
            log.error("{} error(s) while generators preparation. Fileds vs Generators: " + errors, errors.size());
            throw new RuntimeException("Error while generators preparation (see log above)");
        }
        if (fieldIGeneratorMap.isEmpty()) {
            log.debug("No generators have been found");
        } else {
            log.debug(fieldIGeneratorMap.size() + " generators was created for fields: " + fieldIGeneratorMap.keySet());
        }
    }

    IGenerator<?> prepareGenerator(Field field) {
        IGenerator<?> generator = null;
        try {
            generator = selectGenerator(field);
        } catch (Exception e) {
            errors.put(field, e);
        }
        return generator;
    }

    private IRuleRemark getBasicRuleRemark(Field field) {
        if (fieldRuleRemarkMap.containsKey(field.getName())) {
            return fieldRuleRemarkMap.get(field.getName());
        }
        return ruleRemark;
    }

    private IGenerator<?> selectGenerator(Field field) {

        if (field.getType() == Double.class || field.getType() == Double.TYPE) {
            DoubleRules doubleBounds = field.getAnnotation(DoubleRules.class);
            if (doubleBounds != null) {
                IRuleRemark basicRuleRemark = getBasicRuleRemark(field);
                double minValue = doubleBounds.minValue();
                if (basicRuleRemark == NULL_VALUE && field.getType() == Double.TYPE) {
                    log.debug("Doubel primitive field '" + field.getName() + "' can't be null, it will be assigned " +
                            " to DoubleRules.DEFAULT_MIN");
                    basicRuleRemark = MIN_VALUE;
                    minValue = DoubleRules.DEFAULT_MIN;
                }
                return new BasicTypeGenerators.DoubleGenerator(
                        doubleBounds.maxValue(),
                        minValue,
                        doubleBounds.precision(),
                        basicRuleRemark
                );
            }
        }

        if (field.getType() == String.class) {
            StringRules stringBounds = field.getAnnotation(StringRules.class);
            if (stringBounds != null) {
                return new BasicTypeGenerators.StringGenerator(
                        stringBounds.maxSymbols(),
                        stringBounds.minSymbols(),
                        stringBounds.charset(),
                        stringBounds.chars(),
                        getBasicRuleRemark(field)
                );
            }
        }

        if (field.getType() == Integer.class || field.getType() == Integer.TYPE) {
            IntegerRules integerRules = field.getAnnotation(IntegerRules.class);
            if (integerRules != null) {
                IRuleRemark basicRuleRemark = getBasicRuleRemark(field);
                int minValue = integerRules.minValue();
                if (basicRuleRemark == NULL_VALUE && field.getType() == Integer.TYPE) {
                    log.debug("Integer primitive field '" + field.getName() + "' can't be null, it will be assigned " +
                            " to IntegerRules.DEFAULT_MIN");
                    basicRuleRemark = MIN_VALUE;
                    minValue = IntegerRules.DEFAULT_MIN;
                }
                return new BasicTypeGenerators.IntegerGenerator(
                        integerRules.maxValue(),
                        minValue,
                        basicRuleRemark
                );
            }
        }

        if (field.getType() == Long.class || field.getType() == Long.TYPE) {
            LongRules longRules = field.getAnnotation(LongRules.class);
            if (longRules != null) {
                IRuleRemark basicRuleRemark = getBasicRuleRemark(field);
                long minValue = longRules.minValue();
                if (basicRuleRemark == NULL_VALUE && field.getType() == Long.TYPE) {
                    log.debug("Long primitive field '" + field.getName() + "' can't be null, it will be assigned " +
                            " to LongRules.DEFAULT_MIN");
                    basicRuleRemark = MIN_VALUE;
                    minValue = LongRules.DEFAULT_MIN;
                }
                return new BasicTypeGenerators.LongGenerator(
                        longRules.maxValue(),
                        minValue,
                        basicRuleRemark
                );
            }
        }

        if (field.getType().isEnum()) {
            EnumRules enumBounds = field.getAnnotation(EnumRules.class);
            if (enumBounds != null) {
                return new BasicTypeGenerators.EnumGenerator(
                        enumBounds.possibleEnumNames(),
                        enumBounds.enumClass(),
                        getBasicRuleRemark(field)
                );
            }
        }

        if (field.getType() == LocalDateTime.class) {
            LocalDateTimeRules enumBounds = field.getAnnotation(LocalDateTimeRules.class);
            if (enumBounds != null) {
                return new BasicTypeGenerators.LocalDateTimeGenerator(
                        enumBounds.leftShiftDays(),
                        enumBounds.rightShiftDays(),
                        getBasicRuleRemark(field)
                );
            }
        }

        /*
         * Custom Generator
         */

        CustomGenerator customGeneratorRules = field.getAnnotation(CustomGenerator.class);

        if (customGeneratorRules != null) {
            Class<?> generatorClass = null;
            try {
                generatorClass = customGeneratorRules.generatorClass();
                Object generatorInstance = generatorClass.newInstance();
                if (generatorInstance instanceof ICustomGeneratorArgs) {
                    log.debug("Args {} have been obtained from Annotation: {}",
                            Arrays.asList(customGeneratorRules.args()), customGeneratorRules);
                    ((ICustomGeneratorArgs<?>) generatorInstance).setArgs(customGeneratorRules.args());
                }
                if (generatorInstance instanceof ICustomGeneratorDtoDependent) {
                    try {
                        ((ICustomGeneratorDtoDependent) generatorInstance).setDto(dtoInstance);
                    } catch (Exception e) {
                        throw new DtoGeneratorException("Exception was thrown while trying to set DTO into " +
                                "DTO dependent custom generator: " + generatorInstance.getClass(), e);
                    }
                }
                if (generatorInstance instanceof ICustomGenerator) {
                    return (ICustomGenerator<?>) generatorInstance;
                } else {
                    throw new DtoGeneratorException("Failed to prepare custom generator. " +
                            "Custom generator must implements: '" + ICustomGenerator.class + "' or it's heirs.");
                }
            } catch (Exception e) {
                throw new DtoGeneratorException("Error while preparing custom generator from class: " + generatorClass, e);
            }
        }
        log.debug("Field " + field + " hasn't been mapped with any generator.");
        return null;
    }

}
