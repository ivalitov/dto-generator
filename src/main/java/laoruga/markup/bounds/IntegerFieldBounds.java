package laoruga.markup.bounds;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Retention(RUNTIME)
@Target(FIELD)
public @interface IntegerFieldBounds {

    int maxValue() default 999999999;

    int minValue() default 0;
}
