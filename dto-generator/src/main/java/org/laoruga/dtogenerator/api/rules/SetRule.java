package org.laoruga.dtogenerator.api.rules;

import org.laoruga.dtogenerator.api.BoundType;
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

    Class<? extends Set> setClass() default HashSet.class;

    @BoundType(BasicRuleRemark.MAX_VALUE)
    int maxSize() default 10;

    @BoundType(BasicRuleRemark.MIN_VALUE)
    int minSize() default 1;

    BasicRuleRemark ruleRemark() default BasicRuleRemark.RANDOM_VALUE;

    String group() default Group.DEFAULT;

    Class<?> generatedType() default Set.class;
}
