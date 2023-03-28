package org.laoruga.dtogenerator.config.types;

import org.laoruga.dtogenerator.generator.config.dto.*;
import org.laoruga.dtogenerator.generator.config.dto.datetime.DateTimeConfigDto;

import java.time.temporal.Temporal;
import java.util.Collection;
import java.util.Map;

/**
 * @author Il'dar Valitov
 * Created on 21.02.2023
 */
public interface TypeGeneratorsConfigSupplier {

    StringConfigDto getStringConfig();

    NumberCommonConfigDto getNumberConfig();

    DecimalCommonConfigDto getDecimalConfig();

    EnumConfigDto getEnumConfig();

    DateTimeConfigDto getDateTimeConfig(Class<? extends Temporal> dateTimeType);

    CollectionConfigDto getCollectionConfig(Class<? extends Collection> collectionType);

    ArrayConfigDto getArrayConfig(Class<?> arrayType);

    MapConfigDto getMapConfig(Class<? extends Map> mapType);
}
