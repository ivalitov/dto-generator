package laoruga.markup.rules;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Retention(RUNTIME)
@Target(FIELD)
public @interface DecimalFieldRules {

    double maxValue() default 999999999999999999D;

    double minValue() default 0D;

    int precision() default 2;

}
