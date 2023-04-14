package org.laoruga.dtogenerator;

import lombok.NonNull;
import org.laoruga.dtogenerator.api.remarks.RuleRemark;
import org.laoruga.dtogenerator.exceptions.DtoGeneratorException;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author Il'dar Valitov
 * Created on 19.05.2022
 */

public class RemarksHolder {

    private final Map<String, RuleRemark> ruleRemarksMapByField;
    private final AtomicReference<RuleRemark> ruleRemarkForAnyField;

    public RemarksHolder() {
        this(new AtomicReference<>());
    }

    /**
     * Copy Constructor
     *
     * @param toCopy - source
     */
    RemarksHolder(RemarksHolder toCopy) {
        this(new AtomicReference<>(toCopy.ruleRemarkForAnyField.get()));
    }

    private RemarksHolder(AtomicReference<RuleRemark> ruleRemarkForAnyField) {
        this.ruleRemarksMapByField = new HashMap<>();
        this.ruleRemarkForAnyField = ruleRemarkForAnyField;
    }

    /*
     * Getters and Setters
     */

    void setRuleRemarkForField(@NonNull String filedName,
                               @NonNull RuleRemark ruleRemark) {
        if (ruleRemarksMapByField.containsKey(filedName)) {
            throw new DtoGeneratorException("Attempt to overwrite remark from: '" + getRuleRemarkOrNull(filedName) + "'" +
                    " to: '" + ruleRemark + "' for field '" + filedName + "'.");
        }
        ruleRemarksMapByField.put(filedName, ruleRemark);
    }

    void setRuleRemarkForAnyField(RuleRemark ruleRemark) {
        if (ruleRemarkForAnyField.get() != null && ruleRemarkForAnyField.get() != ruleRemark) {
            throw new DtoGeneratorException("Attempt to overwrite remark for all fields from: '"
                    + ruleRemarkForAnyField.get() + "' to: '" + ruleRemark + "'.");
        }
        ruleRemarkForAnyField.set(ruleRemark);
    }

    public RuleRemark getRuleRemarkOrNull(String fieldName) {
        if (ruleRemarksMapByField.containsKey(fieldName)) {
            return ruleRemarksMapByField.get(fieldName);
        }
        return ruleRemarkForAnyField.get();
    }
}
