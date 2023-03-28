package org.laoruga.dtogenerator.generator;

import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import org.laoruga.dtogenerator.api.generators.IGenerator;
import org.laoruga.dtogenerator.api.remarks.IRuleRemark;
import org.laoruga.dtogenerator.constants.RuleRemark;
import org.laoruga.dtogenerator.exceptions.DtoGeneratorException;
import org.laoruga.dtogenerator.generator.config.dto.EnumConfig;
import org.laoruga.dtogenerator.util.RandomUtils;

import java.util.Arrays;
import java.util.Comparator;

/**
 * @author Il'dar Valitov
 * Created on 19.05.2022
 */

@AllArgsConstructor
public class EnumGenerator implements IGenerator<Enum> {

    private final String[] possibleEnumNames;
    private final Class<? extends Enum<?>> enumClass;
    private final IRuleRemark ruleRemark;

    public EnumGenerator(EnumConfig enumConfig) {
        if (enumConfig.getEnumClass() == null) {
            throw new DtoGeneratorException("Enum class wasn't set for generator.");
        }
        if (enumConfig.getPossibleEnumNames().length == 0) {
            enumConfig.setPossibleEnumNames(Arrays
                    .stream(enumConfig.getEnumClass().getEnumConstants())
                    .map(Enum::name).toArray(String[]::new));
        }
        possibleEnumNames = enumConfig.getPossibleEnumNames();
        enumClass = enumConfig.getEnumClass();
        ruleRemark = enumConfig.getRuleRemark();
    }

    @Override
    @SneakyThrows
    public Enum<?> generate() {
        String[] sortedEnumNames = Arrays.stream(possibleEnumNames)
                .sorted(Comparator.comparing(String::length))
                .toArray(String[]::new);
        String enumInstanceName;
        if (ruleRemark == RuleRemark.MIN_VALUE) {
            enumInstanceName = sortedEnumNames[0];
        } else if (ruleRemark == RuleRemark.MAX_VALUE) {
            enumInstanceName = sortedEnumNames[sortedEnumNames.length - 1];
        } else if (ruleRemark == RuleRemark.RANDOM_VALUE) {
            int count = sortedEnumNames.length;
            enumInstanceName = sortedEnumNames[RandomUtils.RANDOM.nextInt(count)];
        } else if (ruleRemark == RuleRemark.NULL_VALUE) {
            return null;
        } else {
            throw new IllegalStateException("Unexpected value " + ruleRemark);
        }
        for (Enum<?> enumConstant : enumClass.getEnumConstants()) {
            if (enumConstant.name().equals(enumInstanceName)) {
                return enumConstant;
            }
        }
        throw new DtoGeneratorException("Enum instance with name: '" + enumInstanceName +
                "' not found in Class: '" + enumClass + "'");
    }

}
