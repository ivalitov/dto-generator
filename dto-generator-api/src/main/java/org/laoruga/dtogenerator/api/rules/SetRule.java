package org.laoruga.dtogenerator.api.rules;

import org.laoruga.dtogenerator.api.BoundType;
import org.laoruga.dtogenerator.api.remarks.IRuleRemark;
import org.laoruga.dtogenerator.api.rules.meta.RuleForCollection;
import org.laoruga.dtogenerator.constants.BasicRuleRemark;
import org.laoruga.dtogenerator.constants.Group;

import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.util.HashSet;
import java.util.Set;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Retention(RUNTIME)
@Target(FIELD)
@RuleForCollection
@Repeatable(SetRules.class)
public @interface SetRule {

    int DEFAULT_MIN_SIZE = 1;
    int DEFAULT_MAX_SIZE = 10;
    IRuleRemark DEFAULT_RULE_REMARK = BasicRuleRemark.RANDOM_VALUE;
    Class<?>[] APPLICABLE_TYPES = {Set.class};

    Class<? extends Set> setClass() default HashSet.class;

    @BoundType(BasicRuleRemark.MAX_VALUE)
    int maxSize() default DEFAULT_MAX_SIZE;

    @BoundType(BasicRuleRemark.MIN_VALUE)
    int minSize() default DEFAULT_MIN_SIZE;

    BasicRuleRemark ruleRemark() default BasicRuleRemark.RANDOM_VALUE;

    String group() default Group.DEFAULT;
}
