package laoruga.markup.bounds;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface IntFieldBounds {

    long maxSymbols() default 999999999999999999L;
    long minSymbols() default 0L;
}
