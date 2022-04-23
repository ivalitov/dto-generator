package laoruga.dtogenerator.api.markup.rules;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Retention(RUNTIME)
@Target(FIELD)
public @interface DoubleRules {

    double DEFAULT_MIN = 0D;
    double DEFAULT_MAX = 999999999999999999D;

    double maxValue() default DEFAULT_MAX;

    double minValue() default DEFAULT_MIN;

    int precision() default 2;

}
