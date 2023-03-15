package org.laoruga.dtogenerator.generator.configs.datetime;

import org.laoruga.dtogenerator.api.remarks.IRuleRemark;
import org.laoruga.dtogenerator.constants.RuleRemark;
import org.laoruga.dtogenerator.util.RandomUtils;

import java.time.temporal.Temporal;
import java.time.temporal.TemporalField;

/**
 * @author Il'dar Valitov
 * Created on 15.03.2023
 */
public class ChronoFieldConfig implements ChronoConfig {

    private final long value;
    private final long leftBound;
    private final long rightBound;
    private final TemporalField field;

    ChronoFieldConfig(long value, long leftBound, long rightBound, TemporalField field) {
        this.value = value;
        this.leftBound = leftBound;
        this.rightBound = rightBound;
        this.field = field;
    }

    public static ChronoFieldConfig newAbsolute(long value, TemporalField unit) {
        return new ChronoFieldConfig(value, 0L, 0L, unit);
    }

    public static ChronoFieldConfig newBounds(long leftBound, long rightBound, TemporalField unit) {
        return new ChronoFieldConfig(0, leftBound, rightBound, unit);
    }

    @Override
    public Temporal adjust(Temporal temporal, IRuleRemark ruleRemark) {
        if (value != 0) {
            return temporal.with(field, value);
        }
        long shiftValue;
        if (ruleRemark == RuleRemark.MIN_VALUE) {
            shiftValue = leftBound;
        } else if (ruleRemark == RuleRemark.MAX_VALUE) {
            shiftValue = rightBound;
        } else if (ruleRemark == RuleRemark.RANDOM_VALUE) {
            shiftValue = RandomUtils.nextLong(leftBound, rightBound);
        } else {
            throw new IllegalStateException("Unexpected value " + ruleRemark);
        }
        return temporal.with(field, shiftValue);
    }
}
