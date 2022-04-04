package laoruga.markup.bounds;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Retention(RUNTIME)
@Target(FIELD)
public @interface LocalDateTimeFieldBounds {

    int leftShiftDays() default 365 * 5;

    int rightShiftDays() default 365 * 5;

}
