package org.laoruga.dtogenerator;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.laoruga.dtogenerator.constants.RuleType;
import org.laoruga.dtogenerator.exceptions.DtoGeneratorException;

import java.lang.annotation.Annotation;
import java.util.Objects;

/**
 * @author Il'dar Valitov
 * Created on 11.11.2022
 */
@NoArgsConstructor
@Getter(AccessLevel.PACKAGE)
class RuleInfoBuilder implements IRuleInfoBuilder {
    private Annotation rule;
    private RuleType ruleType;
    private Boolean multipleRules;
    private String groupName;
    private RuleInfoBuilder collectionBuilder;

    public boolean isEmpty() {
        return rule == null && collectionBuilder == null;
    }

    public RuleInfoBuilder rule(Annotation rule) {
        if (this.rule != null) {
            throwUnitException();
        }
        this.rule = rule;
        return this;
    }

    public RuleInfoBuilder collectionRuleInfoBuilder(RuleInfoBuilder collectionBuilder) {
        if (this.collectionBuilder != null) {
            throwCollectionException();
        }
        this.collectionBuilder = collectionBuilder;
        return this;
    }

    public RuleInfoBuilder ruleType(RuleType ruleType) {
        if (this.ruleType != null) {
            throwUnitException();
        }
        this.ruleType = ruleType;
        return this;
    }

    public RuleInfoBuilder multipleRules(boolean multipleRules) {
        if (this.multipleRules != null) {
            throwUnitException();
        }
        this.multipleRules = multipleRules;
        return this;
    }

    public RuleInfoBuilder groupName(String groupName) {
        if (this.groupName != null) {
            throwUnitException();
        }
        this.groupName = groupName;
        return this;
    }

    @Override
    public IRuleInfo build() {
        if (collectionBuilder == null) {
            return buildUnit();
        } else {
            return buildCollection();
        }
    }

    private IRuleInfo buildCollection() {
        asserCollectionParams();
        checkCollectionGroup();
        RuleInfoCollection ruleInfo = new RuleInfoCollection();
        ruleInfo.setElementRule(rule != null ? buildUnit() : null);
        ruleInfo.setCollectionRule(collectionBuilder.buildUnit());
        ruleInfo.setGroup(groupName);
        return ruleInfo;
    }

    private IRuleInfo buildUnit() {
        assertUnitParams();
        RuleInfo ruleInfo = new RuleInfo();
        ruleInfo.setRule(rule);
        ruleInfo.setRuleType(ruleType);
        ruleInfo.setGroup(groupName);
        ruleInfo.setMultipleRules(multipleRules);
        return ruleInfo;
    }

    private void assertUnitParams() {
        try {
            Objects.requireNonNull(rule);
            Objects.requireNonNull(ruleType);
            Objects.requireNonNull(groupName);
            Objects.requireNonNull(multipleRules);
        } catch (Exception e) {
            throw new DtoGeneratorException("Failed to construct unit or collection rules info.", e);
        }

    }

    private void asserCollectionParams() {
        try {
            collectionBuilder.assertUnitParams();
        } catch (Exception e) {
            throw new DtoGeneratorException("Failed to construct collection item rules info.", e);
        }
        try {
            assertUnitParams();
        } catch (Exception e) {
            throw new DtoGeneratorException("Failed to construct collection rules info.", e);
        }
    }

    private void checkCollectionGroup() {
        if (!groupName.equals(collectionBuilder.getGroupName())) {
            throw new DtoGeneratorException("Unexpected error, collection generator's group not equals to unit");
        }
    }

    private static void throwUnitException() {
        throw new DtoGeneratorException("Field annotated more then one unit rules annotation");
    }

    private static void throwCollectionException() {
        throw new DtoGeneratorException("Field annotated more then one collection rules annotation");
    }
}
