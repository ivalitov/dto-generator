package org.laoruga.dtogenerator.generators.basictypegenerators;

import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import org.laoruga.dtogenerator.api.generators.IGenerator;
import org.laoruga.dtogenerator.api.generators.IGeneratorBuilder;
import org.laoruga.dtogenerator.api.remarks.IRuleRemark;
import org.laoruga.dtogenerator.constants.BasicRuleRemark;
import org.laoruga.dtogenerator.exceptions.DtoGeneratorException;
import org.laoruga.dtogenerator.util.RandomUtils;

import java.util.Arrays;
import java.util.Comparator;

/**
 * @author Il'dar Valitov
 * Created on 19.05.2022
 */

@AllArgsConstructor
public class EnumGenerator implements IGenerator<Enum<?>> {

    private final String[] possibleEnumNames;
    private final Class<? extends Enum<?>> enumClass;
    private final IRuleRemark ruleRemark;

    @Override
    @SneakyThrows
    public Enum<?> generate() {
        String[] sortedEnumNames = Arrays.stream(possibleEnumNames)
                .sorted(Comparator.comparing(String::length))
                .toArray(String[]::new);
        String enumInstanceName;
        if (ruleRemark == BasicRuleRemark.MIN_VALUE) {
            enumInstanceName = sortedEnumNames[0];
        } else if (ruleRemark == BasicRuleRemark.MAX_VALUE) {
            enumInstanceName = sortedEnumNames[sortedEnumNames.length - 1];
        } else if (ruleRemark == BasicRuleRemark.RANDOM_VALUE) {
            int count = sortedEnumNames.length;
            enumInstanceName = sortedEnumNames[RandomUtils.getRandom().nextInt(count)];
        } else if (ruleRemark == BasicRuleRemark.NULL_VALUE) {
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
                "' has not been found in Class: '" + enumClass + "'");
    }

    public static EnumGeneratorBuilder builder() {
        return new EnumGeneratorBuilder();
    }

    public static final class EnumGeneratorBuilder implements IGeneratorBuilder<IGenerator<?>> {
        private String[] possibleEnumNames;
        private Class<? extends Enum<?>> enumClass;
        private IRuleRemark ruleRemark;

        private EnumGeneratorBuilder() {
        }

        public EnumGeneratorBuilder possibleEnumNames(String[] possibleEnumNames) {
            this.possibleEnumNames = possibleEnumNames;
            return this;
        }

        public EnumGeneratorBuilder enumClass(Class<? extends Enum<?>> enumClass) {
            this.enumClass = enumClass;
            return this;
        }

        public EnumGeneratorBuilder ruleRemark(IRuleRemark ruleRemark) {
            this.ruleRemark = ruleRemark;
            return this;
        }

        public EnumGenerator build() {
            return new EnumGenerator(possibleEnumNames, enumClass, ruleRemark);
        }
    }
}