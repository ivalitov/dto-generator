package org.laoruga.dtogenerator.api.rules;

import org.laoruga.dtogenerator.api.remarks.IRuleRemark;
import org.laoruga.dtogenerator.api.rules.meta.Rule;
import org.laoruga.dtogenerator.constants.BasicRuleRemark;
import org.laoruga.dtogenerator.constants.Group;

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

    IRuleRemark DEFAULT_RULE_REMARK = BasicRuleRemark.RANDOM_VALUE;
    Class<?>[] APPLICABLE_TYPES = {Enum.class};

    String[] possibleEnumNames();

    Class<? extends Enum<?>> enumClass();

    BasicRuleRemark ruleRemark() default BasicRuleRemark.RANDOM_VALUE;

    String group() default Group.DEFAULT;
}
