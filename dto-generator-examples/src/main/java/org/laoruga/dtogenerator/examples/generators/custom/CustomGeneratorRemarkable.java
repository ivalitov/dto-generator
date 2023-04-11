package org.laoruga.dtogenerator.examples.generators.custom;

import org.laoruga.dtogenerator.DtoGenerator;
import org.laoruga.dtogenerator.api.generators.custom.CustomGeneratorConfigMap;
import org.laoruga.dtogenerator.examples.dto.Gender;
import org.laoruga.dtogenerator.examples.dto.Person;
import org.laoruga.dtogenerator.examples.generators.custom.remark.PersonRemark;
import org.laoruga.dtogenerator.util.RandomUtils;

import java.util.Map;
import java.util.Optional;


/**
 * @author Il'dar Valitov
 * Created on 15.11.2022
 */
public class CustomGeneratorRemarkable implements CustomGeneratorConfigMap<Person> {

    Map<String, String> configMap;

    @Override
    public Person generate() {
        Person person = DtoGenerator.builder(Person.class).build().generateDto();

        Optional<String> maybeMinAge = Optional.ofNullable(configMap.get(PersonRemark.MIN_AGE.name()));
        Optional<String> maybeMaxAge = Optional.ofNullable(configMap.get(PersonRemark.MAX_AGE.name()));
        Optional<String> maybeMinWeight = Optional.ofNullable(configMap.get(PersonRemark.MIN_WEIGHT.name()));
        Optional<String> maybeMaxWeight = Optional.ofNullable(configMap.get(PersonRemark.MAX_WEIGHT.name()));
        Optional<String> maybeMinGrowth = Optional.ofNullable(configMap.get(PersonRemark.MIN_GROWTH.name()));
        Optional<String> maybeMaxGrowth = Optional.ofNullable(configMap.get(PersonRemark.MAX_GROWTH.name()));
        Optional<String> maybeGenderRemark = Optional.ofNullable(configMap.get(PersonRemark.GENDER.name()));


        person.setAge(RandomUtils.nextInt(maybeMinAge.orElse("1"), maybeMaxAge.orElse("99")));
        person.setWeight(RandomUtils.nextInt(maybeMinWeight.orElse("5"), maybeMaxWeight.orElse("250")));
        person.setGrowth(RandomUtils.nextInt(maybeMinGrowth.orElse("18"), maybeMaxGrowth.orElse("85")));
        person.setGender(maybeGenderRemark.map(Gender::valueOf).orElse(RandomUtils.getRandomItem(Gender.values())));

        return person;
    }

    private static int randomInRange(String min, String max) {
        return RandomUtils.nextInt(min, max);
    }

    @Override
    public void setConfigMap(Map<String, String> configMap) {
        this.configMap = configMap;
    }
}
