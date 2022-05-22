package laoruga.dtogenerator.api;

import laoruga.dtogenerator.api.exceptions.DtoGeneratorException;
import laoruga.dtogenerator.api.generators.basictypegenerators.BasicGeneratorsBuilders;
import laoruga.dtogenerator.api.markup.generators.IGenerator;
import laoruga.dtogenerator.api.markup.generators.IGeneratorBuilder;
import laoruga.dtogenerator.api.markup.remarks.IRuleRemark;
import laoruga.dtogenerator.api.markup.rules.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

import static laoruga.dtogenerator.api.markup.remarks.BasicRuleRemark.NULL_VALUE;

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

    IGenerator<?> getStringGenerator(String fieldName, StringRules stringRules) {
        if (isGeneratorOverridden(fieldName, stringRules)) {
            return getOverriddenGenerator(fieldName, stringRules);
        } else {
            return BasicGeneratorsBuilders.stringBuilder()
                    .minLength(stringRules.minSymbols())
                    .maxLength(stringRules.maxSymbols())
                    .charset(stringRules.charset())
                    .chars(stringRules.chars())
                    .ruleRemark(stringRules.ruleRemark())
                    .build();
        }
    }

    IGenerator<?> getDoubleGenerator(String fieldName, DoubleRules doubleRules, boolean isPrimitive) {
        if (isGeneratorOverridden(fieldName, doubleRules)) {
            return getOverriddenGenerator(fieldName, doubleRules);
        } else {
            IRuleRemark remark = generatorRemarksProvider.isBasicRuleRemarkExists(fieldName) ?
                    generatorRemarksProvider.getBasicRuleRemark(fieldName) :
                    doubleRules.ruleRemark();
            if (remark == NULL_VALUE && isPrimitive) {
                log.debug("Primitive field '" + fieldName + "' can't be null, it will be assigned to '0'");
                return (IGenerator<Double>) () -> 0D;
            }
            return BasicGeneratorsBuilders.doubleBuilder()
                    .minValue(doubleRules.minValue())
                    .maxValue(doubleRules.maxValue())
                    .precision(doubleRules.precision())
                    .ruleRemark(remark)
                    .build();
        }
    }

    IGenerator<?> getIntegerGenerator(String fieldName, IntegerRules integerRules, boolean isPrimitive) {
        if (isGeneratorOverridden(fieldName, integerRules)) {
            return getOverriddenGenerator(fieldName, integerRules);
        } else {
            IRuleRemark remark = generatorRemarksProvider.isBasicRuleRemarkExists(fieldName) ?
                    generatorRemarksProvider.getBasicRuleRemark(fieldName) :
                    integerRules.ruleRemark();
            if (remark == NULL_VALUE && isPrimitive) {
                log.debug("Primitive field '" + fieldName + "' can't be null, it will be assigned to '0'");
                return (IGenerator<Integer>) () -> 0;
            }
            return BasicGeneratorsBuilders.integerBuilder()
                    .minValue(integerRules.minValue())
                    .maxValue(integerRules.maxValue())
                    .ruleRemark(remark)
                    .build();
        }
    }

    public IGenerator<?> getLongGenerator(String fieldName, LongRules longRules, boolean isPrimitive) {
        if (isGeneratorOverridden(fieldName, longRules)) {
            return getOverriddenGenerator(fieldName, longRules);
        } else {
            IRuleRemark remark = generatorRemarksProvider.isBasicRuleRemarkExists(fieldName) ?
                    generatorRemarksProvider.getBasicRuleRemark(fieldName) :
                    longRules.ruleRemark();
            if (remark == NULL_VALUE && isPrimitive) {
                log.debug("Primitive field '" + fieldName + "' can't be null, it will be assigned to '0'");
                return (IGenerator<Long>) () -> 0L;
            }
            return BasicGeneratorsBuilders.longBuilder()
                    .minValue(longRules.minValue())
                    .maxValue(longRules.maxValue())
                    .ruleRemark(remark)
                    .build();
        }
    }

    public IGenerator<?> getEnumGenerator(String fieldName, EnumRules enumRules) {
        if (isGeneratorOverridden(fieldName, enumRules)) {
            return getOverriddenGenerator(fieldName, enumRules);
        } else {
            return BasicGeneratorsBuilders.enumBuilder()
                    .enumClass(enumRules.enumClass())
                    .possibleEnumNames(enumRules.possibleEnumNames())
                    .ruleRemark(enumRules.ruleRemark())
                    .build();
        }
    }

    public IGenerator<?> getLocalDateTimeGenerator(String fieldName, LocalDateTimeRules localDateTimeRules) {
        if (isGeneratorOverridden(fieldName, localDateTimeRules)) {
            return getOverriddenGenerator(fieldName, localDateTimeRules);
        } else {
            return BasicGeneratorsBuilders.localDateTimeBuilder()
                    .leftShiftDays(localDateTimeRules.leftShiftDays())
                    .rightShiftDays(localDateTimeRules.rightShiftDays())
                    .ruleRemark(localDateTimeRules.ruleRemark())
                    .build();
        }
    }

    private boolean isGeneratorOverridden(String fieldName, Annotation rules) {
        return overriddenBuildersSpecificFields.containsKey(fieldName) ||
                overriddenBuilders.containsKey(rules.getClass());
    }

    private IGenerator<?> getOverriddenGenerator(String fieldName, Annotation rules) {
        return overriddenBuildersSpecificFields.getOrDefault(
                fieldName,
                Objects.requireNonNull(overriddenBuilders.get(rules.getClass()))).build();
    }
}
