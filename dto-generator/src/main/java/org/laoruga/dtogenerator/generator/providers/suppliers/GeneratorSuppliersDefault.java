package org.laoruga.dtogenerator.generator.providers.suppliers;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.laoruga.dtogenerator.api.rules.*;
import org.laoruga.dtogenerator.api.rules.datetime.DateTimeRule;
import org.laoruga.dtogenerator.generator.*;
import org.laoruga.dtogenerator.generator.config.dto.*;
import org.laoruga.dtogenerator.generator.config.dto.datetime.DateTimeConfig;

/**
 * @author Il'dar Valitov
 * Created on 24.11.2022
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class GeneratorSuppliersDefault {

    private static GeneratorSuppliers instance;

    public static synchronized GeneratorSuppliers getInstance() {
        if (instance == null) {
            instance = createInstance();
        }
        return instance;
    }

    private static GeneratorSuppliers createInstance() {
        GeneratorSuppliers generatorSuppliers = new GeneratorSuppliers();

        // general
        generatorSuppliers.addSuppliersInfo(
                GeneratorSupplierInfo.createInstances(
                        BooleanRule.class,
                        config -> new BooleanGenerator((BooleanConfig) config)));

        generatorSuppliers.addSuppliersInfo(
                GeneratorSupplierInfo.createInstances(
                        StringRule.class,
                        config -> new StringGenerator((StringConfig) config)));

        generatorSuppliers.addSuppliersInfo(
                GeneratorSupplierInfo.createInstances(
                        IntegerRule.class,
                        config -> new IntegerNumberGenerator((IntegerConfig) config)));

        generatorSuppliers.addSuppliersInfo(
                GeneratorSupplierInfo.createInstances(
                        DecimalRule.class,
                        config -> new DecimalNumberGenerator((DecimalConfig) config)));

        generatorSuppliers.addSuppliersInfo(
                GeneratorSupplierInfo.createInstances(
                        EnumRule.class,
                        config -> new EnumGenerator((EnumConfig) config)));

        generatorSuppliers.addSuppliersInfo(
                GeneratorSupplierInfo.createInstances(
                        DateTimeRule.class,
                        config -> new DateTimeGenerator((DateTimeConfig) config)));

        // collection

        generatorSuppliers.addSuppliersInfo(
                GeneratorSupplierInfo.createInstances(
                        CollectionRule.class,
                        config -> new CollectionGenerator((CollectionConfig) config)));

        // map

        generatorSuppliers.addSuppliersInfo(
                GeneratorSupplierInfo.createInstances(
                        MapRule.class,
                        config -> new MapGenerator((MapConfig) config)));

        // array

        generatorSuppliers.addSuppliersInfo(
                GeneratorSupplierInfo.createInstances(
                        ArrayRule.class,
                        config -> new ArrayGenerator((ArrayConfig) config)));

        return generatorSuppliers;
    }

}
