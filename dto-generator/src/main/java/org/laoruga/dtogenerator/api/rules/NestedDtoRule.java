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
 * Put on the DTO field to tell DtoGenerator to process its fields also as DTO fields.
 *
 * @see Boundary
 * @see Group
 */

@Retention(RUNTIME)
@Target(FIELD)
@Rule
@Repeatable(NestedDtoRules.class)
public @interface NestedDtoRule {

    Class<?> GENERATED_TYPE = Object.class;

    /**
     * You may set boundary parameter for nested DTO.
     * Similar to call {@link org.laoruga.dtogenerator.DtoGeneratorBuilder#setBoundary(Boundary)} method.
     *
     * @return boundary parameter
     * @see Boundary
     */
    Boundary boundary() default Boundary.NOT_DEFINED;

    /**
     * @return group of the generators
     * @see Group
     */
    String group() default Group.DEFAULT;
}