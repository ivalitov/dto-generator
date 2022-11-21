package laoruga.dto.generator.examples;

import org.laoruga.dtogenerator.DtoGenerator;
import org.laoruga.dtogenerator.examples.dto.Office;
import org.laoruga.dtogenerator.examples.dto.TwoFields;
import org.laoruga.dtogenerator.examples.dto.SolarSystem;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.laoruga.dtogenerator.examples.generators.custom.remark.PersonRemark;

import java.util.Arrays;

import static org.laoruga.dtogenerator.examples.dto.Gender.FEMALE;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Il'dar Valitov
 * Created on 21.11.2022
 */
@Slf4j
public class ExamplesTest {

    @Test
    void customGeneratorSimple() {
        SolarSystem solarSystem = DtoGenerator.builder(SolarSystem.class).build().generateDto();

        log.info(Utils.toJson(solarSystem));

        assertNotNull(solarSystem);
        assertTrue(solarSystem.getPlanetHabitabilityMap().size() >= 3);
        assertTrue(Arrays.asList("Mercury", "Venus", "Earth", "Mars", "Jupiter", "Saturn", "Uranus", "Neptune")
                .containsAll(solarSystem.getPlanetHabitabilityMap().keySet()));
        assertTrue(Arrays.asList("inhabited", "uninhabited", "possibly inhabited")
                .containsAll(solarSystem.getPlanetHabitabilityMap().values()));
    }

    @Test
    void customGeneratorDtoDependent() {
        TwoFields twoFields = DtoGenerator.builder(TwoFields.class).build().generateDto();

        log.info(Utils.toJson(twoFields));

        assertNotNull(twoFields);
        assertThat(twoFields.getFirstField(), containsString(twoFields.getSecondField()));
    }

    @Test
    void customGeneratorRemarkable() {
        Office office = DtoGenerator.builder(Office.class)
                .addRuleRemarkForFields(
                        PersonRemark.WEIGHT_RANGE.wrap("50", "70"),
                        PersonRemark.GROWTH_RANGE.wrap("130", "150"),
                        PersonRemark.AGE_RANGE.wrap("18", "30"),
                        PersonRemark.GENDER.wrap("FEMALE"))
                .build()
                .generateDto();

        log.info(Utils.toJson(office));

        assertNotNull(office);
        assertEquals(2, office.getPeople().size());
        office.getPeople().forEach(female ->
                assertAll(
                        () -> assertThat(female.getFio(), matchesRegex("^[A-Z]{1}[a-z]{5} [A-Z]{1}[a-z]{5} [A-Z]{1}[a-z]{5}$")),
                        () -> assertThat(female.getAge(), both(greaterThanOrEqualTo(1)).and(lessThanOrEqualTo(101))),
                        () -> assertThat(female.getHairColor(), oneOf("red", "yellow", "black", "gray", "white")),
                        () -> assertThat(female.getPets(), nullValue()),
                        () -> assertThat(female.getGender(), equalTo(FEMALE)),
                        () -> assertThat(female.getWeight(), both(greaterThanOrEqualTo(50)).and(lessThanOrEqualTo(70))),
                        () -> assertThat(female.getGrowth(), both(greaterThanOrEqualTo(130)).and(lessThanOrEqualTo(150)))
                )
        );

    }

}