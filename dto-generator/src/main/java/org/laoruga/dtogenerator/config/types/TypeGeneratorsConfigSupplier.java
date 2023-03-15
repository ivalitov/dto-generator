package org.laoruga.dtogenerator.config.types;

import org.laoruga.dtogenerator.generator.configs.*;
import org.laoruga.dtogenerator.generator.configs.datetime.DateTimeConfigDto;

import java.time.temporal.Temporal;
import java.util.Collection;

/**
 * @author Il'dar Valitov
 * Created on 21.02.2023
 */
public interface TypeGeneratorsConfigSupplier {

    StringConfigDto getStringConfig();

    NumberCommonConfigDto getNumberConfig();

    DecimalCommonConfigDto getDecimalConfig();

    DateTimeConfigDto getDateTimeConfig(Class<? extends Temporal> dateTimeType);

    EnumConfigDto getEnumConfig();

    CollectionConfigDto getCollectionConfig(Class<? extends Collection> collectionType);

}
