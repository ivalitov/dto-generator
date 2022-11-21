package org.laoruga.dtogenerator.generators.basictypegenerators;

import lombok.AllArgsConstructor;
import org.laoruga.dtogenerator.api.generators.IGenerator;
import org.laoruga.dtogenerator.api.remarks.IRuleRemark;
import org.laoruga.dtogenerator.constants.BasicRuleRemark;
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
        if (ruleRemark == BasicRuleRemark.MIN_VALUE) {
            return now.minusDays(leftShiftDays);
        }
        if (ruleRemark == BasicRuleRemark.MAX_VALUE) {
            return now.plusDays(rightShiftDays);
        }
        if (ruleRemark == BasicRuleRemark.RANDOM_VALUE) {
            int randomInt = RandomUtils.getRandom().nextInt(leftShiftDays + rightShiftDays + 1);
            LocalDateTime minDate = now.minusDays(leftShiftDays);
            return minDate.plusDays(randomInt);
        }
        if (ruleRemark == BasicRuleRemark.NULL_VALUE) {
            return null;
        }
        throw new IllegalStateException("Unexpected value " + ruleRemark);
    }

    public static LocalDateTimeGeneratorBuilder builder() {
        return new LocalDateTimeGeneratorBuilder();
    }

    public static final class LocalDateTimeGeneratorBuilder {
        private int leftShiftDays;
        private int rightShiftDays;
        private IRuleRemark ruleRemark;

        private LocalDateTimeGeneratorBuilder() {
        }

        public LocalDateTimeGeneratorBuilder leftShiftDays(int leftShiftDays) {
            this.leftShiftDays = leftShiftDays;
            return this;
        }

        public LocalDateTimeGeneratorBuilder rightShiftDays(int rightShiftDays) {
            this.rightShiftDays = rightShiftDays;
            return this;
        }

        public LocalDateTimeGeneratorBuilder ruleRemark(IRuleRemark ruleRemark) {
            this.ruleRemark = ruleRemark;
            return this;
        }

        public LocalDateTimeGenerator build() {
            return new LocalDateTimeGenerator(leftShiftDays, rightShiftDays, ruleRemark);
        }
    }
}
