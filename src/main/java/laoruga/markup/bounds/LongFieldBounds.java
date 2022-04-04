package laoruga.markup.bounds;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Retention(RUNTIME)
@Target(FIELD)
public @interface LongFieldBounds {

    long maxValue() default 999999999999999999L;

    long minValue() default 0L;
}
