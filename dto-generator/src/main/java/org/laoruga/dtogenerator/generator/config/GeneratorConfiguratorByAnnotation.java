package org.laoruga.dtogenerator.generator.config;

import com.google.common.primitives.Primitives;
import lombok.extern.slf4j.Slf4j;
import org.laoruga.dtogenerator.DtoGeneratorBuildersTree;
import org.laoruga.dtogenerator.RemarksHolder;
import org.laoruga.dtogenerator.api.rules.*;
import org.laoruga.dtogenerator.api.rules.datetime.DateTimeRule;
import org.laoruga.dtogenerator.config.ConfigurationHolder;
import org.laoruga.dtogenerator.exceptions.DtoGeneratorException;
import org.laoruga.dtogenerator.generator.config.dto.*;
import org.laoruga.dtogenerator.generator.config.dto.datetime.DateTimeConfig;

import java.lang.annotation.Annotation;
import java.time.temporal.Temporal;
import java.util.function.Function;

/**
 * @author Il'dar Valitov
 * Created on 24.11.2022
 */
@Slf4j
public class GeneratorConfiguratorByAnnotation extends GeneratorConfigurator {

    private final Function<String, DtoGeneratorBuildersTree.Node> nestedDtoGeneratorBuilderSupplier;

    public GeneratorConfiguratorByAnnotation(ConfigurationHolder configuration,
                                             RemarksHolder remarksHolder,
                                             Function<String, DtoGeneratorBuildersTree.Node> nestedDtoGeneratorBuilderSupplier) {
        super(configuration, remarksHolder);
        this.nestedDtoGeneratorBuilderSupplier = nestedDtoGeneratorBuilderSupplier;
    }

    public ConfigDto createGeneratorConfig(Annotation rules,
                                           Class<?> fieldType,
                                           String fieldName) {

        Class<? extends Annotation> rulesClass = rules.annotationType();

        try {

            if (BooleanRule.class == rulesClass) {

                return mergeGeneratorConfigurations(
                        () -> new BooleanConfig((BooleanRule) rules),
                        booleanGeneratorSpecificConfig(fieldType, fieldName),
                        fieldType,
                        fieldName);

            } else if (StringRule.class == rulesClass) {

                return mergeGeneratorConfigurations(
                        () -> new StringConfig((StringRule) rules),
                        fieldType,
                        fieldName);

            } else if (DecimalRule.class == rulesClass) {

                if (Number.class.isAssignableFrom(Primitives.wrap(fieldType))) {

                    @SuppressWarnings("unchecked")
                    Class<? extends Number> fieldTypeNumber = (Class<? extends Number>) fieldType;

                    return mergeGeneratorConfigurations(
                            () -> new DecimalConfig((DecimalRule) rules, fieldTypeNumber),
                            decimalGeneratorSpecificConfig(fieldType, fieldName),
                            fieldType,
                            fieldName);

                }

                throw new IllegalArgumentException("Unexpected state. Field type '" + fieldType
                        + "' doesn't extend Number.class");

            } else if (NumberRule.class == rulesClass) {

                if (Number.class.isAssignableFrom(Primitives.wrap(fieldType))) {
                    @SuppressWarnings("unchecked")
                    Class<? extends Number> fieldTypeNumber = (Class<? extends Number>) fieldType;

                    return mergeGeneratorConfigurations(
                            () -> new NumberConfig((NumberRule) rules, fieldTypeNumber),
                            integerGeneratorSpecificConfig(fieldType, fieldName),
                            fieldType,
                            fieldName);
                }

                throw new IllegalArgumentException("Unexpected state. Field type '" + fieldType
                        + "' doesn't extend Number.class");

            } else if (EnumRule.class == rulesClass) {

                if (Enum.class.isAssignableFrom(fieldType)) {
                    return mergeGeneratorConfigurations(
                            () -> new EnumConfig((EnumRule) rules),
                            getEnumGeneratorSpecificConfig(fieldType),
                            fieldType,
                            fieldName
                    );
                }

                throw new IllegalArgumentException("Unexpected state. Field type '" + fieldType + "' is not Enum");

            } else if (DateTimeRule.class == rulesClass) {

                if (Temporal.class.isAssignableFrom(fieldType)) {

                    @SuppressWarnings("unchecked")
                    Class<? extends Temporal> fieldTypeTemporal = (Class<? extends Temporal>) fieldType;

                    return mergeGeneratorConfigurations(
                            () -> new DateTimeConfig((DateTimeRule) rules, fieldTypeTemporal),
                            fieldType,
                            fieldName);
                }

                throw new IllegalArgumentException("Unexpected state. Field type '" + fieldType + "' is not Temporal");

            } else if (NestedDtoRule.class == rulesClass) {

                NestedDtoRule nestedRule = (NestedDtoRule) rules;

                return NestedConfig.builder()
                        .ruleRemark(nestedRule.ruleRemark())
                        .dtoGeneratorBuilderTreeNode(nestedDtoGeneratorBuilderSupplier.apply(fieldName))
                        .build();

            } else {

                throw new DtoGeneratorException("Unable to rules class: '" + rulesClass + "'.");

            }

        } catch (Exception e) {
            throw new DtoGeneratorException("Unexpected error.", e);
        }
    }

}
