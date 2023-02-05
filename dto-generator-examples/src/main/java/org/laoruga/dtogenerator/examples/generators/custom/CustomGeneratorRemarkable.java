package org.laoruga.dtogenerator.examples.generators.custom;

import org.laoruga.dtogenerator.DtoGenerator;
import org.laoruga.dtogenerator.api.generators.custom.ICustomGeneratorRemarkable;
import org.laoruga.dtogenerator.api.remarks.ICustomRuleRemark;
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
public class CustomGeneratorRemarkable implements ICustomGeneratorRemarkable<Person> {

    Map<ICustomRuleRemark, ICustomRuleRemark> ruleRemarks;

    @Override
    public Person generate() {
        Person person = DtoGenerator.builder(Person.class).build().generateDto();

        Optional<ICustomRuleRemark> maybeAgeRemark = Optional.ofNullable(ruleRemarks.get(PersonRemark.AGE_RANGE));
        Optional<ICustomRuleRemark> maybeWeightRemark = Optional.ofNullable(ruleRemarks.get(PersonRemark.WEIGHT_RANGE));
        Optional<ICustomRuleRemark> maybeGrowthRemark = Optional.ofNullable(ruleRemarks.get(PersonRemark.GROWTH_RANGE));
        Optional<ICustomRuleRemark> maybeGenderRemark = Optional.ofNullable(ruleRemarks.get(PersonRemark.GENDER));

        maybeAgeRemark.ifPresent(ruleRemarkWrapper -> person.setAge(randomInRange(ruleRemarkWrapper)));
        maybeWeightRemark.ifPresent(ruleRemarkWrapper -> person.setWeight(randomInRange(ruleRemarkWrapper)));
        maybeGrowthRemark.ifPresent(ruleRemarkWrapper -> person.setGrowth(randomInRange(ruleRemarkWrapper)));
        maybeGenderRemark.ifPresent(ruleRemarkWrapper -> person.setGender(Gender.valueOf(ruleRemarkWrapper.getArgs()[0])));

        return person;
    }

    private static int randomInRange(ICustomRuleRemark ruleRemarkWrapper) {
        String[] range = ruleRemarkWrapper.getArgs();
        return RandomUtils.nextInt(range[0], range[1]);
    }

    @Override
    public void setRuleRemarks(Map<ICustomRuleRemark, ICustomRuleRemark> ruleRemarks) {
        this.ruleRemarks = ruleRemarks;
    }
}
