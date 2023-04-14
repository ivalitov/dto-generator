package org.laoruga.dtogenerator.api.rules;

import org.laoruga.dtogenerator.api.rules.meta.Rule;
import org.laoruga.dtogenerator.constants.BoundaryConfig;
import org.laoruga.dtogenerator.constants.Bounds;
import org.laoruga.dtogenerator.constants.Group;

import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.math.BigInteger;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.TYPE_USE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Retention(RUNTIME)
@Target({FIELD, TYPE_USE})
@Rule
@Repeatable(NumberRules.class)
public @interface NumberRule {

    Class<?>[] GENERATED_TYPES = new Class<?>[]
            {Byte.class, Short.class, Integer.class, Long.class, BigInteger.class, AtomicInteger.class, AtomicLong.class};

    long maxLong() default Long.MAX_VALUE;

    long minLong() default Long.MIN_VALUE;

    int maxInt() default Integer.MAX_VALUE;

    int minInt() default Integer.MIN_VALUE;

    short maxShort() default Short.MAX_VALUE;

    short minShort() default Short.MIN_VALUE;

    byte maxByte() default Byte.MAX_VALUE;

    byte minByte() default Byte.MIN_VALUE;

    String maxBigInt() default Bounds.BIG_INTEGER_MAX_VALUE;

    String minBigInt() default Bounds.BIG_INTEGER_MIN_VALUE;

    BoundaryConfig ruleRemark() default BoundaryConfig.RANDOM_VALUE;

    String group() default Group.DEFAULT;

}
