package org.laoruga.dtogenerator.api.rules.datetime;

import org.laoruga.dtogenerator.api.rules.meta.Rules;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Retention(RUNTIME)
@Target(FIELD)
@Rules
public @interface DateTimeRules {

    DateTimeRule[] value();
}
