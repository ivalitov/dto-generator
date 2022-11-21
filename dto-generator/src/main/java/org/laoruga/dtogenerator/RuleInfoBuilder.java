package org.laoruga.dtogenerator;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
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
    private Runnable ruleInfoAsserter = () -> {
    };

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
        if (this.groupName != null) {
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

    public RuleInfoBuilder setRuleInfoAsserter(Runnable ruleInfoAsserter) {
        this.ruleInfoAsserter = ruleInfoAsserter;
        return this;
    }

    @Override
    public IRuleInfo build() {
        ruleInfoAsserter.run();
        if (collectionBuilder == null) {
            return buildUnit();
        } else {
            return buildCollection();
        }
    }

    private IRuleInfo buildCollection() {
        asserCollectionParams();
        RuleInfoCollection ruleInfo = new RuleInfoCollection();
        ruleInfo.setItemRule(buildUnit());
        ruleInfo.setCollectionRule(collectionBuilder.buildUnit());
        ruleInfo.setGroup(groupName);
        checkCollectionGroup();
        return ruleInfo;
    }

    private IRuleInfo buildUnit() {
        assertUnitParams();
        RuleInfo ruleInfo = new RuleInfo();
        ruleInfo.setRule(Objects.requireNonNull(rule));
        ruleInfo.setRuleType(Objects.requireNonNull(ruleType));
        ruleInfo.setGroup(Objects.requireNonNull(groupName));
        ruleInfo.setMultipleRules(Objects.requireNonNull(multipleRules));
        return ruleInfo;
    }

    private void assertUnitParams() {
        try {
            Objects.requireNonNull(rule);
            Objects.requireNonNull(ruleType);
            Objects.requireNonNull(groupName);
            Objects.requireNonNull(multipleRules);
        } catch (Exception e) {
            throw new DtoGeneratorException("Failed to construct unit or collection item generator.", e);
        }

    }

    private void asserCollectionParams() {
        try {
            Objects.requireNonNull(collectionBuilder, "Collection rules not found");
            collectionBuilder.assertUnitParams();
            assertUnitParams();
        } catch (Exception e) {
            throw new DtoGeneratorException("Failed to construct collection generator.", e);
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
