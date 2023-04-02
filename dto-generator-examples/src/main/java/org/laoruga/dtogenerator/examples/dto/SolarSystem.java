package org.laoruga.dtogenerator.examples.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import org.laoruga.dtogenerator.api.rules.CustomRule;
import org.laoruga.dtogenerator.examples.generators.custom.CustomGeneratorExample;

import java.util.Map;

/**
 * @author Il'dar Valitov
 * Created on 15.11.2022
 */
@NoArgsConstructor
public class SolarSystem {

    @CustomRule(generatorClass = CustomGeneratorExample.class)
    @Getter
    Map<String, String> planetHabitabilityMap;

}
