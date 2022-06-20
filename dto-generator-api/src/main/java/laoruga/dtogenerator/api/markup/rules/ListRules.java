package laoruga.dtogenerator.api.markup.rules;

import laoruga.dtogenerator.api.markup.rules.meta.RulesForCollection;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Retention(RUNTIME)
@Target(FIELD)
@RulesForCollection
public @interface ListRules {

    ListRule[] value();
}
