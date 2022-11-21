package laoruga.dtogenerator.examples.dtos;

import laoruga.dtogenerator.api.markup.rules.CustomRule;
import laoruga.dtogenerator.examples.generators.CustomGenerator;
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
