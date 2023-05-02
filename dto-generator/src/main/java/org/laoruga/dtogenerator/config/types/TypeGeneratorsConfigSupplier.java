package org.laoruga.dtogenerator.config.types;

import org.laoruga.dtogenerator.generator.config.dto.*;
import org.laoruga.dtogenerator.generator.config.dto.datetime.DateTimeConfig;

import java.time.temporal.Temporal;
import java.util.*;

/**
 * @author Il'dar Valitov
 * Created on 21.02.2023
 */
public interface TypeGeneratorsConfigSupplier {

    /**
     * @return configuration for strings
     */
    StringConfig getStringConfig();

    /**
     * Configuration facade for any supported integral type.
     * Use different setters to different integral types.
     *
     * @return configuration of integral types
     */
    IntegralConfigCommonConfig getIntegralConfig();

    /**
     * Configuration facade for any supported decimal type.
     * Use different setters to different decimal types.
     *
     * @return configuration of decimal types
     */
    DecimalCommonConfig getDecimalConfig();

    /**
     * @return configuration of any enum type
     */
    EnumConfig getEnumConfig();

    /**
     * Configuration apples to passed type only.
     *
     * @param dateTimeType class or interface to configure
     * @return configuration of required {@code Temporal} type
     */
    DateTimeConfig getDateTimeConfig(Class<? extends Temporal> dateTimeType);

    /**
     * Configuration apples to both:
     * <ul>
     * <li>provided class or interface</li>
     * <li>classes and interfaces that extends or implements provided type</li>
     * </ul>
     * For example, config set for {@link List} will apply to {@link LinkedList}, {@link ArrayList} etc.
     *
     * @param collectionType class or interface to configure
     * @return configuration of required collection type and all its subtypes
     */
    CollectionConfig getCollectionConfig(Class<? extends Collection> collectionType);

    /**
     * Configuration apples to provided array class.
     *
     * @param arrayType class or interface to configure
     *                  for example: int[].class, Integer[].class
     *                  (these are two different types which processed independently)
     * @return configuration of required array type
     */
    ArrayConfig getArrayConfig(Class<?> arrayType);

    /**
     * Configuration apples to both:
     * <ul>
     * <li>provided class or interface</li>
     * <li>classes and interfaces that extends or implements provided type</li>
     * </ul>
     *
     * @param mapType class or interface to configure
     * @return configuration of required map type and all its subtypes
     */
    MapConfig getMapConfig(Class<? extends Map> mapType);
}
