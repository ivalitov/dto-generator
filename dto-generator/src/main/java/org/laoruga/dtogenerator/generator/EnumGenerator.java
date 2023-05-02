package org.laoruga.dtogenerator.generator;

import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import org.laoruga.dtogenerator.api.RuleRemark;
import org.laoruga.dtogenerator.api.generators.Generator;
import org.laoruga.dtogenerator.constants.Boundary;
import org.laoruga.dtogenerator.exceptions.DtoGeneratorException;
import org.laoruga.dtogenerator.generator.config.dto.EnumConfig;
import org.laoruga.dtogenerator.util.RandomUtils;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author Il'dar Valitov
 * Created on 19.05.2022
 */

@AllArgsConstructor
public class EnumGenerator implements Generator<Enum> {

    private final String[] sortedEnumNamesSelection;
    private final Map<String, Enum<?>> allEnumValues;
    private final RuleRemark ruleRemark;
    private int numberOfEnumConstantsOfMinLength = 0;
    private int numberOfEnumConstantsOfMaxLength = 0;

    public EnumGenerator(EnumConfig enumConfig) {
        if (enumConfig.getEnumClass() == null) {
            throw new DtoGeneratorException("Enum class wasn't set for generator.");
        }
        allEnumValues = Arrays
                .stream(enumConfig.getEnumClass().getEnumConstants())
                .collect(Collectors.toMap(Enum::name, v -> v));
        ruleRemark = enumConfig.getRuleRemark();

        String[] enumConstantNames;
        if (enumConfig.getPossibleEnumNames().length == 0) {
            enumConstantNames = Arrays
                    .stream(enumConfig.getEnumClass().getEnumConstants())
                    .map(Enum::name)
                    .toArray(String[]::new);
        } else {
            enumConstantNames = enumConfig.getPossibleEnumNames();
            validateEnumNames(enumConstantNames, enumConfig.getEnumClass());
        }

        sortedEnumNamesSelection = Arrays.stream(enumConstantNames)
                .sorted(Comparator.comparing(String::length))
                .toArray(String[]::new);
    }

    @Override
    @SneakyThrows
    public Enum<?> generate() {
        String enumConstantName;
        switch ((Boundary) ruleRemark) {

            case MIN_VALUE:

                if (numberOfEnumConstantsOfMinLength == 0) {
                    numberOfEnumConstantsOfMinLength = countNumberOfEnumConstantsOfMinLength();
                }

                int randomIdx = RandomUtils.nextInt(0, numberOfEnumConstantsOfMinLength - 1);
                enumConstantName = sortedEnumNamesSelection[randomIdx];
                break;

            case MAX_VALUE:

                if (numberOfEnumConstantsOfMaxLength == 0) {
                    numberOfEnumConstantsOfMaxLength = countNumberOfEnumConstantsOfMaxLength();
                }

                randomIdx = RandomUtils.nextInt(
                        sortedEnumNamesSelection.length - numberOfEnumConstantsOfMaxLength,
                        sortedEnumNamesSelection.length - 1);
                enumConstantName = sortedEnumNamesSelection[randomIdx];

                break;

            case NULL_VALUE:
                return null;

            case RANDOM_VALUE:
            case NOT_DEFINED:
                enumConstantName = sortedEnumNamesSelection[RandomUtils.nextInt(0, sortedEnumNamesSelection.length - 1)];
                break;

            default:
                throw new IllegalStateException("Unexpected value " + ruleRemark);
        }

        return allEnumValues.get(enumConstantName);
    }

    private void validateEnumNames(String[] enumConstantNames, Class<? extends Enum<?>> enumClass) {
        for (String selectedEnumName : enumConstantNames) {
            if (!allEnumValues.containsKey(selectedEnumName)) {
                throw new DtoGeneratorException("Enum constant with name: " +
                        "'" + selectedEnumName + "' not found in the Class: '" + enumClass + "'");
            }
        }
    }

    private int countNumberOfEnumConstantsOfMinLength() {
        int number = 1;
        int minLength = sortedEnumNamesSelection[0].length();
        for (int i = 1; i < sortedEnumNamesSelection.length; i++) {
            if (sortedEnumNamesSelection[i].length() == minLength) {
                number++;
            } else {
                break;
            }
        }
        return number;
    }

    private int countNumberOfEnumConstantsOfMaxLength() {
        int number = 1;
        int maxLength = sortedEnumNamesSelection[sortedEnumNamesSelection.length - 1].length();
        for (int i = sortedEnumNamesSelection.length - 2; i >= 0; i--) {
            if (sortedEnumNamesSelection[i].length() == maxLength) {
                number++;
            } else {
                break;
            }
        }
        return number;
    }
}
