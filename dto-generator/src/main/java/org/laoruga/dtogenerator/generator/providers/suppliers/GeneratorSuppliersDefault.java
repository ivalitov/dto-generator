package org.laoruga.dtogenerator.generator.providers.suppliers;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.laoruga.dtogenerator.api.rules.*;
import org.laoruga.dtogenerator.api.rules.datetime.DateTimeRule;
import org.laoruga.dtogenerator.generator.*;
import org.laoruga.dtogenerator.generator.config.dto.*;
import org.laoruga.dtogenerator.generator.config.dto.datetime.DateTimeConfigDto;

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
                        config -> new BooleanGenerator((BooleanConfigDto) config)));

        generatorSuppliers.addSuppliersInfo(
                GeneratorSupplierInfo.createInstances(
                        StringRule.class,
                        config -> new StringGenerator((StringConfigDto) config)));

        generatorSuppliers.addSuppliersInfo(
                GeneratorSupplierInfo.createInstances(
                        NumberRule.class,
                        config -> new NumberGenerator((NumberConfigDto) config)));

        generatorSuppliers.addSuppliersInfo(
                GeneratorSupplierInfo.createInstances(
                        DecimalRule.class,
                        config -> new DecimalGenerator((DecimalConfigDto) config)));

        generatorSuppliers.addSuppliersInfo(
                GeneratorSupplierInfo.createInstances(
                        EnumRule.class,
                        config -> new EnumGenerator((EnumConfigDto) config)));

        generatorSuppliers.addSuppliersInfo(
                GeneratorSupplierInfo.createInstances(
                        DateTimeRule.class,
                        config -> new DateTimeGenerator((DateTimeConfigDto) config)));

        // collection

        generatorSuppliers.addSuppliersInfo(
                GeneratorSupplierInfo.createInstances(
                        CollectionRule.class,
                        config -> new CollectionGenerator((CollectionConfigDto) config)));

        // map

        generatorSuppliers.addSuppliersInfo(
                GeneratorSupplierInfo.createInstances(
                        MapRule.class,
                        config -> new MapGenerator((MapConfigDto) config)));

        // array

        generatorSuppliers.addSuppliersInfo(
                GeneratorSupplierInfo.createInstances(
                        ArrayRule.class,
                        config -> new ArrayGenerator((ArrayConfigDto) config)));

        // extended
        generatorSuppliers.addSuppliersInfo(
                GeneratorSupplierInfo.createInstances(
                        CustomRule.class,
                        config -> new CustomGenerator((CustomConfigDto) config)));

        generatorSuppliers.addSuppliersInfo(
                GeneratorSupplierInfo.createInstances(
                        NestedDtoRule.class,
                        config -> new NestedDtoGenerator((NestedConfigDto) config)));

        return generatorSuppliers;
    }

}
