package org.laoruga.dtogenerator.api.rules;

import org.laoruga.dtogenerator.api.rules.meta.Rule;
import org.laoruga.dtogenerator.constants.Group;
import org.laoruga.dtogenerator.constants.RuleRemark;

import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Retention(RUNTIME)
@Target(FIELD)
@Rule
@Repeatable(EnumRules.class)
public @interface EnumRule {

    // All names are used by default
    String[] possibleEnumNames() default {};

    RuleRemark ruleRemark() default RuleRemark.RANDOM_VALUE;

    String group() default Group.DEFAULT;

    Class<? extends Enum> generatedType() default Enum.class;
}
