package org.laoruga.dtogenerator.api.rules.datetime;

import org.laoruga.dtogenerator.api.rules.meta.Rule;
import org.laoruga.dtogenerator.constants.Boundary;
import org.laoruga.dtogenerator.constants.Group;

import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.Temporal;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Retention(RUNTIME)
@Target(FIELD)
@Rule
@Repeatable(DateTimeRules.class)
public @interface DateTimeRule {

    /**
     * Types that implement {@link Temporal} interface
     * and have a static instantiation method {@code now()} are supported.
     * For example, some popular types:
     * <ul>
     * <li>{@link LocalDateTime}
     * <li>{@link LocalDate}
     * <li>{@link LocalTime}
     * <li>{@link Instant}
     * <li>and others
     * </ul>
     */
    Class<?> GENERATED_TYPE = Temporal.class;

    ChronoUnitShift[] chronoUnitShift() default {};

    ChronoFieldShift[] chronoFieldShift() default {};

    Boundary boundary() default Boundary.RANDOM_VALUE;

    String group() default Group.DEFAULT;

}
