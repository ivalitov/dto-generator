package org.laoruga.dtogenerator.api.rules;

import org.laoruga.dtogenerator.api.rules.meta.Rule;
import org.laoruga.dtogenerator.constants.Boundary;
import org.laoruga.dtogenerator.constants.Bounds;
import org.laoruga.dtogenerator.constants.Group;

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

    double maxDouble() default Bounds.DOUBLE_MAX_VALUE;

    double minDouble() default Bounds.DOUBLE_MIN_VALUE;

    float maxFloat() default Bounds.FLOAT_MAX_VALUE;

    float minFloat() default Bounds.FLOAT_MIN_VALUE;

    String maxBigDecimal() default Bounds.BIG_DECIMAL_MAX_VALUE;

    String minBigDecimal() default Bounds.BIG_DECIMAL_MIN_VALUE;

    Boundary boundary() default Boundary.RANDOM_VALUE;

    String group() default Group.DEFAULT;

}