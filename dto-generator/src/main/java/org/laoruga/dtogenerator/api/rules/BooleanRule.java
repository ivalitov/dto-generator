package org.laoruga.dtogenerator.api.rules;

import org.laoruga.dtogenerator.api.rules.meta.Rule;
import org.laoruga.dtogenerator.constants.Boundary;
import org.laoruga.dtogenerator.constants.Group;

import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.TYPE_USE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Put on the boolean field to generate random boolean value.
 *
 * @see Boundary
 * @see Group
 */

@Retention(RUNTIME)
@Target({FIELD, TYPE_USE})
@Rule
@Repeatable(BooleanRules.class)
public @interface BooleanRule {

    Class<?> GENERATED_TYPE = Boolean.class;

    /**
     * Specify probability of getting true.
     *
     * @return number ranging from 0 to 1;
     * 1 - always true;
     * 0 - always false
     */
    double trueProbability() default 0.5D;

    /**
     * Meaning of the boundary params:
     * <pre>
     * {@link Boundary#MIN_VALUE} - always true
     * {@link Boundary#MAX_VALUE} - always false
     * {@link Boundary#RANDOM_VALUE} - depended on {@link BooleanRule#trueProbability()}
     * {@link Boundary#NULL_VALUE} - null value or false if generated field is primitive
     * </pre>
     *
     * @return boundary parameter
     */
    Boundary boundary() default Boundary.RANDOM_VALUE;

    /**
     * @return group of the generators
     * @see Group
     */
    String group() default Group.DEFAULT;

}
