package laoruga.dtogenerator.api.markup.rules;

import laoruga.dtogenerator.api.constants.Group;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.util.List;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static laoruga.dtogenerator.api.constants.Group.DEFAULT;

@Retention(RUNTIME)
@Target(FIELD)
@Rule
public @interface NestedDtoRules {

    Class<?>[] APPLICABLE_TYPES = {Object.class};

    Group group() default DEFAULT;
}
