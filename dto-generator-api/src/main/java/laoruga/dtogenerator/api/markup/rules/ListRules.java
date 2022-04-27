package laoruga.dtogenerator.api.markup.rules;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.util.Collection;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Retention(RUNTIME)
@Target(FIELD)
@Rule
@RuleForCollection
public @interface ListRules {

    Class<? extends Collection> listClass() default Collection.class;

//    CustomGenerator rules() default @CustomGenerator(generatorClass = Object.class);
}
