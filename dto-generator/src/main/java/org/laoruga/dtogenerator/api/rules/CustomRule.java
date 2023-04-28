package org.laoruga.dtogenerator.api.rules;

import org.laoruga.dtogenerator.api.generators.custom.CustomGenerator;
import org.laoruga.dtogenerator.api.rules.meta.Rule;
import org.laoruga.dtogenerator.constants.Boundary;
import org.laoruga.dtogenerator.constants.Group;

import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Put on any field to set custom generator.
 * Custom generator have to implement at list one of the interfaces:
 * <ul>
 *     <li>{@link CustomGenerator CustomGenerator}</li>
 *     <li>{@link org.laoruga.dtogenerator.api.generators.custom.CustomGeneratorArgs CustomGeneratorArgs}</li>
 *     <li>{@link org.laoruga.dtogenerator.api.generators.custom.CustomGeneratorConfigMap CustomGeneratorConfigMap}</li>
 *     <li>{@link org.laoruga.dtogenerator.api.generators.custom.CustomGeneratorBoundary CustomGeneratorBoundary}</li>
 *     <li>{@link org.laoruga.dtogenerator.api.generators.custom.CustomGeneratorDtoDependent CustomGeneratorDtoDependent}</li>
 * </ul>
 *
 * @see Boundary
 * @see Group
 */

@Retention(RUNTIME)
@Target(FIELD)
@Rule
@Repeatable(CustomRules.class)
public @interface CustomRule {

    Class<?> GENERATED_TYPE = Object.class;

    /**
     * Specify custom generator class.
     * Class must have no arg or default constructor.
     * @return custom generator class
     */
    Class<? extends CustomGenerator<?>> generatorClass();

    /**
     * You may set args if generator implements
     * {@link org.laoruga.dtogenerator.api.generators.custom.CustomGeneratorArgs CustomGeneratorArgs} interface.
     * @return arguments for {@link org.laoruga.dtogenerator.api.generators.custom.CustomGeneratorArgs CustomGeneratorArgs}
     * generator
     */
    String[] args() default {};

    /**
     * @return group of the generators
     * @see Group
     */
    String group() default Group.DEFAULT;

    /**
     * @return boundary parameter
     * @see Boundary
     */
    Boundary boundary() default Boundary.NOT_DEFINED;
}
