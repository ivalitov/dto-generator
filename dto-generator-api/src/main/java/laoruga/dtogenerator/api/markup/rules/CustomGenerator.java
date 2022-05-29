package laoruga.dtogenerator.api.markup.rules;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.util.List;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Retention(RUNTIME)
@Target(FIELD)
@Rule
public @interface CustomGenerator {

    Class<?> generatorClass();

    String[] args() default {};
}
