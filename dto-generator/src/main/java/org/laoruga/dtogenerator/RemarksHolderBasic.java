package org.laoruga.dtogenerator;

import lombok.NonNull;
import org.laoruga.dtogenerator.api.remarks.IRuleRemark;
import org.laoruga.dtogenerator.exceptions.DtoGeneratorException;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author Il'dar Valitov
 * Created on 19.05.2022
 */

public class RemarksHolderBasic {

    private final Map<String, IRuleRemark> basicRuleRemarksMapByField;
    private final AtomicReference<IRuleRemark> basicRuleRemarkForAnyField;

    public RemarksHolderBasic() {
        this(new AtomicReference<>());
    }

    /**
     * Copy Constructor
     *
     * @param toCopy - source
     */
    RemarksHolderBasic(RemarksHolderBasic toCopy) {
        this(new AtomicReference<>(toCopy.basicRuleRemarkForAnyField.get()));
    }

    private RemarksHolderBasic(AtomicReference<IRuleRemark> basicRuleRemarkForAnyField) {
        this.basicRuleRemarksMapByField = new HashMap<>();
        this.basicRuleRemarkForAnyField = basicRuleRemarkForAnyField;
    }

    /*
     * Getters and Setters
     */

    void setBasicRuleRemarkForField(@NonNull String filedName,
                                    @NonNull org.laoruga.dtogenerator.constants.RuleRemark ruleRemark) {
        if (basicRuleRemarksMapByField.containsKey(filedName)) {
            throw new DtoGeneratorException("Attempt to overwrite remark from: '" + getBasicRuleRemarkOrNull(filedName) + "'" +
                    " to: '" + ruleRemark + "' for field '" + filedName + "'.");
        }
        basicRuleRemarksMapByField.put(filedName, ruleRemark);
    }

    void setBasicRuleRemarkForAnyField(org.laoruga.dtogenerator.constants.RuleRemark basicRuleRemark) {
        if (basicRuleRemarkForAnyField.get() != null && basicRuleRemarkForAnyField.get() != basicRuleRemark) {
            throw new DtoGeneratorException("Attempt to overwrite remark for all fields from: '"
                    + basicRuleRemarkForAnyField.get() + "' to: '" + basicRuleRemark + "'.");
        }
        basicRuleRemarkForAnyField.set(basicRuleRemark);
    }

    public IRuleRemark getBasicRuleRemarkOrNull(String fieldName) throws NullPointerException {
        if (basicRuleRemarksMapByField.containsKey(fieldName)) {
            return basicRuleRemarksMapByField.get(fieldName);
        }
        return basicRuleRemarkForAnyField.get();
    }
}
