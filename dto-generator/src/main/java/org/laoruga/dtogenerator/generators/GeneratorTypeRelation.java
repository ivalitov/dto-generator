package org.laoruga.dtogenerator.generators;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * @author Il'dar Valitov
 * Created on 23.11.2022
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class GeneratorTypeRelation {

    private static GeneratorTypeRelation instance = new GeneratorTypeRelation();

    public static GeneratorTypeRelation getInstance() {
        return instance;
    }


}
