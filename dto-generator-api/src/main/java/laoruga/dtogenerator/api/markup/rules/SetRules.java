package laoruga.dtogenerator.api.markup.rules;

import laoruga.dtogenerator.api.markup.rules.meta.Rules;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Retention(RUNTIME)
@Target(FIELD)
@Rules
public @interface SetRules {

    SetRule[] value();
}
