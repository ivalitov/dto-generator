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

/**
 * Put on the floating point field to generate random floating point value.
 * <p>
 * Supported field types:
 * <ul>
 *     <li>{@link Double} and primitive {@code double}</li>
 *     <li>{@link Float} and primitive {@code float}</li>
 *     <li>{@link BigDecimal}</li>
 * </ul>
 * You may specify number digits ofter dot with {@link DecimalRule#precision()}
 * and values range depended on field's type.
 *
 * @see Boundary
 * @see Group
 */

@Retention(RUNTIME)
@Target(FIELD)
@Rule
@Repeatable(DecimalRules.class)
public @interface DecimalRule {

    Class<?>[] GENERATED_TYPES = new Class<?>[]{Double.class, Float.class, BigDecimal.class};

    /**
     * Specifies number of digits after dot.
     * <p>
     * Please notice, that depending on the restrictions of floating point types,
     * the specified precision might not be achieved.
     *
     * @return number of digits after dot
     */
    int precision() default 2;

    /**
     * Default value equals to: 200,147,483,647
     *
     * @return maximum value of generated double
     */
    double maxDouble() default Bounds.DOUBLE_MAX_VALUE;

    /**
     * Default value equals to: -200,147,483,647
     *
     * @return minimum value of generated double
     */
    double minDouble() default Bounds.DOUBLE_MIN_VALUE;

    /**
     * Default value equals to: 50,000
     *
     * @return maximum value of generated float
     */
    float maxFloat() default Bounds.FLOAT_MAX_VALUE;

    /**
     * Default value equals to: -50,000
     *
     * @return minimum value of generated float
     */
    float minFloat() default Bounds.FLOAT_MIN_VALUE;

    /**
     * Default value equals to max value of long type: 9,223,372,036,854,775,807
     *
     * @return maximum value of generated big decimal
     */
    String maxBigDecimal() default Bounds.BIG_DECIMAL_MAX_VALUE;

    /**
     * Default value equals to max value of long type but negative -9,223,372,036,854,775,807
     *
     * @return minimum value of generated big decimal
     */
    String minBigDecimal() default Bounds.BIG_DECIMAL_MIN_VALUE;

    /**
     * @return boundary parameter
     * @see Boundary
     */
    Boundary boundary() default Boundary.RANDOM_VALUE;

    /**
     * @return group of the generators
     * @see Group
     */
    String group() default Group.DEFAULT;

}