package laoruga.dtogenerator.api;

import laoruga.dtogenerator.api.exceptions.DtoGeneratorException;
import laoruga.dtogenerator.api.markup.generators.IGenerator;
import laoruga.dtogenerator.api.markup.generators.IGeneratorBuilder;
import laoruga.dtogenerator.api.markup.remarks.BasicRuleRemark;
import laoruga.dtogenerator.api.markup.remarks.CustomRuleRemarkWrapper;
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

    DtoGeneratorBuilder() {
        this.gensBuildersProvider = new GeneratorBuildersProvider(new GeneratorRemarksProvider());
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
        gensBuildersProvider.getGeneratorRemarksProvider().setBasicRuleRemarkForField(filedName, ruleRemark);
        return this;
    }

    public DtoGeneratorBuilder setRuleRemarkForAllFields(@NonNull BasicRuleRemark basicRuleRemark) throws DtoGeneratorException {
        gensBuildersProvider.getGeneratorRemarksProvider().setBasicRuleRemarkForField(null, basicRuleRemark);
        return this;
    }

    /*
     * Custom Rule Remarks
     */

    public DtoGeneratorBuilder addRuleRemarkForField(@NonNull String filedName,
                                                     @NonNull CustomRuleRemarkWrapper... ruleRemark) {
        gensBuildersProvider.getGeneratorRemarksProvider().addCustomRuleRemarkForField(filedName,ruleRemark);
        return this;
    }

    public DtoGeneratorBuilder addRuleRemarkForAllFields(@NonNull CustomRuleRemarkWrapper... ruleRemarks) {
        gensBuildersProvider.getGeneratorRemarksProvider().addRuleRemarkForAllFields(ruleRemarks);
        return this;
    }

    public DtoGenerator build() {
        return new DtoGenerator(
                gensBuildersProvider,
                this);
    }
}
