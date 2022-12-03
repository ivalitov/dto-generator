package org.laoruga.dtogenerator.generators;

import org.laoruga.dtogenerator.api.rules.*;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import static org.laoruga.dtogenerator.generators.GeneratorBuildersHolder.GenBuilderInfo.createInstance;

/**
 * @author Il'dar Valitov
 * Created on 24.11.2022
 */
public class StaticGeneratorBuildersHolder {

    private static GeneratorBuildersHolder instance;

    public static synchronized GeneratorBuildersHolder getInstance() {
        if (instance == null) {
            List<GeneratorBuildersHolder.GenBuilderInfo> basicBuilderInfoList = Arrays.asList(

                    // general
                    createInstance(StringRule.class, String.class, GeneratorBuilders::stringBuilder),
                    createInstance(IntegerRule.class, Integer.class, Integer.TYPE, GeneratorBuilders::integerBuilder),
                    createInstance(DoubleRule.class, Double.class, Double.TYPE, GeneratorBuilders::doubleBuilder),
                    createInstance(LongRule.class, Long.class, Long.TYPE, GeneratorBuilders::longBuilder),
                    createInstance(EnumRule.class, Enum.class, GeneratorBuilders::enumBuilder),
                    createInstance(LocalDateTimeRule.class, LocalDateTime.class, GeneratorBuilders::localDateTimeBuilder),

                    // collection
                    createInstance(SetRule.class, Set.class, GeneratorBuilders::setBuilder),
                    createInstance(ListRule.class, List.class, GeneratorBuilders::listBuilder),

                    // extended
                    createInstance(CustomRule.class, Object.class, GeneratorBuilders::customBuilder),
                    createInstance(NestedDtoRule.class, Object.class, GeneratorBuilders::nestedDtoBuilder)

            );

            instance = new GeneratorBuildersHolder(basicBuilderInfoList);
        }
        return instance;
    }

}
