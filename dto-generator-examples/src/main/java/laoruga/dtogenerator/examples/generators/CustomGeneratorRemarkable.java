package laoruga.dtogenerator.examples.generators;

import laoruga.dtogenerator.api.DtoGenerator;
import laoruga.dtogenerator.api.markup.generators.AbstractCustomGeneratorRemarkable;
import laoruga.dtogenerator.api.markup.remarks.CustomRuleRemarkWrapper;
import laoruga.dtogenerator.api.util.RandomUtils;
import laoruga.dtogenerator.examples.dtos.Gender;
import laoruga.dtogenerator.examples.dtos.Person;

import java.util.Optional;

import static laoruga.dtogenerator.examples.generators.remark.PersonRemark.*;

/**
 * @author Il'dar Valitov
 * Created on 15.11.2022
 */
public class CustomGeneratorRemarkable extends AbstractCustomGeneratorRemarkable<Person> {

    @Override
    public Person generate() {
        Person person = DtoGenerator.builder(Person.class).build().generateDto();

        Optional<CustomRuleRemarkWrapper> maybeAgeRemark = getWrappedRemark(AGE_RANGE);
        Optional<CustomRuleRemarkWrapper> maybeWeightRemark = getWrappedRemark(WEIGHT_RANGE);
        Optional<CustomRuleRemarkWrapper> maybeGrowthRemark = getWrappedRemark(GROWTH_RANGE);
        Optional<CustomRuleRemarkWrapper> maybeGenderRemark = getWrappedRemark(GENDER);

        maybeAgeRemark.ifPresent(ruleRemarkWrapper -> person.setAge(randomInRange(ruleRemarkWrapper)));
        maybeWeightRemark.ifPresent(ruleRemarkWrapper -> person.setWeight(randomInRange(ruleRemarkWrapper)));
        maybeGrowthRemark.ifPresent(ruleRemarkWrapper -> person.setGrowth(randomInRange(ruleRemarkWrapper)));
        maybeGenderRemark.ifPresent(ruleRemarkWrapper -> person.setGender(Gender.valueOf(ruleRemarkWrapper.getArgs()[0])));

        return person;
    }

    private static int randomInRange(CustomRuleRemarkWrapper ruleRemarkWrapper) {
        String[] range = ruleRemarkWrapper.getArgs();
        return RandomUtils.nextInt(range[0], range[1]);
    }
}
