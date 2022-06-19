package laoruga.dtogenerator.api.markup.rules;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static laoruga.dtogenerator.api.constants.Group.DEFAULT;

@Retention(RUNTIME)
@Target(FIELD)
@Rule
public @interface CustomGenerator {

    Class<?> generatorClass();

    String[] args() default {};

    String group() default DEFAULT;
}
