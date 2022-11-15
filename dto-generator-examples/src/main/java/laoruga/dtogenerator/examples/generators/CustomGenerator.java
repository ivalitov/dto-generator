package laoruga.dtogenerator.examples.generators;

import laoruga.dtogenerator.api.markup.generators.ICustomGenerator;
import laoruga.dtogenerator.api.util.RandomUtils;

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

    protected static final List<String> planets =
            Arrays.asList("Mercury", "Venus", "Earth", "Mars", "Jupiter", "Saturn", "Uranus", "Neptune");

    @Override
    public synchronized Map<String, String> generate() {
        Collections.shuffle(planets);
        Integer numberOfPlanets = RandomUtils.nextInt(1, planets.size());
        return planets.subList(0, numberOfPlanets).stream().collect(Collectors.toMap(
                i -> i,
                i -> RandomUtils.nextBoolean() ? "inhabited" : "uninhabited"
        ));
    }
}
