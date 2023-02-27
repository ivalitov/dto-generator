package org.laoruga.dtogenerator.config.types;

import org.laoruga.dtogenerator.generator.configs.*;

/**
 * @author Il'dar Valitov
 * Created on 21.02.2023
 */
public interface TypeGeneratorsConfigSupplier {

    StringConfigDto getStringConfig();

    IntegerConfigDto getIntegerConfig();

    LongConfigDto getLongConfig();

    DoubleConfigDto getDoubleConfig();

    LocalDateTimeConfigDto getLocalDateTimeConfig();

    EnumConfigDto getEnumConfig();

    CollectionConfigDto getListConfig();

    CollectionConfigDto getSetConfig();
}
