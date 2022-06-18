package laoruga.dtogenerator.api.markup.rules;

import laoruga.dtogenerator.api.constants.CharSet;
import laoruga.dtogenerator.api.constants.BasicRuleRemark;
import laoruga.dtogenerator.api.constants.Group;
import laoruga.dtogenerator.api.markup.remarks.IRuleRemark;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static laoruga.dtogenerator.api.constants.BasicRuleRemark.RANDOM_VALUE;
import static laoruga.dtogenerator.api.constants.Group.DEFAULT;

@Retention(RUNTIME)
@Target(FIELD)
@Rule
public @interface StringRule {

    int DEFAULT_MIN_SYMBOLS_NUMBER = 0;
    int DEFAULT_MAX_SYMBOLS_NUMBER = 1000;
    CharSet[] DEFAULT_CHARSET = new CharSet[]{CharSet.NUM, CharSet.ENG, CharSet.RUS};
    String DEFAULT_CHARS = "";
    IRuleRemark DEFAULT_RULE_REMARK = RANDOM_VALUE;
    Class<?>[] APPLICABLE_TYPES = {String.class};
    char DEFAULT_WILDCARD = '*';
    char DEFAULT_TYPE_MARKER = '%';
    String DEFAULT_MASK = "";

    int minSymbols() default DEFAULT_MIN_SYMBOLS_NUMBER;

    int maxSymbols() default DEFAULT_MAX_SYMBOLS_NUMBER;

    CharSet[] charset() default {CharSet.NUM, CharSet.ENG, CharSet.RUS};

    String chars() default DEFAULT_CHARS;

    BasicRuleRemark ruleRemark() default RANDOM_VALUE;

    Group group() default DEFAULT;

    /**
     * Mask examples:
     * 1) mask: +89 (***) ***-**-** and charset: NUM
     * result: +89 (923) 152-12-64
     * 2) mask: %ENG%{*} +89 (***) ***-**-** %ENG%** and charset: NUM
     * @return mask for resulting string
     */
    String mask() default DEFAULT_MASK;

    char maskWildcard() default DEFAULT_WILDCARD;

    char maskTypeMarker() default DEFAULT_TYPE_MARKER;
}
