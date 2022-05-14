package laoruga.dtogenerator.api.markup.rules;

import laoruga.dtogenerator.api.constants.CharSet;
import laoruga.dtogenerator.api.markup.remarks.BasicRuleRemark;
import laoruga.dtogenerator.api.markup.remarks.IRuleRemark;
import org.apache.commons.math3.analysis.function.Max;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static laoruga.dtogenerator.api.markup.remarks.BasicRuleRemark.RANDOM_VALUE;

@Retention(RUNTIME)
@Target(FIELD)
@Rule
public @interface StringRules {

    int DEFAULT_MIN_SYMBOLS_NUMBER = 0;
    int DEFAULT_MAX_SYMBOLS_NUMBER = 1000;
    CharSet[] DEFAULT_CHARSET = new CharSet[]{CharSet.NUM, CharSet.ENG, CharSet.RUS};
    String DEFAULT_CHARS = "";
    IRuleRemark RULE_REMARK = RANDOM_VALUE;

    int maxSymbols() default DEFAULT_MAX_SYMBOLS_NUMBER;

    int minSymbols() default DEFAULT_MIN_SYMBOLS_NUMBER;

    CharSet[] charset() default {CharSet.NUM, CharSet.ENG, CharSet.RUS};

    String chars() default DEFAULT_CHARS;
    BasicRuleRemark ruleRemark() default RANDOM_VALUE;
}
