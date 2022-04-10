package dtogenerator.api.markup.rules;

import dtogenerator.api.markup.BoundType;
import dtogenerator.api.markup.remarks.RuleRemark;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Retention(RUNTIME)
@Target(FIELD)
public @interface IntegerRules {

    @BoundType(RuleRemark.MAX_VALUE)
    int maxValue() default 999999999;

    @BoundType(RuleRemark.MIN_VALUE)
    int minValue() default 0;
}
