package laoruga.dtogenerator.api;

import lombok.*;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;


/**
 * @author Il'dar Valitov
 * Created on 23.07.2022
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor
@Builder
class RulesInfo {
    @Builder.Default
    private List<RuleType> ruleType = new LinkedList<>();
    private Action action;
    private Annotation collectionGenerationRules;
    private Annotation itemGenerationRules;
    @Setter
    boolean collectionExplicitlySet;
    @Setter
    boolean itemExplicitlySet;

    public boolean checkType(RuleType... types) {
        if (types.length == 1) {
            return ruleType.size() == 1 && ruleType.get(0) == types[0];
        } else {
            return types.length == ruleType.size() &&
                    new HashSet<>(ruleType).containsAll(Arrays.asList(types));
        }
    }
}
