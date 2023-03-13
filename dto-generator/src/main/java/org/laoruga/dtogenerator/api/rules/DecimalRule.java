package org.laoruga.dtogenerator.api.rules;

import org.laoruga.dtogenerator.api.rules.meta.Rule;
import org.laoruga.dtogenerator.constants.Bounds;
import org.laoruga.dtogenerator.constants.Group;
import org.laoruga.dtogenerator.constants.RuleRemark;

import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.math.BigDecimal;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Retention(RUNTIME)
@Target(FIELD)
@Rule
@Repeatable(DecimalRules.class)
public @interface DecimalRule {

    Class<?>[] GENERATED_TYPES = new Class<?>[]{Double.class, Float.class, BigDecimal.class};

    int precision() default 2;

    double maxDouble() default Double.MAX_VALUE;

    double minDouble() default Double.MIN_VALUE;

    float maxFloat() default Float.MAX_VALUE;

    float minFloat() default Float.MIN_VALUE;

    String maxBigDecimal() default Bounds.BIG_DECIMAL_MAX_VALUE;

    String minBigDecimal() default Bounds.BIG_DECIMAL_MIN_VALUE;

    RuleRemark ruleRemark() default RuleRemark.RANDOM_VALUE;

    String group() default Group.DEFAULT;

}