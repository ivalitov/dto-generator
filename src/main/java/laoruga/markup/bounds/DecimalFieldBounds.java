package laoruga.markup.bounds;

import laoruga.CharSet;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface DecimalFieldBounds {

    long maxSymbols() default 999999999999999999L;
    long minSymbols() default 0L;
    int symbolsAfterDot() default 2;

}
