package laoruga.dtogenerator.api.markup;

import laoruga.dtogenerator.api.markup.remarks.BasicRuleRemark;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * @author Il'dar Valitov
 * Created on 16.04.2022
 */

@Retention(RUNTIME)
@Target(METHOD)
public @interface BoundType {

    BasicRuleRemark value();

}
