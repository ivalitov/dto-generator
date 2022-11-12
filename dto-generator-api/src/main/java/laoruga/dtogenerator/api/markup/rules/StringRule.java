package laoruga.dtogenerator.api.markup.rules;

import laoruga.dtogenerator.api.constants.BasicRuleRemark;
import laoruga.dtogenerator.api.constants.CharSet;
import laoruga.dtogenerator.api.markup.remarks.IRuleRemark;
import laoruga.dtogenerator.api.markup.rules.meta.Rule;

import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static laoruga.dtogenerator.api.constants.BasicRuleRemark.RANDOM_VALUE;
import static laoruga.dtogenerator.api.constants.Group.DEFAULT;

@Retention(RUNTIME)
@Target(FIELD)
@Rule
@Repeatable(StringRules.class)
public @interface StringRule {

    int DEFAULT_MIN_SYMBOLS_NUMBER = 0;
    int DEFAULT_MAX_SYMBOLS_NUMBER = 1000;
    CharSet[] DEFAULT_CHARSET = new CharSet[]{CharSet.NUM, CharSet.ENG, CharSet.RUS};
    String DEFAULT_CHARS = "";
    IRuleRemark DEFAULT_RULE_REMARK = RANDOM_VALUE;
    Class<?>[] APPLICABLE_TYPES = {String.class};
    char DEFAULT_WILDCARD = '*';
    char DEFAULT_TYPE_MARKER = '%';
    String DEFAULT_REGEXP = "";

    int minSymbols() default DEFAULT_MIN_SYMBOLS_NUMBER;

    int maxSymbols() default DEFAULT_MAX_SYMBOLS_NUMBER;

    CharSet[] charset() default {CharSet.NUM, CharSet.ENG, CharSet.RUS};

    String chars() default DEFAULT_CHARS;

    BasicRuleRemark ruleRemark() default RANDOM_VALUE;

    String group() default DEFAULT;

    String regexp() default DEFAULT_REGEXP;

    char maskWildcard() default DEFAULT_WILDCARD;

    char maskTypeMarker() default DEFAULT_TYPE_MARKER;
}
