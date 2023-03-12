package org.laoruga.dtogenerator.config.types;

import org.laoruga.dtogenerator.generator.configs.*;

import java.util.Collection;

/**
 * @author Il'dar Valitov
 * Created on 21.02.2023
 */
public interface TypeGeneratorsConfigSupplier {

    StringConfigDto getStringConfig();

    NumberCommonConfigDto getNumberConfig();

    DecimalConfigDto getDoubleConfig();

    LocalDateTimeConfigDto getLocalDateTimeConfig();

    EnumConfigDto getEnumConfig();

    CollectionConfigDto getCollectionConfig(Class<? extends Collection> collectionType);

}
