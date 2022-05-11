package laoruga.dtogenerator.api;

import laoruga.dtogenerator.api.exceptions.DtoGeneratorException;
import laoruga.dtogenerator.api.markup.remarks.BasicRuleRemark;
import laoruga.dtogenerator.api.markup.remarks.IRuleRemark;
import lombok.NonNull;

import java.util.HashMap;
import java.util.Map;

import static laoruga.dtogenerator.api.markup.remarks.BasicRuleRemark.RANDOM_VALUE;

public class DtoGeneratorBuilder {

    /**
     * key - field name;
     * if key == null - rule remark is passing to all basic fields generators;
     * if key != null - rule remarks is passing to field with this name.
     */
    private final Map<String, IRuleRemark> fieldSimpleRuleRemarkMap = new HashMap<>();

    public DtoGeneratorBuilder setRuleRemarkForAllFields(@NonNull BasicRuleRemark basicRuleRemark) throws DtoGeneratorException {
        if (fieldSimpleRuleRemarkMap.containsKey(null)) {
            throw new DtoGeneratorException("Dto Generator Builder already has a ruleRemarkForAllFields: "
                    + "'" + fieldSimpleRuleRemarkMap.get(null) + "'");
        }
        fieldSimpleRuleRemarkMap.put(null, basicRuleRemark);
        return this;
    }

    public DtoGeneratorBuilder addRuleRemarkForField(@NonNull String filedName,
                                                     @NonNull IRuleRemark ruleRemark) throws DtoGeneratorException {
        if (fieldSimpleRuleRemarkMap.containsKey(filedName)) {
            throw new DtoGeneratorException("For field '" + filedName + "' has already been passed 'remark");
        }
        fieldSimpleRuleRemarkMap.put(filedName, ruleRemark);
        return this;
    }

    protected Map<String, IRuleRemark> getFieldSimpleRuleRemarkMap() {
        if (!fieldSimpleRuleRemarkMap.containsKey(null)) {
            fieldSimpleRuleRemarkMap.put(null, RANDOM_VALUE);
        }
        return fieldSimpleRuleRemarkMap;
    }

    public DtoGenerator build() {
        return new DtoGenerator(getFieldSimpleRuleRemarkMap(), this);
    }
}
