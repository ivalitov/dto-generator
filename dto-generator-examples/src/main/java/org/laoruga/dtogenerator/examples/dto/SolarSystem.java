package org.laoruga.dtogenerator.examples.dto;

import org.laoruga.dtogenerator.api.rules.CustomRule;
import org.laoruga.dtogenerator.examples.generators.custom.CustomGenerator;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * @author Il'dar Valitov
 * Created on 15.11.2022
 */
@NoArgsConstructor
@Data
public class SolarSystem {

    @CustomRule(generatorClass = CustomGenerator.class)
    Map<String, String> planetHabitabilityMap;

}
