package laoruga.dtogenerator.api.markup.rules;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.util.ArrayList;
import java.util.List;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Retention(RUNTIME)
@Target(FIELD)
@Rule
@RuleForCollection
public @interface ListRules {

    Class<? extends List> listClass() default ArrayList.class;

//    CustomGenerator rules() default @CustomGenerator(generatorClass = Object.class);
}
