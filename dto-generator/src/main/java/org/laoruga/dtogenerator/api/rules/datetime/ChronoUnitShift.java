package org.laoruga.dtogenerator.api.rules.datetime;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.time.temporal.ChronoUnit;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * @author Il'dar Valitov
 * Created on 15.03.2023
 */
@Retention(RUNTIME)
@Target({})
public @interface ChronoUnitShift {

    long shift() default 0;

    long leftBound() default -365;

    long rightBound() default 365;

    ChronoUnit unit();
}
