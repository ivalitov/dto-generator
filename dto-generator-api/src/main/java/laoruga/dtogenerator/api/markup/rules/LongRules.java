package laoruga.dtogenerator.api.markup.rules;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Retention(RUNTIME)
@Target(FIELD)
public @interface LongRules {

    long DEFAULT_MIN = 0L;
    long DEFAULT_MAX = 999999999999999999L;

    long maxValue() default 999999999999999999L;

    long minValue() default 0L;
}
