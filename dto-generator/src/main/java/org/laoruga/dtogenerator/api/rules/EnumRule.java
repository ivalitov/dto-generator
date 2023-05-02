package org.laoruga.dtogenerator.api.rules;

import org.laoruga.dtogenerator.api.rules.meta.Rule;
import org.laoruga.dtogenerator.constants.Boundary;
import org.laoruga.dtogenerator.constants.Group;

import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Put on the enum field to select random enum constant.
 *
 * @see Boundary
 * @see Group
 */

@Retention(RUNTIME)
@Target(FIELD)
@Rule
@Repeatable(EnumRules.class)
public @interface EnumRule {

    Class<?> GENERATED_TYPE = Enum.class;

    /**
     * You may specify enum constant names, from which one will be chosen randomly.
     * If not defined, a random constant will be chosen from all enum values.
     *
     * @return enum constant names to select from
     */
    String[] possibleEnumNames() default {};

    /**
     * Meaning of the boundary params:
     * <pre>
     * {@link Boundary#MIN_VALUE} - random value having minimal length
     * {@link Boundary#MAX_VALUE} - random value having maximal length
     * {@link Boundary#RANDOM_VALUE} - random value
     * {@link Boundary#NULL_VALUE} - null value
     * </pre>
     *
     * @return boundary parameter
     **/
    Boundary boundary() default Boundary.RANDOM_VALUE;

    /**
     * @return group of the generators
     * @see Group
     */
    String group() default Group.DEFAULT;

}
