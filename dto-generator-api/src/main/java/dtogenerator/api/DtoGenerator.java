package dtogenerator.api;

import dtogenerator.api.generators.BasicTypeGenerators;
import dtogenerator.api.markup.generators.ICustomGenerator;
import dtogenerator.api.markup.generators.IDtoDependentCustomGenerator;
import dtogenerator.api.markup.generators.IGenerator;
import dtogenerator.api.markup.remarks.IRuleRemark;
import dtogenerator.api.markup.remarks.RuleRemark;
import dtogenerator.api.markup.rules.*;
import lombok.Builder;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.*;

@Slf4j
public class DtoGenerator {

    private Object dtoInstance;

    private final Map<Field, Exception> errors = new HashMap<>();
    private final Map<Field, IGenerator<?>> fieldIGeneratorMap = new LinkedHashMap<>();

    private final Set<IRuleRemark> ruleRemarks;
    private final Map<String, IRuleRemark> fieldRuleRemarkMap;

    protected DtoGenerator(Set<IRuleRemark> ruleRemarks, Map<String, IRuleRemark> fieldRuleRemarkMap) {
        this.ruleRemarks = ruleRemarks;
        this.fieldRuleRemarkMap = fieldRuleRemarkMap;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        protected final Set<IRuleRemark> ruleRemarks = new HashSet<>();
        protected final Map<String, IRuleRemark> fieldRuleRemarkMap = new HashMap<>();

        public Builder addRuleRemarks(RuleRemark ruleRemark, IRuleRemark... ruleRemarks) {
            this.ruleRemarks.add(ruleRemark);
            if (ruleRemarks.length > 0) {
                for (IRuleRemark remark : ruleRemarks) {
                    this.ruleRemarks.add(remark);
                }
            }
            return this;
        }

        public Builder addRuleRemarks(String filedName, IRuleRemark ruleRemark) {
            fieldRuleRemarkMap.put(filedName, ruleRemark);
            return this;
        }

        public DtoGenerator build() {
            return new DtoGenerator(ruleRemarks, fieldRuleRemarkMap);
        }
    }

    public <T> T generateDto(Class<T> dtoClass) {
        createDtoInstance(dtoClass);
        prepareGenerators();
        applyGenerators();
        return (T) dtoInstance;
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
                    if (generator instanceof IDtoDependentCustomGenerator) {
                        if (!((IDtoDependentCustomGenerator<?, ?>) generator).isDtoReady()) {
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
    }

    IGenerator<?> prepareGenerator(Field field) {
        IGenerator<?> generator = null;
        try {
            generator = selectGenerator(field);
            if (generator instanceof IDtoDependentCustomGenerator) {
                try {
                    ((IDtoDependentCustomGenerator) generator).setDto(dtoInstance);
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
            DoubleRules decimalBounds = field.getAnnotation(DoubleRules.class);
            if (decimalBounds != null) {
                return new BasicTypeGenerators.DoubleGenerator(
                        decimalBounds.maxValue(),
                        decimalBounds.minValue(),
                        decimalBounds.precision()
                );
            }
        }

        if (field.getType() == String.class) {
            StringRules stringBounds = field.getAnnotation(StringRules.class);
            if (stringBounds != null) {
                return new BasicTypeGenerators.StringGenerator(
                        stringBounds.maxSymbols(),
                        stringBounds.minSymbols(),
                        stringBounds.charset()
                );
            }
        }

        if (field.getType() == Long.class) {
            LongRules stringBounds = field.getAnnotation(LongRules.class);
            if (stringBounds != null) {
                return new BasicTypeGenerators.IntegerGenerator(
                        stringBounds.maxValue(),
                        stringBounds.minValue()
                );
            }
        }

        if (field.getType().isEnum()) {
            EnumRules enumBounds = field.getAnnotation(EnumRules.class);
            if (enumBounds != null) {
                return new BasicTypeGenerators.EnumGenerator(
                        enumBounds.possibleEnumNames(),
                        enumBounds.enumClass()
                );
            }
        }

        if (field.getType() == LocalDateTime.class) {
            LocalDateTimeRules enumBounds = field.getAnnotation(LocalDateTimeRules.class);
            if (enumBounds != null) {
                return new BasicTypeGenerators.LocalDateTimeGenerator(
                        enumBounds.leftShiftDays(),
                        enumBounds.rightShiftDays()
                );
            }
        }

        /*
         * Custom Generator
         */

        CustomGenerator customGeneratorRules = field.getAnnotation(CustomGenerator.class);

        if (customGeneratorRules != null) {
            try {
                Class<?> generatorClass = customGeneratorRules.generatorClass();
                Object generatorInstance = generatorClass.newInstance();
                if (generatorInstance instanceof ICustomGenerator) {
                    log.debug("Args {} have been obtained from Annotation: {}",
                            Arrays.asList(customGeneratorRules.args()), customGeneratorRules);
                    ((ICustomGenerator<?>) generatorInstance).setArgs(customGeneratorRules.args());
                }
                if (generatorInstance instanceof IGenerator) {
                    return (IGenerator<?>) generatorInstance;
                } else {
                    throw new RuntimeException();
                }
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InstantiationException e) {
                e.printStackTrace();
            }
        }

        return new BasicTypeGenerators.NullGenerator();
    }

}
