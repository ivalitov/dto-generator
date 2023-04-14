package org.laoruga.dtogenerator.api.rules;

import org.laoruga.dtogenerator.api.generators.custom.CustomGenerator;
import org.laoruga.dtogenerator.api.rules.meta.Rule;
import org.laoruga.dtogenerator.constants.BoundaryConfig;
import org.laoruga.dtogenerator.constants.Group;

import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Retention(RUNTIME)
@Target(FIELD)
@Rule
@Repeatable(CustomRules.class)
public @interface CustomRule {

    Class<?> GENERATED_TYPE = Object.class;

    Class<? extends CustomGenerator<?>> generatorClass();

    String[] args() default {};

    String group() default Group.DEFAULT;

    BoundaryConfig ruleRemark() default BoundaryConfig.NOT_DEFINED;
}
