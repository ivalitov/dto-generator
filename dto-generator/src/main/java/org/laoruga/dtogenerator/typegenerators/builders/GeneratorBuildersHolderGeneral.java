package org.laoruga.dtogenerator.typegenerators.builders;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.laoruga.dtogenerator.api.rules.*;
import org.laoruga.dtogenerator.rules.RulesInstance;

import java.util.Arrays;
import java.util.List;

import static org.laoruga.dtogenerator.typegenerators.builders.GeneratorBuildersHolder.GenBuilderInfo.createInstance;

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
        List<GeneratorBuildersHolder.GenBuilderInfo> basicBuilderInfoList = Arrays.asList(

                // general
                createInstance(StringRule.class, RulesInstance.stringRule.generatedType(), GeneratorBuildersFactory::stringBuilder),
                createInstance(IntegerRule.class, RulesInstance.integerRule.generatedType(), Integer.TYPE, GeneratorBuildersFactory::integerBuilder),
                createInstance(DoubleRule.class, RulesInstance.doubleRule.generatedType(), Double.TYPE, GeneratorBuildersFactory::doubleBuilder),
                createInstance(LongRule.class, RulesInstance.longRule.generatedType(), Long.TYPE, GeneratorBuildersFactory::longBuilder),
                createInstance(EnumRule.class, RulesInstance.enumRule.generatedType(), GeneratorBuildersFactory::enumBuilder),
                createInstance(LocalDateTimeRule.class, RulesInstance.localDateTimeRule.generatedType(), GeneratorBuildersFactory::localDateTimeBuilder),

                // collection
                createInstance(SetRule.class, RulesInstance.setRule.generatedType(), GeneratorBuildersFactory::setBuilder),
                createInstance(ListRule.class, RulesInstance.listRule.generatedType(), GeneratorBuildersFactory::listBuilder),

                // extended
                createInstance(CustomRule.class, RulesInstance.customRule.generatedType(), GeneratorBuildersFactory::customBuilder),
                createInstance(NestedDtoRule.class, RulesInstance.nestedDtoRule.generatedType(), GeneratorBuildersFactory::nestedDtoBuilder)

        );
        return new GeneratorBuildersHolder(basicBuilderInfoList);
    }

}
