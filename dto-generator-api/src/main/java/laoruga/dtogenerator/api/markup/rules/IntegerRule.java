package laoruga.dtogenerator.api.markup.rules;

import laoruga.dtogenerator.api.constants.Group;
import laoruga.dtogenerator.api.markup.BoundType;
import laoruga.dtogenerator.api.constants.BasicRuleRemark;
import laoruga.dtogenerator.api.markup.remarks.IRuleRemark;

import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static laoruga.dtogenerator.api.constants.BasicRuleRemark.RANDOM_VALUE;
import static laoruga.dtogenerator.api.constants.Group.DEFAULT;

@Retention(RUNTIME)
@Target(FIELD)
@Rule
@Repeatable(IntegerRules.class)
public @interface IntegerRule {

    int DEFAULT_MIN = 0;
    int DEFAULT_MAX = 999999999;
    IRuleRemark DEFAULT_RULE_REMARK = RANDOM_VALUE;
    Class<?>[] APPLICABLE_TYPES = {Integer.class, Integer.TYPE};

    @BoundType(BasicRuleRemark.MAX_VALUE)
    int maxValue() default DEFAULT_MAX;

    @BoundType(BasicRuleRemark.MIN_VALUE)
    int minValue() default DEFAULT_MIN;

    BasicRuleRemark ruleRemark() default RANDOM_VALUE;

    Group group() default DEFAULT;
}
