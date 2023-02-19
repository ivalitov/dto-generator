package org.laoruga.dtogenerator.generator;

import lombok.AllArgsConstructor;
import org.laoruga.dtogenerator.api.generators.IGenerator;
import org.laoruga.dtogenerator.api.remarks.IRuleRemark;
import org.laoruga.dtogenerator.constants.RuleRemark;
import org.laoruga.dtogenerator.generator.builder.builders.LocalDateTimeGeneratorBuilder;
import org.laoruga.dtogenerator.util.RandomUtils;

import java.time.LocalDateTime;

/**
 * @author Il'dar Valitov
 * Created on 19.05.2022
 */

@AllArgsConstructor
public class LocalDateTimeGenerator implements IGenerator<LocalDateTime> {

    private final int leftShiftDays;
    private final int rightShiftDays;
    private final IRuleRemark ruleRemark;

    @Override
    public LocalDateTime generate() {
        LocalDateTime now = LocalDateTime.now();
        if (ruleRemark == RuleRemark.MIN_VALUE) {
            return now.minusDays(leftShiftDays);
        }
        if (ruleRemark == RuleRemark.MAX_VALUE) {
            return now.plusDays(rightShiftDays);
        }
        if (ruleRemark == RuleRemark.RANDOM_VALUE) {
            int randomInt = RandomUtils.getRandom().nextInt(leftShiftDays + rightShiftDays + 1);
            LocalDateTime minDate = now.minusDays(leftShiftDays);
            return minDate.plusDays(randomInt);
        }
        if (ruleRemark == RuleRemark.NULL_VALUE) {
            return null;
        }
        throw new IllegalStateException("Unexpected value " + ruleRemark);
    }

    public static LocalDateTimeGeneratorBuilder builder() {
        return new LocalDateTimeGeneratorBuilder();
    }

}
