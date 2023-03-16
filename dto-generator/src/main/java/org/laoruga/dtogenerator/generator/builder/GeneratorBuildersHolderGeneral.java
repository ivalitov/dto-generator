package org.laoruga.dtogenerator.generator.builder;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.laoruga.dtogenerator.api.rules.*;
import org.laoruga.dtogenerator.api.rules.datetime.DateTimeRule;
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
        generatorBuildersHolder.addBuilder(
                GeneratorBuilderInfo.createInstance(
                        BooleanRule.class,
                        BooleanRule.GENERATED_TYPE,
                        BooleanGenerator::builder));

        generatorBuildersHolder.addBuilder(
                GeneratorBuilderInfo.createInstance(
                        StringRule.class,
                        StringRule.GENERATED_TYPE,
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

        generatorBuildersHolder.addBuilder(
                GeneratorBuilderInfo.createInstance(
                        EnumRule.class,
                        EnumRule.GENERATED_TYPE,
                        EnumGenerator::builder));

        generatorBuildersHolder.addBuilder(
                GeneratorBuilderInfo.createInstance(
                        DateTimeRule.class,
                        DateTimeRule.GENERATED_TYPE,
                        DateTimeGenerator::builder));

        // collection

        generatorBuildersHolder.addBuilder(
                GeneratorBuilderInfo.createInstance(
                        CollectionRule.class,
                        CollectionRule.GENERATED_TYPE,
                        CollectionGenerator::builder));

        // extended
        generatorBuildersHolder.addBuilder(
                GeneratorBuilderInfo.createInstance(
                        CustomRule.class,
                        CustomRule.GENERATED_TYPE,
                        CustomGenerator::builder));

        generatorBuildersHolder.addBuilder(
                GeneratorBuilderInfo.createInstance(
                        NestedDtoRule.class,
                        NestedDtoRule.GENERATED_TYPE,
                        NestedDtoGenerator::builder));

        return generatorBuildersHolder;
    }

}
