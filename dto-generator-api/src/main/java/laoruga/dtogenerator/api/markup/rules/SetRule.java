package laoruga.dtogenerator.api.markup.rules;

import laoruga.dtogenerator.api.constants.BasicRuleRemark;
import laoruga.dtogenerator.api.markup.BoundType;
import laoruga.dtogenerator.api.markup.remarks.IRuleRemark;
import laoruga.dtogenerator.api.markup.rules.meta.RuleForCollection;

import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static laoruga.dtogenerator.api.constants.BasicRuleRemark.RANDOM_VALUE;
import static laoruga.dtogenerator.api.constants.Group.DEFAULT;

@Retention(RUNTIME)
@Target(FIELD)
@RuleForCollection
@Repeatable(SetRules.class)
public @interface SetRule {

    int DEFAULT_MIN_SIZE = 1;
    int DEFAULT_MAX_SIZE = 10;
    IRuleRemark DEFAULT_RULE_REMARK = RANDOM_VALUE;
    Class<?>[] APPLICABLE_TYPES = {Set.class};

    Class<? extends Set> setClass() default HashSet.class;

    @BoundType(BasicRuleRemark.MAX_VALUE)
    int maxSize() default DEFAULT_MAX_SIZE;

    @BoundType(BasicRuleRemark.MIN_VALUE)
    int minSize() default DEFAULT_MIN_SIZE;

    BasicRuleRemark ruleRemark() default RANDOM_VALUE;

    String group() default DEFAULT;
}
