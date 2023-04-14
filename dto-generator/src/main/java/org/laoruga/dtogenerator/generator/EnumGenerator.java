package org.laoruga.dtogenerator.generator;

import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import org.laoruga.dtogenerator.api.RuleRemark;
import org.laoruga.dtogenerator.api.generators.Generator;
import org.laoruga.dtogenerator.constants.BoundaryConfig;
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
public class EnumGenerator implements Generator<Enum> {

    private final String[] possibleEnumNames;
    private final Class<? extends Enum<?>> enumClass;
    private final RuleRemark ruleRemark;

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
        switch ((BoundaryConfig) ruleRemark) {

            case MIN_VALUE:
                enumInstanceName = sortedEnumNames[0];
                break;

            case MAX_VALUE:
                enumInstanceName = sortedEnumNames[sortedEnumNames.length - 1];
                break;

            case NULL_VALUE:
                return null;

            case RANDOM_VALUE:
            case NOT_DEFINED:
                int count = sortedEnumNames.length;
                enumInstanceName = sortedEnumNames[RandomUtils.RANDOM.nextInt(count)];
                break;

            default:
                throw new IllegalStateException("Unexpected value " + ruleRemark);
        }
        for (Enum<?> enumConstant : enumClass.getEnumConstants()) {
            if (enumConstant.name().equals(enumInstanceName)) {
                return enumConstant;
            }
        }
        throw new DtoGeneratorException("Enum instance with name: " +
                "'" + enumInstanceName + "' not found in Class: '" + enumClass + "'");
    }

}
