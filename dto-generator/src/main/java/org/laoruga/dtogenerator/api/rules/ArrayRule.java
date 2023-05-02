package org.laoruga.dtogenerator.api.rules;

import org.laoruga.dtogenerator.api.rules.meta.Rule;
import org.laoruga.dtogenerator.constants.Boundary;
import org.laoruga.dtogenerator.constants.Group;
import org.laoruga.dtogenerator.constants.RuleType;

import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Put on the array field to generate random array containing random data.
 * <p>
 * DtoGenerator will initiate new array of length
 * between {@link ArrayRule#minLength()} and {@link ArrayRule#maxLength()} or null value
 * depends on selected {@link ArrayRule#boundary()}.
 * <p>
 * Array's elements will be generated according to the rule specified in {@link ArrayRule#element()}
 * or using user's generator if it has been defined to the array's element type.
 *
 * @see Entry
 * @see Boundary
 * @see Group
 */

@Retention(RUNTIME)
@Target(FIELD)
@Rule(RuleType.ARRAY)
@Repeatable(ArrayRules.class)
public @interface ArrayRule {

    Class<?>[] GENERATED_TYPES = new Class<?>[]
            {Object[].class, byte[].class, short[].class, char[].class, int[].class, long[].class, boolean[].class};

    /**
     * You may specify the generation {@link Rule} for array's elements generation.
     * If not defined array's elements will be generated if one of the following conditions met:
     * <ul>
     *     <li>array's type corresponds to one of the rules listed in {@link Entry}</li>
     *     <li>there is user's generator exists for array's element type</li>
     * </ul>
     * Otherwise, exception will be thrown.
     *
     * @return {@link Entry} containing rule for generating array's elements
     * @see Entry
     */
    Entry element() default @Entry;

    /**
     * @return maximum length of generating array
     */
    int maxLength() default 10;

    /**
     * @return minimum length of generating array
     */
    int minLength() default 1;

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
