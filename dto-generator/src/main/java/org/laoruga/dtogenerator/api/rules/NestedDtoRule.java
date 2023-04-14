package org.laoruga.dtogenerator.api.rules;

import org.laoruga.dtogenerator.api.rules.meta.Rule;
import org.laoruga.dtogenerator.constants.Boundary;
import org.laoruga.dtogenerator.constants.Group;

import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Retention(RUNTIME)
@Target(FIELD)
@Rule
@Repeatable(NestedDtoRules.class)
public @interface NestedDtoRule {

    Class<?> GENERATED_TYPE = Object.class;

    String group() default Group.DEFAULT;

    Class<?> generatedType() default Object.class;

    Boundary ruleRemark() default Boundary.NOT_DEFINED;
}