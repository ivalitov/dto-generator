package laoruga.dtogenerator.api;

import laoruga.dtogenerator.api.exceptions.DtoGeneratorException;
import laoruga.dtogenerator.api.markup.generators.IGenerator;
import laoruga.dtogenerator.api.markup.remarks.BasicRuleRemark;
import laoruga.dtogenerator.api.markup.remarks.CustomRuleRemarkWrapper;
import laoruga.dtogenerator.api.markup.remarks.IRuleRemark;
import lombok.NonNull;

import java.util.*;

import static laoruga.dtogenerator.api.markup.remarks.BasicRuleRemark.RANDOM_VALUE;

public class DtoGeneratorBuilder {

    /**
     * key - field name;
     * if key == null - rule remark is passing to all basic fields generators;
     * if key != null - rule remarks is passing to field with this name.
     */
    private final Map<String, IRuleRemark> fieldSimpleRuleRemarkMap = new HashMap<>();
    private final Map<String, IGenerator<?>> fieldGeneratorMap = new LinkedHashMap<>();
    protected Map<Class<? extends IGenerator<?>>, List<CustomRuleRemarkWrapper>> customRuleRemarksForAllFields = new HashMap<>();
    protected Map<String, List<CustomRuleRemarkWrapper>> fieldCustomRuleRemarkMap = new HashMap<>();

    public DtoGeneratorBuilder setRuleRemarkForAllFields(@NonNull BasicRuleRemark basicRuleRemark) throws DtoGeneratorException {
        if (fieldSimpleRuleRemarkMap.containsKey(null)) {
            throw new DtoGeneratorException("Dto Generator Builder already has a ruleRemarkForAllFields: "
                    + "'" + fieldSimpleRuleRemarkMap.get(null) + "'");
        }
        fieldSimpleRuleRemarkMap.put(null, basicRuleRemark);
        return this;
    }

    // TODO
    //    public DtoGeneratorBuilder addRuleRemarkForAllFields(BasicGensBuilder builders) {
    //        fieldGeneratorMap.put(null, builders.getGenerator());
    //        return this;
    //    }

    // TODO fields of nested objects
    public DtoGeneratorBuilder addRuleRemarkForField(@NonNull String fieldName,
                                                     @NonNull IGenerator<?> explicitGenerator) throws DtoGeneratorException {
        if (fieldGeneratorMap.containsKey(fieldName)) {
            throw new DtoGeneratorException("Generator has already been explicitly added for field: '" + fieldName + "'");
        }
        fieldGeneratorMap.put(fieldName, explicitGenerator);
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

    public DtoGeneratorBuilder addRuleRemarkForField(@NonNull String filedName,
                                                     @NonNull CustomRuleRemarkWrapper... ruleRemark) {
        this.fieldCustomRuleRemarkMap.putIfAbsent(filedName, new LinkedList<>());
        for (CustomRuleRemarkWrapper remark : ruleRemark) {
            this.fieldCustomRuleRemarkMap.get(filedName).add(remark);
        }
        return this;
    }

    public DtoGeneratorBuilder addRuleRemarkForAllFields(@NonNull CustomRuleRemarkWrapper... ruleRemarks) {
        for (CustomRuleRemarkWrapper remark : ruleRemarks) {
            this.customRuleRemarksForAllFields.putIfAbsent(remark.getGeneratorClass(), new LinkedList<>());
            this.customRuleRemarksForAllFields.get(remark.getGeneratorClass()).add(remark);
        }
        return this;
    }

    public DtoGenerator build() {
        return new DtoGenerator(getFieldSimpleRuleRemarkMap(), customRuleRemarksForAllFields, this);
    }

    // TODO maybe exclude this
    private Map<String, IRuleRemark> getFieldSimpleRuleRemarkMap() {
        if (!fieldSimpleRuleRemarkMap.containsKey(null)) {
            fieldSimpleRuleRemarkMap.put(null, RANDOM_VALUE);
        }
        return fieldSimpleRuleRemarkMap;
    }
}
