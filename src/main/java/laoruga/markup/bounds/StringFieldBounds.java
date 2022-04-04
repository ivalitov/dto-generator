package laoruga.markup.bounds;

import laoruga.CharSet;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Retention(RUNTIME)
@Target(FIELD)
public @interface StringFieldBounds {

    int maxSymbols() default 999999999;

    int minSymbols() default 0;

    CharSet[] charset() default {CharSet.NUM, CharSet.ENG, CharSet.RUS};

}
