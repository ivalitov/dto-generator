package org.laoruga.dtogenerator;

import lombok.NonNull;
import org.laoruga.dtogenerator.api.remarks.RuleRemark;
import org.laoruga.dtogenerator.constants.BoundaryConfig;
import org.laoruga.dtogenerator.exceptions.DtoGeneratorException;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author Il'dar Valitov
 * Created on 19.05.2022
 */

public class RemarksHolderBasic {

    private final Map<String, RuleRemark> basicRuleRemarksMapByField;
    private final AtomicReference<RuleRemark> basicRuleRemarkForAnyField;

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

    private RemarksHolderBasic(AtomicReference<RuleRemark> basicRuleRemarkForAnyField) {
        this.basicRuleRemarksMapByField = new HashMap<>();
        this.basicRuleRemarkForAnyField = basicRuleRemarkForAnyField;
    }

    /*
     * Getters and Setters
     */

    void setBasicRuleRemarkForField(@NonNull String filedName,
                                    @NonNull BoundaryConfig boundaryConfig) {
        if (basicRuleRemarksMapByField.containsKey(filedName)) {
            throw new DtoGeneratorException("Attempt to overwrite remark from: '" + getBasicRuleRemarkOrNull(filedName) + "'" +
                    " to: '" + boundaryConfig + "' for field '" + filedName + "'.");
        }
        basicRuleRemarksMapByField.put(filedName, boundaryConfig);
    }

    void setBasicRuleRemarkForAnyField(BoundaryConfig boundaryConfig) {
        if (basicRuleRemarkForAnyField.get() != null && basicRuleRemarkForAnyField.get() != boundaryConfig) {
            throw new DtoGeneratorException("Attempt to overwrite remark for all fields from: '"
                    + basicRuleRemarkForAnyField.get() + "' to: '" + boundaryConfig + "'.");
        }
        basicRuleRemarkForAnyField.set(boundaryConfig);
    }

    public RuleRemark getBasicRuleRemarkOrNull(String fieldName) {
        if (basicRuleRemarksMapByField.containsKey(fieldName)) {
            return basicRuleRemarksMapByField.get(fieldName);
        }
        return basicRuleRemarkForAnyField.get();
    }
}
