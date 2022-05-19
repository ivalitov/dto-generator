package laoruga.dtogenerator.api;

import laoruga.dtogenerator.api.exceptions.DtoGeneratorException;
import laoruga.dtogenerator.api.markup.generators.IGenerator;
import laoruga.dtogenerator.api.markup.generators.IGeneratorBuilder;
import laoruga.dtogenerator.api.markup.remarks.BasicRuleRemark;
import laoruga.dtogenerator.api.markup.remarks.CustomRuleRemarkWrapper;
import laoruga.dtogenerator.api.markup.remarks.IRuleRemark;
import lombok.NonNull;

import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * 1. ок - Basic remark applicable to any field marked with simple rules
 * 2. ок - Basic remark for field with name
 * <p>
 * 3. ок - Custom remark for custom generator applicable to any field marked with that custom generator
 * 4. [not finished] Custom remark field with name
 * <p>
 * 5. [not finished] - Concrete generator for specific field (basic or custom, override or new - whatever)
 * 6. Change simple generator for any field
 * 6.1 - default builder
 * 6.2 - custom simple field generator
 * <p>
 * 7. [won't fix] Change custom generator for any field
 * 8. [same as set or override basic generator] Change custom generator for specific field
 * <p>
 * 9. how to apply this to nested pojo field ???
 */
public class DtoGeneratorBuilder {

    private final GeneratorBuildersProvider gensBuildersProvider;

    /**
     * key - field name;
     * if key == null - rule remark is passing to all basic fields generators;
     * if key != null - rule remarks is passing to field with this name.
     */
    private final Map<String, IRuleRemark> fieldSimpleRuleRemarkMap;
    private final Map<Class<? extends IGenerator<?>>, List<CustomRuleRemarkWrapper>> customRuleRemarksForAllFields;
    private final Map<String, List<CustomRuleRemarkWrapper>> fieldCustomRuleRemarkMap;

    public DtoGeneratorBuilder() {
        this.gensBuildersProvider = new GeneratorBuildersProvider(new GeneratorRemarksProvider());
        this.fieldSimpleRuleRemarkMap = new HashMap<>();
        this.customRuleRemarksForAllFields = new HashMap<>();
        this.fieldCustomRuleRemarkMap = new HashMap<>();
    }

    public DtoGeneratorBuilder overrideBasicGenerator(@NonNull Class<? extends Annotation> rules,
                                                      @NonNull IGeneratorBuilder newGeneratorBuilder) throws DtoGeneratorException {
        gensBuildersProvider.overrideGenerator(rules, newGeneratorBuilder);
        return this;
    }

    // TODO fields of nested objects
    public DtoGeneratorBuilder setGeneratorForField(@NonNull String fieldName,
                                                    @NonNull IGeneratorBuilder explicitGenerator) throws DtoGeneratorException {
        gensBuildersProvider.setGeneratorForFields(fieldName, explicitGenerator);
        return this;
    }

    /*
     * Basic Rule Remarks
     */

    public DtoGeneratorBuilder setRuleRemarkForField(@NonNull String filedName,
                                                     @NonNull BasicRuleRemark ruleRemark) throws DtoGeneratorException {
        if (fieldSimpleRuleRemarkMap.containsKey(filedName)) {
            throw new DtoGeneratorException("For field '" + filedName + "' has already been passed 'remark");
        }
        fieldSimpleRuleRemarkMap.put(filedName, ruleRemark);
        return this;
    }

    public DtoGeneratorBuilder setRuleRemarkForAllFields(@NonNull BasicRuleRemark basicRuleRemark) throws DtoGeneratorException {
        if (fieldSimpleRuleRemarkMap.containsKey(null)) {
            throw new DtoGeneratorException("Dto Generator Builder already has a ruleRemarkForAllFields: "
                    + "'" + fieldSimpleRuleRemarkMap.get(null) + "'");
        }
        fieldSimpleRuleRemarkMap.put(null, basicRuleRemark);
        return this;
    }

    /*
     * Custom Rule Remarks
     */

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
        return new DtoGenerator(
                gensBuildersProvider,
                this);
    }
}
