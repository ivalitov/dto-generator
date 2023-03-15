package org.laoruga.dtogenerator.api.rules.datetime;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.time.temporal.ChronoUnit;

import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static org.laoruga.dtogenerator.constants.Bounds.CHRONO_UNIT_SHIFT_LEFT;
import static org.laoruga.dtogenerator.constants.Bounds.CHRONO_UNIT_SHIFT_RIGHT;

/**
 * @author Il'dar Valitov
 * Created on 15.03.2023
 */
@Retention(RUNTIME)
@Target({})
public @interface ChronoUnitShift {

    long shift() default 0;

    long leftBound() default CHRONO_UNIT_SHIFT_LEFT;

    long rightBound() default CHRONO_UNIT_SHIFT_RIGHT;

    ChronoUnit unit();
}
