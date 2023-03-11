package org.laoruga.dtogenerator.api.rules;

import org.laoruga.dtogenerator.api.rules.meta.Rule;
import org.laoruga.dtogenerator.constants.Group;
import org.laoruga.dtogenerator.constants.RuleRemark;

import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.TYPE_USE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Retention(RUNTIME)
@Target({FIELD, TYPE_USE})
@Rule
@Repeatable(NumberRules.class)
public @interface NumberRule {

    Class<?>[] GENERATED_TYPES = new Class<?>[]{Byte.class, Short.class, Integer.class, Long.class};

    long maxLong() default Long.MAX_VALUE;

    long minLong() default Long.MIN_VALUE;

    int maxInt() default Integer.MAX_VALUE;

    int minInt() default Integer.MIN_VALUE;

    short maxShort() default Short.MAX_VALUE;

    short minShort() default Short.MIN_VALUE;

    byte maxByte() default Byte.MAX_VALUE;

    byte minByte() default Byte.MIN_VALUE;

    RuleRemark ruleRemark() default RuleRemark.RANDOM_VALUE;

    String group() default Group.DEFAULT;

}
