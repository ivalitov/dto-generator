package org.laoruga.dtogenerator.functional.data.customgenerator;

import org.laoruga.dtogenerator.api.generators.custom.ICustomGeneratorArgs;

/**
 * @author Il'dar Valitov
 * Created on 11.12.2022
 */
public class CustomIntegerGenerator implements ICustomGeneratorArgs<Integer> {
    int generated;

    @Override
    public void setArgs(String... args) {
        generated = Integer.parseInt(args[0]);
    }

    @Override
    public Integer generate() {
        return generated;
    }
}
