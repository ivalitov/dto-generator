package org.laoruga.dtogenerator.api.rules.datetime;

import java.time.temporal.ChronoField;

/**
 * @author Il'dar Valitov
 * Created on 15.03.2023
 */
public @interface ChronoFieldShift {

    int shift() default 0;

    int leftBound() default -7;

    int rightBound() default 7;

    ChronoField unit();
}
