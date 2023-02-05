package org.laoruga.dtogenerator;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import org.apache.commons.lang3.tuple.Pair;
import org.laoruga.dtogenerator.api.generators.IGeneratorBuilder;
import org.laoruga.dtogenerator.api.remarks.CustomRuleRemarkWrapper;
import org.laoruga.dtogenerator.config.DtoGeneratorInstanceConfig;
import org.laoruga.dtogenerator.constants.RuleRemark;
import org.laoruga.dtogenerator.exceptions.DtoGeneratorException;

import java.lang.annotation.Annotation;
import java.util.function.Supplier;

import static org.laoruga.dtogenerator.DtoGeneratorBuildersTree.ROOT;
import static org.laoruga.dtogenerator.util.StringUtils.splitPath;

/**
 * @author Il'dar Valitov
 * Created on 16.04.2022
 */
public class DtoGeneratorBuilder<T> {

    @Getter(AccessLevel.PROTECTED)
    private final DtoGeneratorInstanceConfig configuration;
    @Getter(AccessLevel.PROTECTED)
    private final TypeGeneratorsProvider typeGeneratorsProvider;
    @Getter(AccessLevel.PROTECTED)
    private final DtoGeneratorBuildersTree dtoGeneratorBuildersTree;
    @Getter(AccessLevel.PROTECTED)
    private final FieldGroupFilter fieldGroupFilter;

    DtoGeneratorBuilder(Class<T> dtoClass) {
        this(new DtoInstanceSupplier(dtoClass));
    }

    DtoGeneratorBuilder(T dtoInstance) {
        this(() -> dtoInstance);
    }

    private DtoGeneratorBuilder(Supplier<Object> dtoInstanceSupplier) {
        this.configuration = new DtoGeneratorInstanceConfig();
        this.fieldGroupFilter = new FieldGroupFilter();
        this.typeGeneratorsProvider = new TypeGeneratorsProvider(
                configuration,
                new TypeGeneratorRemarksProvider(),
                fieldGroupFilter,
                new String[]{ROOT},
                this::getDtoGeneratorBuildersTree);
        this.typeGeneratorsProvider.setDtoInstanceSupplier(dtoInstanceSupplier);
        this.dtoGeneratorBuildersTree = new DtoGeneratorBuildersTree(
                DtoGeneratorBuilderTreeNode.createRootNode(this)
        );
    }


    /**
     * Constructor to copy builder for nested DTO generation.
     *
     * @param configuration            - configuration instance
     * @param typeGeneratorsProvider   - generators provider for field values
     * @param dtoGeneratorBuildersTree - generator builders tree
     * @param fieldGroupFilter         - groups for filtering fields
     */
    protected DtoGeneratorBuilder(DtoGeneratorInstanceConfig configuration,
                                  TypeGeneratorsProvider typeGeneratorsProvider,
                                  DtoGeneratorBuildersTree dtoGeneratorBuildersTree,
                                  FieldGroupFilter fieldGroupFilter) {
        this.configuration = configuration;
        this.typeGeneratorsProvider = typeGeneratorsProvider;
        this.dtoGeneratorBuildersTree = dtoGeneratorBuildersTree;
        this.fieldGroupFilter = fieldGroupFilter;
    }


    /**
     * @param rulesAnnotationClass - not collection only
     * @param generatorBuilder     - builder of not collection type
     * @return - this
     */
    public DtoGeneratorBuilder<T> setGeneratorBuilder(@NonNull Class<? extends Annotation> rulesAnnotationClass,
                                                      @NonNull IGeneratorBuilder generatorBuilder) {
        typeGeneratorsProvider.overrideGenerator(rulesAnnotationClass, generatorBuilder);
        return this;
    }


    /**
     * @param fieldName        name of field to generate value
     * @param generatorBuilder builder of generator of any type
     * @return - this
     */
    public DtoGeneratorBuilder<T> setGeneratorBuilder(@NonNull String fieldName,
                                                      @NonNull IGeneratorBuilder generatorBuilder) {
        Pair<String, String[]> fieldNameAndPath = splitPath(fieldName);
        getDtoGeneratorBuildersTree().getBuilderLazy(fieldNameAndPath.getRight())
                .getTypeGeneratorsProvider()
                .setGeneratorBuilderForField(fieldNameAndPath.getLeft(), generatorBuilder);
        return this;
    }

    /*
     * Basic Rule Remarks
     */

    public DtoGeneratorBuilder<T> setRuleRemark(@NonNull String fieldName,
                                                @NonNull RuleRemark ruleRemark) throws DtoGeneratorException {
        Pair<String, String[]> fieldNameAndPath = splitPath(fieldName);
        getDtoGeneratorBuildersTree().getBuilderLazy(fieldNameAndPath.getRight())
                .getTypeGeneratorsProvider()
                .getTypeGeneratorRemarksProvider()
                .setBasicRuleRemarkForField(fieldNameAndPath.getLeft(), ruleRemark);
        return this;
    }

    public DtoGeneratorBuilder<T> setRuleRemark(@NonNull RuleRemark basicRuleRemark) throws DtoGeneratorException {
        this.getTypeGeneratorsProvider()
                .getTypeGeneratorRemarksProvider()
                .setBasicRuleRemarkForAnyField(basicRuleRemark);
        return this;
    }

    /*
     * Custom Rule Remarks
     */

    public DtoGeneratorBuilder<T> setRuleRemarksCustom(@NonNull String fieldName,
                                                       @NonNull CustomRuleRemarkWrapper ruleRemark) {
        Pair<String, String[]> fieldNameAndPath = splitPath(fieldName);
        getDtoGeneratorBuildersTree().getBuilderLazy(fieldNameAndPath.getRight())
                .getTypeGeneratorsProvider()
                .getTypeGeneratorRemarksProvider()
                .addCustomRuleRemarkForField(fieldNameAndPath.getLeft(), ruleRemark);
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
     * @return - this
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

}
