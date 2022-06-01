package laoruga.dtogenerator.api.markup.rules;

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
public @interface EnumRules {

    IRuleRemark DEFAULT_RULE_REMARK = RANDOM_VALUE;
    Class<?>[] APPLICABLE_TYPES = {Enum.class};

    String[] possibleEnumNames();

    Class<? extends Enum<?>> enumClass();

    BasicRuleRemark ruleRemark() default RANDOM_VALUE;

    Group group() default DEFAULT;
}
