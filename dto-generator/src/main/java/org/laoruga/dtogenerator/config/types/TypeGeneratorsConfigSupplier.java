package org.laoruga.dtogenerator.config.types;

import org.laoruga.dtogenerator.generator.config.dto.*;
import org.laoruga.dtogenerator.generator.config.dto.datetime.DateTimeConfig;

import java.time.temporal.Temporal;
import java.util.Collection;
import java.util.Map;

/**
 * @author Il'dar Valitov
 * Created on 21.02.2023
 */
public interface TypeGeneratorsConfigSupplier {

    StringConfig getStringConfig();

    IntegralCommonConfig getNumberConfig();

    DecimalCommonConfig getDecimalConfig();

    EnumConfig getEnumConfig();

    DateTimeConfig getDateTimeConfig(Class<? extends Temporal> dateTimeType);

    CollectionConfig getCollectionConfig(Class<? extends Collection> collectionType);

    ArrayConfig getArrayConfig(Class<?> arrayType);

    MapConfig getMapConfig(Class<? extends Map> mapType);
}
