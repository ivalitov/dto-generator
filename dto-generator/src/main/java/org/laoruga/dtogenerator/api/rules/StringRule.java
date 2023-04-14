package org.laoruga.dtogenerator.api.rules;

import org.laoruga.dtogenerator.api.rules.meta.Rule;
import org.laoruga.dtogenerator.constants.Boundary;
import org.laoruga.dtogenerator.constants.CharSet;
import org.laoruga.dtogenerator.constants.Group;

import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Retention(RUNTIME)
@Target(FIELD)
@Rule
@Repeatable(StringRules.class)
public @interface StringRule {

    Class<?> GENERATED_TYPE = String.class;

    String DEFAULT_REGEXP = "";

    int minLength() default 0;

    int maxLength() default 1000;

    String[] words() default {};

    String chars() default CharSet.DEFAULT_CHARSET;

    Boundary ruleRemark() default Boundary.RANDOM_VALUE;

    String group() default Group.DEFAULT;

    String regexp() default DEFAULT_REGEXP;

}
