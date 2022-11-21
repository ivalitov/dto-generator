package org.laoruga.dtogenerator.examples.generators.custom;

import org.laoruga.dtogenerator.api.generators.ICustomGenerator;
import org.laoruga.dtogenerator.util.RandomUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author Il'dar Valitov
 * Created on 15.11.2022
 */
public class CustomGenerator implements ICustomGenerator<Map<String, String>> {

    protected static final int MIN_PLANETS_NUMBER = 3;
    protected static final List<String> planets =
            Arrays.asList("Mercury", "Venus", "Earth", "Mars", "Jupiter", "Saturn", "Uranus", "Neptune");

    @Override
    public synchronized Map<String, String> generate() {
        Collections.shuffle(planets);
        Integer numberOfPlanets = RandomUtils.nextInt(MIN_PLANETS_NUMBER, planets.size());
        return planets.subList(0, numberOfPlanets).stream().collect(Collectors.toMap(
                i -> i,
                i -> RandomUtils.getRandomItemFromList("inhabited", "uninhabited", "possibly inhabited")
        ));
    }
}
