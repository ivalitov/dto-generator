package laoruga.dtogenerator.api.markup.rules;

import laoruga.dtogenerator.api.markup.BoundType;
import laoruga.dtogenerator.api.markup.remarks.BasicRuleRemark;
import laoruga.dtogenerator.api.markup.remarks.IRuleRemark;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.util.ArrayList;
import java.util.List;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static laoruga.dtogenerator.api.markup.remarks.BasicRuleRemark.RANDOM_VALUE;

@Retention(RUNTIME)
@Target(FIELD)
@Rule
@RuleForCollection
public @interface ListRules {

    int DEFAULT_MIN_SIZE = 1;
    int DEFAULT_MAX_SIZE = 10;
    IRuleRemark DEFAULT_RULE_REMARK = RANDOM_VALUE;

    Class<? extends List> listClass() default ArrayList.class;

    @BoundType(BasicRuleRemark.MAX_VALUE)
    int maxSize() default DEFAULT_MAX_SIZE;

    @BoundType(BasicRuleRemark.MIN_VALUE)
    int minSize() default DEFAULT_MIN_SIZE;

    BasicRuleRemark ruleRemark() default RANDOM_VALUE;
}
