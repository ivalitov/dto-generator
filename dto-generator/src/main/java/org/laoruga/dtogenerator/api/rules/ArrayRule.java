package org.laoruga.dtogenerator.api.rules;

import org.laoruga.dtogenerator.api.rules.meta.Rule;
import org.laoruga.dtogenerator.constants.BoundaryConfig;
import org.laoruga.dtogenerator.constants.Group;
import org.laoruga.dtogenerator.constants.RuleType;

import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Retention(RUNTIME)
@Target(FIELD)
@Rule(RuleType.ARRAY)
@Repeatable(ArrayRules.class)
public @interface ArrayRule {

    Class<?>[] GENERATED_TYPES = new Class<?>[]
            {Object[].class, byte[].class, short[].class, char[].class, int[].class, long[].class, boolean[].class};

    Entry element() default @Entry;

    int maxSize() default 10;

    int minSize() default 1;

    BoundaryConfig ruleRemark() default BoundaryConfig.RANDOM_VALUE;

    String group() default Group.DEFAULT;

}
