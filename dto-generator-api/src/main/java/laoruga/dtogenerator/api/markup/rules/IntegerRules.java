package laoruga.dtogenerator.api.markup.rules;

import laoruga.dtogenerator.api.markup.BoundType;
import laoruga.dtogenerator.api.markup.remarks.RuleRemark;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Retention(RUNTIME)
@Target(FIELD)
@Rule
public @interface IntegerRules {

    int DEFAULT_MIN = 0;
    int DEFAULT_MAX = 999999999;

    @BoundType(RuleRemark.MAX_VALUE)
    int maxValue() default DEFAULT_MAX;

    @BoundType(RuleRemark.MIN_VALUE)
    int minValue() default DEFAULT_MIN;
}
