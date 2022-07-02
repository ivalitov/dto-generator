package laoruga.dtogenerator.api;

import laoruga.dtogenerator.api.exceptions.DtoGeneratorException;
import laoruga.dtogenerator.api.generators.NestedDtoGenerator;
import laoruga.dtogenerator.api.generators.basictypegenerators.BasicGeneratorsBuilders;
import laoruga.dtogenerator.api.markup.generators.*;
import laoruga.dtogenerator.api.markup.remarks.IRuleRemark;
import laoruga.dtogenerator.api.markup.rules.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static laoruga.dtogenerator.api.constants.BasicRuleRemark.NULL_VALUE;
import static laoruga.dtogenerator.api.util.ReflectionUtils.createInstance;
import static laoruga.dtogenerator.api.util.ReflectionUtils.createCollectionFieldInstance;

/**
 * @author Il'dar Valitov
 * Created on 15.05.2022
 */

@Slf4j
public class GeneratorBuildersProvider {

    private final GeneratorRemarksProvider generatorRemarksProvider;

    @Getter(AccessLevel.PACKAGE)
    private final Map<Class<? extends Annotation>, IGeneratorBuilder> overriddenBuilders;
    private final Map<String, IGeneratorBuilder> overriddenBuildersSpecificFields = new HashMap<>();

    GeneratorBuildersProvider(GeneratorRemarksProvider generatorRemarksProvider) {
        this.generatorRemarksProvider = generatorRemarksProvider;
        this.overriddenBuilders = new ConcurrentHashMap<>();
    }

    GeneratorBuildersProvider(GeneratorRemarksProvider generatorRemarksProvider,
                              Map<Class<? extends Annotation>, IGeneratorBuilder> overriddenBuilders) {
        this.generatorRemarksProvider = generatorRemarksProvider;
        this.overriddenBuilders = overriddenBuilders;
    }

    public GeneratorRemarksProvider getGeneratorRemarksProvider() {
        return generatorRemarksProvider;
    }

    void setGeneratorForFields(String fieldName, IGeneratorBuilder genBuilder) throws DtoGeneratorException {
        if (overriddenBuildersSpecificFields.containsKey(fieldName)) {
            throw new DtoGeneratorException("Generator has already been explicitly added for field: '" + fieldName + "'");
        }
        overriddenBuildersSpecificFields.put(fieldName, genBuilder);
    }

    void overrideGenerator(Class<? extends Annotation> rulesClass, @NonNull IGeneratorBuilder genBuilder) {
        if (overriddenBuilders.containsKey(rulesClass)) {
            throw new DtoGeneratorException("Generator has already been explicitly added for Rules: '" + rulesClass + "'");
        }
        overriddenBuilders.put(rulesClass, genBuilder);
    }

    /*
     * Various kinds generators providers
     */

    IGenerator<?> getBasicTypeGenerator(Field field, Annotation rules) {
        return getBasicTypeGenerator(field.getName(), field.getType(), rules);
    }

    IGenerator<?> getBasicTypeGenerator(String fieldName, Class<?> fieldType, Annotation rules) {
        Class<? extends Annotation> rulesClass = rules.annotationType();

        if (DoubleRule.class == rulesClass) {
            return getDoubleGenerator(fieldName, (DoubleRule) rules, fieldType == Double.TYPE);
        }

        if (StringRule.class == rulesClass) {
            return getStringGenerator(fieldName, (StringRule) rules);
        }

        if (IntegerRule.class == rulesClass) {
            return getIntegerGenerator(fieldName, (IntegerRule) rules, fieldType == Integer.TYPE);
        }

        if (LongRule.class == rulesClass) {
            return getLongGenerator(fieldName, (LongRule) rules, fieldType == Integer.TYPE);
        }

        if (EnumRule.class == rulesClass) {
            return getEnumGenerator(fieldName, (EnumRule) rules);
        }

        if (LocalDateTimeRule.class == rulesClass) {
            return getLocalDateTimeGenerator(fieldName, (LocalDateTimeRule) rules);
        }

        throw new DtoGeneratorException("Field " + fieldName + " hasn't been mapped with any basic generator.");
    }

    IGenerator<?> getCollectionTypeGenerator(String fieldName, Class<?> fieldType, Annotation rules, IGenerator<?> itemGenerator) {
        Class<? extends Annotation> rulesClass = rules.annotationType();

        if (ListRule.class == rulesClass) {
            return getListGenerator(fieldName, fieldType, (ListRule) rules, itemGenerator);
        }

        if (SetRule.class == rulesClass) {
            return getSetGenerator(fieldName, fieldType, (SetRule) rules, itemGenerator);
        }

        throw new DtoGeneratorException("Field " + fieldName + " hasn't been mapped with any collection generator.");
    }

