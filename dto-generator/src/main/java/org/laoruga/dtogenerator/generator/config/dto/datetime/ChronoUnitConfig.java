package org.laoruga.dtogenerator.generator.config.dto.datetime;

import org.laoruga.dtogenerator.api.remarks.IRuleRemark;
import org.laoruga.dtogenerator.constants.RuleRemark;

import java.time.temporal.Temporal;
import java.time.temporal.TemporalUnit;

/**
 * @author Il'dar Valitov
 * Created on 15.03.2023
 */
public class ChronoUnitConfig implements ChronoConfig {
    private final long shift;
    private final long leftBound;
    private final long rightBound;
    private final TemporalUnit unit;

    ChronoUnitConfig(long shift, long leftBound, long rightBound, TemporalUnit unit) {
        this.shift = shift;
        this.leftBound = leftBound;
        this.rightBound = rightBound;
        this.unit = unit;
    }

    public static ChronoUnitConfig newAbsolute(long shift, TemporalUnit unit) {
        return new ChronoUnitConfig(shift, 0L, 0L, unit);
    }

    public static ChronoUnitConfig newBounds(long leftBound, long rightBound, TemporalUnit unit) {
        return new ChronoUnitConfig(0, leftBound, rightBound, unit);
    }

    @Override
    public Temporal adjust(Temporal temporal, IRuleRemark ruleRemark) {
        if (shift != 0) {
            return temporal.plus(shift, unit);
        }
        long shiftValue = selectShift((RuleRemark) ruleRemark, leftBound, rightBound);
        return temporal.plus(shiftValue, unit);
    }

}
