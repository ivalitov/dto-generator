package laoruga.dtogenerator.api.markup.rules;

import laoruga.dtogenerator.api.markup.rules.meta.Rule;

import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static laoruga.dtogenerator.api.constants.Group.DEFAULT;

@Retention(RUNTIME)
@Target(FIELD)
@Rule
@Repeatable(CustomRules.class)
public @interface CustomRule {

    Class<?>[] APPLICABLE_TYPES = {Object.class};

    Class<?> generatorClass();

    String[] args() default {};

    String group() default DEFAULT;
}
