package org.laoruga.dtogenerator.examples.generators.custom;

import org.laoruga.dtogenerator.DtoGenerator;
import org.laoruga.dtogenerator.api.generators.AbstractCustomGeneratorRemarkable;
import org.laoruga.dtogenerator.api.remarks.CustomRuleRemarkWithArgs;
import org.laoruga.dtogenerator.examples.generators.custom.remark.PersonRemark;
import org.laoruga.dtogenerator.util.RandomUtils;
import org.laoruga.dtogenerator.examples.dto.Gender;
import org.laoruga.dtogenerator.examples.dto.Person;

import java.util.Optional;


/**
 * @author Il'dar Valitov
 * Created on 15.11.2022
 */
public class CustomGeneratorRemarkable extends AbstractCustomGeneratorRemarkable<Person> {

    @Override
    public Person generate() {
        Person person = DtoGenerator.builder(Person.class).build().generateDto();

        Optional<CustomRuleRemarkWithArgs> maybeAgeRemark = getWrappedRemark(PersonRemark.AGE_RANGE);
        Optional<CustomRuleRemarkWithArgs> maybeWeightRemark = getWrappedRemark(PersonRemark.WEIGHT_RANGE);
        Optional<CustomRuleRemarkWithArgs> maybeGrowthRemark = getWrappedRemark(PersonRemark.GROWTH_RANGE);
        Optional<CustomRuleRemarkWithArgs> maybeGenderRemark = getWrappedRemark(PersonRemark.GENDER);

        maybeAgeRemark.ifPresent(ruleRemarkWrapper -> person.setAge(randomInRange(ruleRemarkWrapper)));
        maybeWeightRemark.ifPresent(ruleRemarkWrapper -> person.setWeight(randomInRange(ruleRemarkWrapper)));
        maybeGrowthRemark.ifPresent(ruleRemarkWrapper -> person.setGrowth(randomInRange(ruleRemarkWrapper)));
        maybeGenderRemark.ifPresent(ruleRemarkWrapper -> person.setGender(Gender.valueOf(ruleRemarkWrapper.getArgs()[0])));

        return person;
    }

    private static int randomInRange(CustomRuleRemarkWithArgs ruleRemarkWrapper) {
        String[] range = ruleRemarkWrapper.getArgs();
        return RandomUtils.nextInt(range[0], range[1]);
    }
}
