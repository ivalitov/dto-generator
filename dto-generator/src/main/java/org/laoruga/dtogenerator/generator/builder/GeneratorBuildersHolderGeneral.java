package org.laoruga.dtogenerator.generator.builder;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.laoruga.dtogenerator.api.rules.*;
import org.laoruga.dtogenerator.generator.*;

/**
 * @author Il'dar Valitov
 * Created on 24.11.2022
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class GeneratorBuildersHolderGeneral {

    private static GeneratorBuildersHolder instance;

    public static synchronized GeneratorBuildersHolder getInstance() {
        if (instance == null) {
            instance = createInstance();
        }
        return instance;
    }

    private static GeneratorBuildersHolder createInstance() {
        GeneratorBuildersHolder generatorBuildersHolder = new GeneratorBuildersHolder();

        // general
        generatorBuildersHolder.addBuilders(
                GeneratorBuilderInfo.createInstances(
                        BooleanRule.class,
                        BooleanRule.GENERATED_TYPES,
                        BooleanGenerator::builder));

        generatorBuildersHolder.addBuilders(
                GeneratorBuilderInfo.createInstances(
                        StringRule.class,
                        StringRule.GENERATED_TYPES,
                        StringGenerator::builder));

        generatorBuildersHolder.addBuilders(
                GeneratorBuilderInfo.createInstances(
                        NumberRule.class,
                        NumberRule.GENERATED_TYPES,
                        NumberGenerator::builder));


        generatorBuildersHolder.addBuilders(
                GeneratorBuilderInfo.createInstances(
                        DecimalRule.class,
                        DecimalRule.GENERATED_TYPES,
                        DecimalGenerator::builder));

        generatorBuildersHolder.addBuilders(
                GeneratorBuilderInfo.createInstances(
                        EnumRule.class,
                        EnumRule.GENERATED_TYPES,
                        EnumGenerator::builder));

        generatorBuildersHolder.addBuilders(
                GeneratorBuilderInfo.createInstances(
                        LocalDateTimeRule.class,
                        LocalDateTimeRule.GENERATED_TYPES,
                        LocalDateTimeGenerator::builder));

        // collection

        generatorBuildersHolder.addBuilders(
                GeneratorBuilderInfo.createInstances(
                        CollectionRule.class,
                        CollectionRule.GENERATED_TYPES,
                        CollectionGenerator::builder));

        // extended
        generatorBuildersHolder.addBuilders(
                GeneratorBuilderInfo.createInstances(
                        CustomRule.class,
                        CustomRule.GENERATED_TYPES,
                        CustomGenerator::builder));

        generatorBuildersHolder.addBuilders(
                GeneratorBuilderInfo.createInstances(
                        NestedDtoRule.class,
                        NestedDtoRule.GENERATED_TYPES,
                        NestedDtoGenerator::builder));

        return generatorBuildersHolder;
    }

}
