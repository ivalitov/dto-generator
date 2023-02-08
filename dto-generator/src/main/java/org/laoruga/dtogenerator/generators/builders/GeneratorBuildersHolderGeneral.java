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
            instance = init();
        }
        return instance;
    }

    private static GeneratorBuildersHolder init() {
        GeneratorBuildersHolder generatorBuildersHolder = new GeneratorBuildersHolder();

        // general
        generatorBuildersHolder.addBuilder(
                createInstance(
                        StringRule.class,
                        RulesInstance.stringRule.generatedType(),
                        GeneratorBuildersFactory::stringBuilder));
        generatorBuildersHolder.addBuilder(
                createInstance(
                        IntegerRule.class,
                        RulesInstance.integerRule.generatedType(),
                        Integer.TYPE,
                        GeneratorBuildersFactory::integerBuilder));
        generatorBuildersHolder.addBuilder(
                createInstance(
                        DoubleRule.class,
                        RulesInstance.doubleRule.generatedType(),
                        Double.TYPE,
                        GeneratorBuildersFactory::doubleBuilder));
        generatorBuildersHolder.addBuilder(
                createInstance(
                        LongRule.class,
                        RulesInstance.longRule.generatedType(),
                        Long.TYPE,
                        GeneratorBuildersFactory::longBuilder));
        generatorBuildersHolder.addBuilder(
                createInstance(
                        EnumRule.class,
                        RulesInstance.enumRule.generatedType(),
                        GeneratorBuildersFactory::enumBuilder));
        generatorBuildersHolder.addBuilder(
                createInstance(
                        LocalDateTimeRule.class,
                        RulesInstance.localDateTimeRule.generatedType(),
                        GeneratorBuildersFactory::localDateTimeBuilder));

        // collection
        generatorBuildersHolder.addBuilder(
                createInstance(
                        SetRule.class,
                        RulesInstance.setRule.generatedType(),
                        GeneratorBuildersFactory::setBuilder));
        generatorBuildersHolder.addBuilder(
                createInstance(
                        ListRule.class,
                        RulesInstance.listRule.generatedType(),
                        GeneratorBuildersFactory::listBuilder));

        // extended
        generatorBuildersHolder.addBuilder(
                createInstance(
                        CustomRule.class,
                        RulesInstance.customRule.generatedType(),
                        GeneratorBuildersFactory::customBuilder));
        generatorBuildersHolder.addBuilder(
                createInstance(
                        NestedDtoRule.class,
                        RulesInstance.nestedDtoRule.generatedType(),
                        GeneratorBuildersFactory::nestedDtoBuilder));

        return generatorBuildersHolder;
    }

}
