package org.laoruga.dtogenerator.generator.config.dto.datetime;

import org.laoruga.dtogenerator.api.RuleRemark;
import org.laoruga.dtogenerator.constants.BoundaryConfig;
import org.laoruga.dtogenerator.util.RandomUtils;

import java.time.temporal.Temporal;

/**
 * @author Il'dar Valitov
 * Created on 15.03.2023
 */
public interface ChronoConfig {
    Temporal adjust(Temporal temporal, RuleRemark ruleRemark);

    default long selectShift(BoundaryConfig boundaryValue, long leftBound, long rightBound) {
        switch (boundaryValue) {
            case MIN_VALUE:
                return leftBound;
            case MAX_VALUE:
                return rightBound;
            case RANDOM_VALUE:
            case NOT_DEFINED:
                return RandomUtils.nextLong(leftBound, rightBound);
            default:
                throw new IllegalStateException("Unexpected value " + boundaryValue);
        }
    }
}
