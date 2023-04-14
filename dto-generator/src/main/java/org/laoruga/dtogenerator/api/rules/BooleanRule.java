package org.laoruga.dtogenerator.api.rules;

import org.laoruga.dtogenerator.api.rules.meta.Rule;
import org.laoruga.dtogenerator.constants.BoundaryConfig;
import org.laoruga.dtogenerator.constants.Group;

import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.TYPE_USE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Retention(RUNTIME)
@Target({FIELD, TYPE_USE})
@Rule
@Repeatable(BooleanRules.class)
public @interface BooleanRule {

    Class<?> GENERATED_TYPE = Boolean.class;

    double trueProbability() default 0.5D;

    BoundaryConfig ruleRemark() default BoundaryConfig.RANDOM_VALUE;

    String group() default Group.DEFAULT;

}
