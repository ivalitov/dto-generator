package laoruga.custom;

import laoruga.ChField;
import laoruga.markup.CustomRules;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@CustomRules
@Retention(RUNTIME)
@Target(FIELD)
public @interface ArrearsBusinessRule {

    int arrearsCount();

}
