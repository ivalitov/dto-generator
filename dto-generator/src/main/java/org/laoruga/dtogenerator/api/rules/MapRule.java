package org.laoruga.dtogenerator.api.rules;

import org.laoruga.dtogenerator.api.rules.meta.Rule;
import org.laoruga.dtogenerator.constants.Group;
import org.laoruga.dtogenerator.constants.RuleRemark;
import org.laoruga.dtogenerator.constants.RuleType;
import org.laoruga.dtogenerator.util.dummy.DummyMapClass;

import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.util.Map;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * @author Il'dar Valitov
 * Created on 17.03.2023
 */
@Retention(RUNTIME)
@Target(FIELD)
@Rule(RuleType.MAP)
@Repeatable(MapRules.class)
public @interface MapRule {

    Class<?> GENERATED_TYPE = Map.class;

    Entry key();

    Entry value();

    Class<? extends Map> mapClass() default DummyMapClass.class;

    int maxSize() default 10;

    int minSize() default 1;

    RuleRemark ruleRemark() default RuleRemark.RANDOM_VALUE;

    String group() default Group.DEFAULT;

}
