package org.laoruga.dtogenerator.api.rules.meta;

import org.laoruga.dtogenerator.constants.RuleType;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Retention(RUNTIME)
@Target(ANNOTATION_TYPE)
@Rule
public @interface Rule {

    RuleType value() default RuleType.BASIC;
}
