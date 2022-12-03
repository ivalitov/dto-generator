package org.laoruga.dtogenerator.api.rules;

import org.laoruga.dtogenerator.api.remarks.IRuleRemark;
import org.laoruga.dtogenerator.api.rules.meta.Rule;
import org.laoruga.dtogenerator.constants.BasicRuleRemark;
import org.laoruga.dtogenerator.constants.CharSet;
import org.laoruga.dtogenerator.constants.Group;

import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Retention(RUNTIME)
@Target(FIELD)
@Rule
@Repeatable(StringRules.class)
public @interface StringRule {

    int DEFAULT_MIN_SYMBOLS_NUMBER = 0;
    int DEFAULT_MAX_SYMBOLS_NUMBER = 1000;
    String[] WORDS = new String[]{};
    String DEFAULT_CHARS = CharSet.DEFAULT_CHARSET;
    IRuleRemark DEFAULT_RULE_REMARK = BasicRuleRemark.RANDOM_VALUE;
    String DEFAULT_REGEXP = "";

    int minSymbols() default DEFAULT_MIN_SYMBOLS_NUMBER;

    int maxSymbols() default DEFAULT_MAX_SYMBOLS_NUMBER;

    // TODO realize
    String[] words() default {};

    String chars() default DEFAULT_CHARS;

    BasicRuleRemark ruleRemark() default BasicRuleRemark.RANDOM_VALUE;

    String group() default Group.DEFAULT;

    String regexp() default DEFAULT_REGEXP;

    Class<?> generatedType() default String.class;
}
