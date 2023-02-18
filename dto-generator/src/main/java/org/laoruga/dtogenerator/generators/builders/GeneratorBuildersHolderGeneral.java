package org.laoruga.dtogenerator.generators.builders;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.laoruga.dtogenerator.api.rules.*;
import org.laoruga.dtogenerator.rules.RulesInstance;

import static org.laoruga.dtogenerator.generators.builders.GeneratorBuilderInfo.createInstance;

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
                        StringRule.class,
                        RulesInstance.stringRule.generatedType(),
                        GeneratorBuildersFactory::stringBuilder));
        generatorBuildersHolder.addBuilder(
                GeneratorBuilderInfo.createInstance(
                        IntegerRule.class,
                        RulesInstance.integerRule.generatedType(),
                        Integer.TYPE,
                        GeneratorBuildersFactory::integerBuilder));
        generatorBuildersHolder.addBuilder(
                GeneratorBuilderInfo.createInstance(
                        DoubleRule.class,
                        RulesInstance.doubleRule.generatedType(),
                        Double.TYPE,
                        GeneratorBuildersFactory::doubleBuilder));
        generatorBuildersHolder.addBuilder(
                GeneratorBuilderInfo.createInstance(
                        LongRule.class,
                        RulesInstance.longRule.generatedType(),
                        Long.TYPE,
                        GeneratorBuildersFactory::longBuilder));
        generatorBuildersHolder.addBuilder(
                GeneratorBuilderInfo.createInstance(
                        EnumRule.class,
                        RulesInstance.enumRule.generatedType(),
                        GeneratorBuildersFactory::enumBuilder));
        generatorBuildersHolder.addBuilder(
                GeneratorBuilderInfo.createInstance(
                        LocalDateTimeRule.class,
                        RulesInstance.localDateTimeRule.generatedType(),
                        GeneratorBuildersFactory::localDateTimeBuilder));

        // collection
        generatorBuildersHolder.addBuilder(
                GeneratorBuilderInfo.createInstance(
                        SetRule.class,
                        RulesInstance.setRule.generatedType(),
                        GeneratorBuildersFactory::setBuilder));
        generatorBuildersHolder.addBuilder(
                GeneratorBuilderInfo.createInstance(
                        ListRule.class,
                        RulesInstance.listRule.generatedType(),
                        GeneratorBuildersFactory::listBuilder));

        // extended
        generatorBuildersHolder.addBuilder(
                GeneratorBuilderInfo.createInstance(
                        CustomRule.class,
                        RulesInstance.customRule.generatedType(),
                        GeneratorBuildersFactory::customBuilder));
        generatorBuildersHolder.addBuilder(
                GeneratorBuilderInfo.createInstance(
                        NestedDtoRule.class,
                        RulesInstance.nestedDtoRule.generatedType(),
                        GeneratorBuildersFactory::nestedDtoBuilder));

        return generatorBuildersHolder;
    }

}
