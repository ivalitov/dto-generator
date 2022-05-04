package laoruga.dtogenerator.api.markup.rules;

import laoruga.dtogenerator.api.constants.CharSet;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Retention(RUNTIME)
@Target(FIELD)
@Rule
public @interface StringRules {

    int maxSymbols() default 1000;

    int minSymbols() default 0;

    CharSet[] charset() default {CharSet.NUM, CharSet.ENG, CharSet.RUS};

    String chars() default "";

}
