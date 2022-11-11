package laoruga.dtogenerator.api;

import java.lang.annotation.Annotation;

/**
 * @author Il'dar Valitov
 * Created on 11.11.2022
 */
public interface IRuleInfo {
    Annotation getRule();
    String getGroup();
    boolean isTypesEqual(RuleType ruleType);
}
