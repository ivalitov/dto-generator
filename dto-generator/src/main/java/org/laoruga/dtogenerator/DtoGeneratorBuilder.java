package org.laoruga.dtogenerator;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import org.apache.commons.lang3.tuple.Pair;
import org.laoruga.dtogenerator.api.generators.IGeneratorBuilder;
import org.laoruga.dtogenerator.api.remarks.ICustomRuleRemark;
import org.laoruga.dtogenerator.api.rules.meta.Rule;
import org.laoruga.dtogenerator.config.ConfigurationHolder;
import org.laoruga.dtogenerator.config.dto.DtoGeneratorConfig;
import org.laoruga.dtogenerator.config.dto.DtoGeneratorInstanceConfig;
import org.laoruga.dtogenerator.config.types.TypeGeneratorsConfigLazy;
import org.laoruga.dtogenerator.config.types.TypeGeneratorsConfigSupplier;
import org.laoruga.dtogenerator.constants.RuleRemark;
import org.laoruga.dtogenerator.exceptions.DtoGeneratorException;

import java.lang.annotation.Annotation;
import java.util.function.Supplier;

import static org.laoruga.dtogenerator.DtoGeneratorBuildersTree.ROOT;
import static org.laoruga.dtogenerator.util.StringUtils.splitPath;

/**
 * DtoGeneratorBuilder is configuring {@link DtoGenerator} instance.
 * It allows to change default field values generation logic and
 * update generation parameters from {@link Rule} annotations.
 * It also allows to add your own custom generators for specific fields.
 *
 * @author Il'dar Valitov
 * Created on 16.04.2022
 */
public class DtoGeneratorBuilder<T> {

    @Getter(AccessLevel.PROTECTED)
    private final ConfigurationHolder configuration;
    @Getter(AccessLevel.PROTECTED)
    private final FieldGeneratorsProvider fieldGeneratorsProvider;
    @Getter(AccessLevel.PROTECTED)
    private final DtoGeneratorBuildersTree dtoGeneratorBuildersTree;
    @Getter(AccessLevel.PROTECTED)
    private final RemarksHolder remarksHolder;

    DtoGeneratorBuilder(Class<T> dtoClass) {
        this(new DtoInstanceSupplier(dtoClass));
    }

    DtoGeneratorBuilder(T dtoInstance) {
        this(() -> dtoInstance);
    }

    private DtoGeneratorBuilder(Supplier<?> dtoInstanceSupplier) {
        this.configuration = new ConfigurationHolder(
                new DtoGeneratorInstanceConfig(),
                new TypeGeneratorsConfigLazy()
        );
        this.remarksHolder = new RemarksHolder();
        this.fieldGeneratorsProvider = new FieldGeneratorsProvider(
                configuration,
                remarksHolder,
                new FieldFilter(),
                new String[]{ROOT},
                this::getDtoGeneratorBuildersTree,
                dtoInstanceSupplier
        );
        this.dtoGeneratorBuildersTree = new DtoGeneratorBuildersTree(this);
    }

    /**
     * Constructor for copying a builder for nested DTO generation.
     *
     * @param copyFrom        source builder
     * @param pathFromRootDto path to nested dto
     */

    protected DtoGeneratorBuilder(DtoGeneratorBuilder<?> copyFrom, String[] pathFromRootDto) {
        this.remarksHolder = new RemarksHolder(copyFrom.getRemarksHolder());
        this.configuration = copyFrom.getConfiguration();
        this.fieldGeneratorsProvider = new FieldGeneratorsProvider(
                copyFrom.getFieldGeneratorsProvider(),
                remarksHolder,
                pathFromRootDto);
        this.dtoGeneratorBuildersTree = copyFrom.getDtoGeneratorBuildersTree();
    }


    /**
     * @return {@link DtoGenerator} instance
     */
    public DtoGenerator<T> build() {
        return new DtoGenerator<>(fieldGeneratorsProvider);
    }

    /*
     * Generator Builders Overriding
     */

    /**
     * Overrides generator builder related to the rule annotation.
     * Provided generator will be used for any field annotated with rule of provided class.
     *
     * @param rulesAnnotationClass - rules annotation class
     * @param generatorBuilder     - field generator builder related to provided rules annotation
     * @return - builder instance
     */
    public DtoGeneratorBuilder<T> setGeneratorBuilder(@NonNull Class<? extends Annotation> rulesAnnotationClass,
                                                      @NonNull IGeneratorBuilder generatorBuilder) {
        fieldGeneratorsProvider.overrideGenerator(rulesAnnotationClass, generatorBuilder);
        return this;
    }

    /**
     * Overrides generator builder for the provided field only.
     * If the field is in nested object, path to the field must contain a "path" leads
     * to the field - sequence of field names separated by dots.
     * For example, if DTO contains 'person' object, path to the 'age' field inside it
     * will the following: 'person.age'
     *
     * @param fieldName        - name of the field or path to the field separated by dots
     * @param generatorBuilder - field generator builder related to the provided field
     * @return - builder instance
     */
    public DtoGeneratorBuilder<T> setGeneratorBuilder(@NonNull String fieldName,
                                                      @NonNull IGeneratorBuilder generatorBuilder) {
        Pair<String, String[]> fieldNameAndPath = splitPath(fieldName);
        dtoGeneratorBuildersTree.getBuilderLazy(fieldNameAndPath.getRight())
                .getFieldGeneratorsProvider()
                .setGeneratorBuilderForField(fieldNameAndPath.getLeft(), generatorBuilder);
        return this;
    }

    /*
     * Basic Rule Remarks
     */

    public DtoGeneratorBuilder<T> setRuleRemark(@NonNull String fieldName,
                                                @NonNull RuleRemark ruleRemark) throws DtoGeneratorException {
        Pair<String, String[]> fieldNameAndPath = splitPath(fieldName);
        dtoGeneratorBuildersTree.getBuilderLazy(fieldNameAndPath.getRight())
                .getRemarksHolder()
                .getBasicRemarks()
                .setBasicRuleRemarkForField(fieldNameAndPath.getLeft(), ruleRemark);
        return this;
    }

    public DtoGeneratorBuilder<T> setRuleRemark(@NonNull RuleRemark basicRuleRemark) throws DtoGeneratorException {
        getRemarksHolder()
                .getBasicRemarks()
                .setBasicRuleRemarkForAnyField(basicRuleRemark);
        return this;
    }

    /*
     * Custom Rule Remarks
     */

    public DtoGeneratorBuilder<T> addRuleRemark(@NonNull String fieldName,
                                                @NonNull ICustomRuleRemark ruleRemark) {
        Pair<String, String[]> fieldNameAndPath = splitPath(fieldName);
        dtoGeneratorBuildersTree.getBuilderLazy(fieldNameAndPath.getRight())
                .getRemarksHolder()
                .getCustomRemarks()
                .addRemark(fieldNameAndPath.getLeft(), ruleRemark);
        return this;
    }

    public DtoGeneratorBuilder<T> addRuleRemark(@NonNull ICustomRuleRemark ruleRemarks) {
        getRemarksHolder()
                .getCustomRemarks()
                .addRemarkForAnyField(ruleRemarks);
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
            fieldGeneratorsProvider.addGroups(groups);
        }
        return this;
    }

    /*
     * Configuration
     */

    public DtoGeneratorConfig getDtoGeneratorConfig() {
        return configuration.getDtoGeneratorConfig();
    }

    public TypeGeneratorsConfigSupplier getTypeGeneratorsConfig() {
        return configuration.getTypeGeneratorsConfig();
    }

}
