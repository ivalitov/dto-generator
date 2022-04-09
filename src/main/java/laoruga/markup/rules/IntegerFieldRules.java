package laoruga.markup.rules;

import laoruga.markup.BoundType;
import laoruga.markup.Marker;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Retention(RUNTIME)
@Target(FIELD)
public @interface IntegerFieldRules {

    @BoundType(Marker.MAX_VALUE)
    int maxValue() default 999999999;

    @BoundType(Marker.MIN_VALUE)
    int minValue() default 0;
}
