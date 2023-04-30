package org.laoruga.dtogenerator.api.rules;

import org.laoruga.dtogenerator.api.rules.meta.Rule;
import org.laoruga.dtogenerator.constants.Boundary;
import org.laoruga.dtogenerator.constants.CharSet;
import org.laoruga.dtogenerator.constants.Group;

import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;


/**
 * Put on the string field to generate random string value using one of the approaches:
 * <ul>
 *     <li>1. selecting random word from specified array of words.
 *     To do that, set words array through {@link StringRule#words()} method.
 *     </li>
 *
 *     <li>2. generating string that matched to a given regular expression.
 *     To do that, set regular expression through {@link StringRule#regexp()} method.
 *     </li>
 *
 *     <li>3. generating string of random length within specified bounds and containing specified symbols.
 *     To do that, do not set words and regular expression data.
 *     User methods {@link StringRule#chars()}, {@link StringRule#minLength()}, {@link StringRule#maxLength()} to
 *     configure generating string.
 *     </li>
 * </ul>
 *
 * @see Boundary
 * @see Group
 */

@Retention(RUNTIME)
@Target(FIELD)
@Rule
@Repeatable(StringRules.class)
public @interface StringRule {

    Class<?> GENERATED_TYPE = String.class;

    /**
     * Set words to getting one of them randomly.
     * <p>
     * If randomly chosen string will be shorter or longer then
     * {@link StringRule#minLength()} and {@link StringRule#maxLength()} restrictions,
     * exception will be thrown.
     *
     * @return string array to select random one
     */
    String[] words() default {};

    /**
     * Set regular expression to get a random string matching to it.
     * <p>
     * If generated string will be shorter or longer then
     * {@link StringRule#minLength()} and {@link StringRule#maxLength()} restrictions,
     * exception will be thrown.
     *
     * @return regular expression to generate string
     */
    String regexp() default "";

    /**
     * @return minimum string length
     */
    int minLength() default 0;

    /**
     * @return maximum string length
     */
    int maxLength() default 1000;

    /**
     * @return chars to generate string within specified min and max bounds
     */
    String chars() default CharSet.DEFAULT_CHARSET;

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
