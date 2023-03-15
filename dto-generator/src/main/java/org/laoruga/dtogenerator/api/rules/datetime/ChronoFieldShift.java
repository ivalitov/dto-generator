package org.laoruga.dtogenerator.api.rules.datetime;

import java.time.temporal.ChronoField;

import static org.laoruga.dtogenerator.constants.Bounds.CHRONO_FIELD_SHIFT_LEFT;
import static org.laoruga.dtogenerator.constants.Bounds.CHRONO_FIELD_SHIFT_RIGHT;

/**
 * @author Il'dar Valitov
 * Created on 15.03.2023
 */
public @interface ChronoFieldShift {

    int shift() default 0;

    long leftBound() default CHRONO_FIELD_SHIFT_LEFT;

    long rightBound() default CHRONO_FIELD_SHIFT_RIGHT;

    ChronoField unit();
}
