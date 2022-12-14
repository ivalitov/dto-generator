package org.laoruga.dtogenerator;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import org.apache.commons.math3.util.Pair;
import org.laoruga.dtogenerator.api.generators.IGeneratorBuilder;
import org.laoruga.dtogenerator.api.remarks.CustomRuleRemarkWrapper;
import org.laoruga.dtogenerator.config.DtoGeneratorInstanceConfig;
import org.laoruga.dtogenerator.constants.RuleRemark;
import org.laoruga.dtogenerator.exceptions.DtoGeneratorException;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.function.Supplier;

/**
 * @author Il'dar Valitov
 * Created on 16.04.2022
 */
public class DtoGeneratorBuilder<T> {

    private final DtoGeneratorInstanceConfig configuration;
    @Getter(AccessLevel.PROTECTED)
    private final TypeGeneratorsProvider<T> typeGeneratorsProvider;
    private final DtoGeneratorBuildersTree dtoGeneratorBuildersTree;
    private final FieldGroupFilter fieldGroupFilter;

    DtoGeneratorBuilder(Class<T> dtoClass) {
        this(new DtoInstanceSupplier<>(dtoClass));
    }

    DtoGeneratorBuilder(T dtoInstance) {
        this(() -> dtoInstance);
    }

    private DtoGeneratorBuilder(Supplier<T> dtoInstanceSupplier) {
        this.configuration = new DtoGeneratorInstanceConfig();
        this.fieldGroupFilter = new FieldGroupFilter();
        this.dtoGeneratorBuildersTree = new DtoGeneratorBuildersTree(this);
        this.typeGeneratorsProvider = new TypeGeneratorsProvider<>(
                configuration,
                new TypeGeneratorRemarksProvider(),
                fieldGroupFilter,
                new String[]{DtoGeneratorBuildersTree.ROOT},
                dtoGeneratorBuildersTree);
        this.typeGeneratorsProvider.setDtoInstanceSupplier(dtoInstanceSupplier);
    }


    /**
     * Constructor to copy builder for creating Builder for nested DTOs generating.
     *
     * @param toCopy          from
     * @param pathFromRootDto - path to nested DTO field
     */
    DtoGeneratorBuilder(DtoGeneratorBuilder<T> toCopy, String[] pathFromRootDto) {
        this.configuration = toCopy.configuration;
        this.typeGeneratorsProvider = new TypeGeneratorsProvider<>(toCopy.typeGeneratorsProvider, pathFromRootDto);
        this.dtoGeneratorBuildersTree = toCopy.dtoGeneratorBuildersTree;
        this.fieldGroupFilter = toCopy.fieldGroupFilter;
    }

    /**
     * @param rulesAnnotationClass - not collection only
     * @param generatorBuilder     - builder of not collection type
     */
    public DtoGeneratorBuilder<T> setGeneratorBuilder(@NonNull Class<? extends Annotation> rulesAnnotationClass,
                                                      @NonNull IGeneratorBuilder generatorBuilder) throws DtoGeneratorException {
        typeGeneratorsProvider.overrideGenerator(rulesAnnotationClass, generatorBuilder);
        return this;
    }


    /**
     * @param fieldName        name of field to generate value
     * @param generatorBuilder builder of generator of any type
     */
    public DtoGeneratorBuilder<T> setGeneratorBuilder(@NonNull String fieldName,
                                                      @NonNull IGeneratorBuilder generatorBuilder) throws DtoGeneratorException {
        Pair<String, String[]> fieldAndPath = splitPathToField(fieldName);
        DtoGeneratorBuilder<?> dtoGeneratorBuilder = getBuilderFromTreeOrThis(fieldAndPath.getSecond());
        dtoGeneratorBuilder.typeGeneratorsProvider.setGeneratorBuilderForField(fieldAndPath.getFirst(), generatorBuilder);
        return this;
    }

    /*
     * Basic Rule Remarks
     */

    public DtoGeneratorBuilder<T> setRuleRemark(@NonNull String fieldName,
                                                @NonNull RuleRemark ruleRemark) throws DtoGeneratorException {
        Pair<String, String[]> fieldAndPath = splitPathToField(fieldName);
        DtoGeneratorBuilder<?> fieldAndBuilder = getBuilderFromTreeOrThis(fieldAndPath.getSecond());
        fieldAndBuilder.typeGeneratorsProvider.getTypeGeneratorRemarksProvider().setBasicRuleRemarkForField(
                fieldAndPath.getFirst(), ruleRemark);
        return this;
    }

    public DtoGeneratorBuilder<T> setRuleRemark(@NonNull RuleRemark basicRuleRemark) throws DtoGeneratorException {
        this.typeGeneratorsProvider.getTypeGeneratorRemarksProvider().setBasicRuleRemarkForFields(basicRuleRemark);
        return this;
    }

    /*
     * Custom Rule Remarks
     */

    public DtoGeneratorBuilder<T> setRuleRemarksCustom(@NonNull String fieldName,
                                                       @NonNull CustomRuleRemarkWrapper... ruleRemark) {
        Pair<String, String[]> fieldAndPath = splitPathToField(fieldName);
        DtoGeneratorBuilder<?> fieldAndBuilder = getBuilderFromTreeOrThis(fieldAndPath.getSecond());
        fieldAndBuilder.typeGeneratorsProvider.getTypeGeneratorRemarksProvider().addCustomRuleRemarkForField(
                fieldAndPath.getFirst(), ruleRemark);
        return this;
    }

    public DtoGeneratorBuilder<T> setRuleRemarksCustom(@NonNull CustomRuleRemarkWrapper... ruleRemarks) {
        this.typeGeneratorsProvider.getTypeGeneratorRemarksProvider().addRuleRemarkForAllFields(ruleRemarks);
        return this;
    }

    /*
     * Groups
     */

    /**
     * Passing groups using for:
     * - exclusion fields annotated with @Rule;
     * - selecting @Rule by group when field annotated with more than one @Rule.
     * <p>
     * If no group passed - DEFAULT group will be used by default,
     * else - only passed groups are used (In this case, if you need to use DEFAULT group, you need to pass it too).
     *
     * @param groups - groups by which @Rule will be filtered
     */
    public DtoGeneratorBuilder<T> includeGroups(String... groups) {
        if (groups != null && groups.length != 0) {
            this.typeGeneratorsProvider.getRulesInfoExtractor().getFieldsGroupFilter().includeGroups(groups);
        }
        return this;
    }

    public DtoGeneratorInstanceConfig getUserConfig() {
        return configuration;
    }

    /*
     * Build
     */

    /**
     * @return dto builder instance
     */
    public DtoGenerator<T> build() {
        return new DtoGenerator<>(typeGeneratorsProvider, this);
    }

    private DtoGeneratorBuilder<?> getBuilderFromTreeOrThis(String[] pathToField) {
        if (pathToField != null) {
            return dtoGeneratorBuildersTree.getBuilder(pathToField);
        } else {
            return this;
        }
    }

    private Pair<String, String[]> splitPathToField(String fieldsFromRoot) {
        if (fieldsFromRoot.contains(".")) {
            String[] pathToField = fieldsFromRoot.split("\\.");
            String fieldName = pathToField[pathToField.length - 1];
            pathToField = Arrays.copyOf(pathToField, pathToField.length - 1);
            return Pair.create(fieldName, pathToField);
        } else {
            return Pair.create(fieldsFromRoot, null);
        }
    }

}
