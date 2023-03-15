package org.laoruga.dtogenerator.api.rules.datetime;

import org.laoruga.dtogenerator.api.rules.meta.Rule;
import org.laoruga.dtogenerator.constants.Group;
import org.laoruga.dtogenerator.constants.RuleRemark;

import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.time.*;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Retention(RUNTIME)
@Target(FIELD)
@Rule
@Repeatable(DateTimeRules.class)
public @interface DateTimeRule {

    Class<?>[] GENERATED_TYPES = new Class<?>[]
            {LocalDateTime.class, LocalDate.class, LocalTime.class, Year.class, YearMonth.class, Instant.class};

    ChronoUnitShift[] chronoUnitShift() default {};

    ChronoFieldShift[] chronoFieldShift() default {};

    RuleRemark ruleRemark() default RuleRemark.RANDOM_VALUE;

    String group() default Group.DEFAULT;

}