    IGenerator<?> getNestedDtoGenerator(Field field,
                                        String[] fieldsPath,
                                        DtoGeneratorBuilder dtoGeneratorBuilder) {
        String[] pathToNestedDtoField = Arrays.copyOf(fieldsPath, fieldsPath.length + 1);
        pathToNestedDtoField[fieldsPath.length] = field.getName();
        return new NestedDtoGenerator<>(
                dtoGeneratorBuilder.buildNestedFieldGenerator(pathToNestedDtoField), field.getType());
    }

    IGenerator<?> getCustomGenerator(Annotation customGeneratorRules, Object dtoInstance) throws DtoGeneratorException {
        if (CustomGenerator.class == customGeneratorRules.annotationType()) {
            CustomGenerator customRules = (CustomGenerator) customGeneratorRules;
            Class<?> generatorClass = null;
            try {
                generatorClass = customRules.generatorClass();
                Object generatorInstance = createInstance(generatorClass);
                if (generatorInstance instanceof ICustomGeneratorArgs) {
                    log.debug("Args {} have been obtained from Annotation: {}",
                            Arrays.asList(customRules.args()), customRules);
                    ((ICustomGeneratorArgs<?>) generatorInstance).setArgs(customRules.args());
                }
                if (generatorInstance instanceof ICustomGeneratorDtoDependent) {
                    try {
                        ((ICustomGeneratorDtoDependent) generatorInstance).setDto(dtoInstance);
                    } catch (ClassCastException e) {
                        throw new DtoGeneratorException("ClassCastException while trying to set basic DTO into " +
                                "DTO dependent custom generator. Perhaps there is wrong argument type is passing into " +
                                "'setDto' method of generator class. " +
                                "Generator class: '" + generatorInstance.getClass() + "', " +
                                "Passing argument type: '" + dtoInstance.getClass() + "'", e);
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
        throw new DtoGeneratorException("Unexpected error. Unexpected annotation instead of: " + CustomGenerator.class);
    }

    /*
     * Basic type generators providers
     */

    private IGenerator<?> getStringGenerator(String fieldName, StringRule stringRule) {
        if (isGeneratorOverridden(fieldName, stringRule)) {
            return getOverriddenGenerator(fieldName, stringRule);
        } else {
            IRuleRemark remark = generatorRemarksProvider.isBasicRuleRemarkExists(fieldName) ?
                    generatorRemarksProvider.getBasicRuleRemark(fieldName) :
                    stringRule.ruleRemark();
            return BasicGeneratorsBuilders.stringBuilder()
                    .minLength(stringRule.minSymbols())
                    .maxLength(stringRule.maxSymbols())
                    .charset(stringRule.charset())
                    .chars(stringRule.chars())
                    .ruleRemark(remark)
                    .mask(stringRule.mask())
                    .maskWildcard(stringRule.maskWildcard())
                    .maskTypeMarker(stringRule.maskTypeMarker())
                    .build();
        }
    }

    private IGenerator<?> getDoubleGenerator(String fieldName, DoubleRule doubleRule, boolean isPrimitive) {
        if (isGeneratorOverridden(fieldName, doubleRule)) {
            return getOverriddenGenerator(fieldName, doubleRule);
        } else {
            IRuleRemark remark = generatorRemarksProvider.isBasicRuleRemarkExists(fieldName) ?
                    generatorRemarksProvider.getBasicRuleRemark(fieldName) :
                    doubleRule.ruleRemark();
            if (remark == NULL_VALUE && isPrimitive) {
                log.debug("Primitive field '" + fieldName + "' can't be null, it will be assigned to '0'");
                return (IGenerator<Double>) () -> 0D;
            }
            return BasicGeneratorsBuilders.doubleBuilder()
                    .minValue(doubleRule.minValue())
                    .maxValue(doubleRule.maxValue())
                    .precision(doubleRule.precision())
                    .ruleRemark(remark)
                    .build();
        }
    }

    private IGenerator<?> getIntegerGenerator(String fieldName, IntegerRule integerRule, boolean isPrimitive) {
        if (isGeneratorOverridden(fieldName, integerRule)) {
            return getOverriddenGenerator(fieldName, integerRule);
        } else {
            IRuleRemark remark = generatorRemarksProvider.isBasicRuleRemarkExists(fieldName) ?
                    generatorRemarksProvider.getBasicRuleRemark(fieldName) :
                    integerRule.ruleRemark();
            if (remark == NULL_VALUE && isPrimitive) {
                log.debug("Primitive field '" + fieldName + "' can't be null, it will be assigned to '0'");
                return (IGenerator<Integer>) () -> 0;
            }
            return BasicGeneratorsBuilders.integerBuilder()
                    .minValue(integerRule.minValue())
                    .maxValue(integerRule.maxValue())
                    .ruleRemark(remark)
                    .build();
        }
    }

    private IGenerator<?> getLongGenerator(String fieldName, LongRule longRule, boolean isPrimitive) {
        if (isGeneratorOverridden(fieldName, longRule)) {
            return getOverriddenGenerator(fieldName, longRule);
        } else {
            IRuleRemark remark = generatorRemarksProvider.isBasicRuleRemarkExists(fieldName) ?
                    generatorRemarksProvider.getBasicRuleRemark(fieldName) :
                    longRule.ruleRemark();
            if (remark == NULL_VALUE && isPrimitive) {
                log.debug("Primitive field '" + fieldName + "' can't be null, it will be assigned to '0'");
                return (IGenerator<Long>) () -> 0L;
            }
            return BasicGeneratorsBuilders.longBuilder()
                    .minValue(longRule.minValue())
                    .maxValue(longRule.maxValue())
                    .ruleRemark(remark)
                    .build();
        }
    }

    private IGenerator<?> getEnumGenerator(String fieldName, EnumRule enumRule) {
        if (isGeneratorOverridden(fieldName, enumRule)) {
            return getOverriddenGenerator(fieldName, enumRule);
        } else {
            IRuleRemark remark = generatorRemarksProvider.isBasicRuleRemarkExists(fieldName) ?
                    generatorRemarksProvider.getBasicRuleRemark(fieldName) :
                    enumRule.ruleRemark();
            return BasicGeneratorsBuilders.enumBuilder()
                    .enumClass(enumRule.enumClass())
                    .possibleEnumNames(enumRule.possibleEnumNames())
                    .ruleRemark(remark)
                    .build();
        }
    }

    private IGenerator<?> getLocalDateTimeGenerator(String fieldName, LocalDateTimeRule localDateTimeRule) {
        if (isGeneratorOverridden(fieldName, localDateTimeRule)) {
            return getOverriddenGenerator(fieldName, localDateTimeRule);
        } else {
            IRuleRemark remark = generatorRemarksProvider.isBasicRuleRemarkExists(fieldName) ?
                    generatorRemarksProvider.getBasicRuleRemark(fieldName) :
                    localDateTimeRule.ruleRemark();
            return BasicGeneratorsBuilders.localDateTimeBuilder()
                    .leftShiftDays(localDateTimeRule.leftShiftDays())
                    .rightShiftDays(localDateTimeRule.rightShiftDays())
                    .ruleRemark(remark)
                    .build();
        }
    }

    /*
     * Collection generators providers
     */

    IGenerator<?> getListGenerator(String fieldName, Class<?> fieldType, ListRule listRule, IGenerator<?> listItemGenerator) {
        if (isGeneratorOverridden(fieldName, listRule)) {
            return getOverriddenGenerator(fieldName, listRule);
        } else {
            IRuleRemark remark = generatorRemarksProvider.isBasicRuleRemarkExists(fieldName) ?
                    generatorRemarksProvider.getBasicRuleRemark(fieldName) :
                    listRule.ruleRemark();
            return BasicGeneratorsBuilders.collectionBuilder()
                    .minSize(listRule.minSize())
                    .maxSize(listRule.maxSize())
                    .listInstance(createCollectionFieldInstance(fieldType, listRule.listClass()))
                    .itemGenerator(listItemGenerator)
                    .ruleRemark(remark)
                    .build();
        }
    }

    IGenerator<?> getSetGenerator(String fieldName, Class<?> fieldType, SetRule setRule, IGenerator<?> listItemGenerator) {
        if (isGeneratorOverridden(fieldName, setRule)) {
            return getOverriddenGenerator(fieldName, setRule);
        } else {
            IRuleRemark remark = generatorRemarksProvider.isBasicRuleRemarkExists(fieldName) ?
                    generatorRemarksProvider.getBasicRuleRemark(fieldName) :
                    setRule.ruleRemark();
            return BasicGeneratorsBuilders.collectionBuilder()
                    .minSize(setRule.minSize())
                    .maxSize(setRule.maxSize())
                    .listInstance(createCollectionFieldInstance(fieldType, setRule.setClass()))
                    .itemGenerator(listItemGenerator)
                    .ruleRemark(remark)
                    .build();
        }
    }

    /*
     * Utils
     */

    private boolean isGeneratorOverridden(String fieldName, Annotation rules) {
        return overriddenBuildersSpecificFields.containsKey(fieldName) ||
                overriddenBuilders.containsKey(rules.annotationType());
    }

    private IGenerator<?> getOverriddenGenerator(String fieldName, Annotation rules) {
        return overriddenBuildersSpecificFields.getOrDefault(
                fieldName,
                overriddenBuilders.get(rules.annotationType())
        ).build();
    }
}
