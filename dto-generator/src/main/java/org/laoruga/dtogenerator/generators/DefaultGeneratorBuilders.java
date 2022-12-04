package org.laoruga.dtogenerator.generators;

import org.laoruga.dtogenerator.api.rules.*;

import java.util.Arrays;
import java.util.List;

import static org.laoruga.dtogenerator.generators.GeneratorBuildersHolder.GenBuilderInfo.createInstance;

/**
 * @author Il'dar Valitov
 * Created on 24.11.2022
 */
public class DefaultGeneratorBuilders {

    private static GeneratorBuildersHolder instance;

    public static synchronized GeneratorBuildersHolder getInstance() {
        if (instance == null) {
            List<GeneratorBuildersHolder.GenBuilderInfo> basicBuilderInfoList = Arrays.asList(

                    // general
                    createInstance(StringRule.class, RulesInstance.stringRule.generatedType(), GeneratorBuilders::stringBuilder),
                    createInstance(IntegerRule.class, RulesInstance.integerRule.generatedType(), Integer.TYPE, GeneratorBuilders::integerBuilder),
                    createInstance(DoubleRule.class, RulesInstance.doubleRule.generatedType(), Double.TYPE, GeneratorBuilders::doubleBuilder),
                    createInstance(LongRule.class, RulesInstance.longRule.generatedType(), Long.TYPE, GeneratorBuilders::longBuilder),
                    createInstance(EnumRule.class, RulesInstance.enumRule.generatedType(), GeneratorBuilders::enumBuilder),
                    createInstance(LocalDateTimeRule.class, RulesInstance.localDateTimeRule.generatedType(), GeneratorBuilders::localDateTimeBuilder),

                    // collection
                    createInstance(SetRule.class, RulesInstance.setRule.generatedType(), GeneratorBuilders::setBuilder),
                    createInstance(ListRule.class, RulesInstance.listRule.generatedType(), GeneratorBuilders::listBuilder),

                    // extended
                    createInstance(CustomRule.class, RulesInstance.customRule.generatedType(), GeneratorBuilders::customBuilder),
                    createInstance(NestedDtoRule.class, RulesInstance.nestedDtoRule.generatedType(), GeneratorBuilders::nestedDtoBuilder)

            );

            instance = new GeneratorBuildersHolder(basicBuilderInfoList);
        }
        return instance;
    }

}
