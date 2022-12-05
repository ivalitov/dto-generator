package org.laoruga.dtogenerator.generators.basictypegenerators;

import lombok.*;
import org.laoruga.dtogenerator.api.generators.IGenerator;
import org.laoruga.dtogenerator.api.generators.IGeneratorBuilder;
import org.laoruga.dtogenerator.api.generators.IGeneratorBuilderConfigurable;
import org.laoruga.dtogenerator.api.remarks.IRuleRemark;
import org.laoruga.dtogenerator.api.rules.EnumRule;
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
    private final Class<? extends Enum> enumClass;
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
                "' not found in Class: '" + enumClass + "'");
    }

    public static EnumGeneratorBuilder builder() {
        return new EnumGeneratorBuilder();
    }

    public static final class EnumGeneratorBuilder implements IGeneratorBuilderConfigurable {

        ConfigDto configDto;

        private EnumGeneratorBuilder() {
            this.configDto = new ConfigDto();
        }

        public EnumGeneratorBuilder possibleEnumNames(String... possibleEnumNames) {
            configDto.possibleEnumNames = possibleEnumNames;
            return this;
        }

        @SuppressWarnings("unchecked")
        public EnumGeneratorBuilder enumClass(Class<?> enumClass) {
            if (enumClass.isEnum()) {
                configDto.enumClass = (Class<? extends Enum<?>>) enumClass;
            } else {
                throw new DtoGeneratorException(new ClassCastException("Enum class expected"));
            }
            return this;
        }

        public EnumGeneratorBuilder ruleRemark(IRuleRemark ruleRemark) {
            configDto.ruleRemark = ruleRemark;
            return this;
        }

        public EnumGenerator build() {
            return build(configDto, false);
        }

        public EnumGenerator build(IConfigDto configDto, boolean merge) {
            if (merge) {
                configDto.merge(this.configDto);
            }
            ConfigDto enumConfigDto = (ConfigDto) configDto;

            if (enumConfigDto.enumClass == null) {
                throw new DtoGeneratorException("Enum class wasn't set for generator.");
            }
            if (enumConfigDto.possibleEnumNames.length == 0) {
                enumConfigDto.possibleEnumNames = Arrays
                        .stream(enumConfigDto.enumClass.getEnumConstants())
                        .map(Enum::name).toArray(String[]::new);
            }
            return new EnumGenerator(
                    enumConfigDto.possibleEnumNames,
                    enumConfigDto.enumClass,
                    enumConfigDto.ruleRemark);
        }
    }

    @Builder
    @Getter
    @Setter
    @AllArgsConstructor
    public static class ConfigDto implements IConfigDto{
        private String[] possibleEnumNames;
        private Class<? extends Enum> enumClass;
        private IRuleRemark ruleRemark;

        public ConfigDto(EnumRule enumRule) {
            possibleEnumNames = enumRule.possibleEnumNames();
            ruleRemark = enumRule.ruleRemark();
        }

        public ConfigDto() {}

        @Override
        public Class<? extends IGeneratorBuilder> getBuilderClass() {
            return EnumGeneratorBuilder.class;
        }

        public void merge(IConfigDto from) {
            ConfigDto configDto = (ConfigDto) from;
            if (configDto.getPossibleEnumNames() != null) this.possibleEnumNames = configDto.getPossibleEnumNames();
            if (configDto.getEnumClass() != null) this.enumClass = configDto.getEnumClass();
            if (configDto.getRuleRemark() != null) this.ruleRemark = configDto.getRuleRemark();
        }
    }
}
