package laoruga.dtogenerator.api.generators.basictypegenerators;

import laoruga.dtogenerator.api.markup.generators.IGenerator;
import laoruga.dtogenerator.api.markup.remarks.BasicRuleRemark;
import laoruga.dtogenerator.api.markup.remarks.IRuleRemark;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Random;

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
            enumInstanceName = sortedEnumNames[new Random().nextInt(count)];
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
        throw new RuntimeException("Enum instance with name: '" + enumInstanceName +
                "' has not been found in Class: '" + enumClass + "'");
    }

    public static EnumGeneratorBuilder builder() {
        return new EnumGeneratorBuilder();
    }

    public static final class EnumGeneratorBuilder {
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
