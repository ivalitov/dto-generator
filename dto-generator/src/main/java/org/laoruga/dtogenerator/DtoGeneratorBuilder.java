package org.laoruga.dtogenerator;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import org.apache.commons.lang3.tuple.Pair;
import org.laoruga.dtogenerator.api.generators.IGenerator;
import org.laoruga.dtogenerator.api.generators.custom.ICustomGeneratorArgs;
import org.laoruga.dtogenerator.api.remarks.ICustomRuleRemark;
import org.laoruga.dtogenerator.api.rules.meta.Rule;
import org.laoruga.dtogenerator.config.Configuration;
import org.laoruga.dtogenerator.config.ConfigurationHolder;
import org.laoruga.dtogenerator.config.TypeGeneratorsConfigForFiled;
import org.laoruga.dtogenerator.config.dto.DtoGeneratorInstanceConfig;
import org.laoruga.dtogenerator.config.dto.DtoGeneratorStaticConfig;
import org.laoruga.dtogenerator.config.types.TypeGeneratorsConfigLazy;
import org.laoruga.dtogenerator.constants.RuleRemark;
import org.laoruga.dtogenerator.exceptions.DtoGeneratorException;
import org.laoruga.dtogenerator.generator.config.dto.ConfigDto;

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
                new TypeGeneratorsConfigLazy(),
                new TypeGeneratorsConfigForFiled()
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

    protected DtoGeneratorBuilder(DtoGeneratorBuilder<?> copyFrom,
                                  String[] pathFromRootDto,
                                  Supplier<?> dtoInstanceSupplier) {
        final ConfigurationHolder configurationCopy = new ConfigurationHolder(copyFrom.getConfiguration());
        this.remarksHolder = new RemarksHolder(copyFrom.getRemarksHolder());
        this.configuration = configurationCopy;
        this.fieldGeneratorsProvider = new FieldGeneratorsProvider(
                copyFrom.getFieldGeneratorsProvider(),
                remarksHolder,
                pathFromRootDto,
                dtoInstanceSupplier,
                configurationCopy);
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
     * Overrides generator builder related to generated type.
     *
     * @param generatedType - type of generated class
     * @param typeGenerator - generator of provided generated type
     * @param args          - params for custom generators with args {@link ICustomGeneratorArgs}
     * @return - this
     */

    public <U> DtoGeneratorBuilder<T> setGenerator(@NonNull Class<U> generatedType,
                                                   @NonNull IGenerator<? super U> typeGenerator,
                                                   String... args) {
        fieldGeneratorsProvider.setGenerator(generatedType, typeGenerator, args);
        return this;
    }

    /**
     * Overrides generator for the provided field only.
     * If the field is in nested object, path to the field must contain a "path" leads
     * to the field - dots separated sequence of field names.
     * For example, if DTO contains 'person' object, path to the 'age' field
     * will the following: 'person.age'
     *
     * @param fieldName     - name of the field or path to the field separated by dots
     * @param typeGenerator - field value generator
     * @param args          - params for custom generators with args {@link ICustomGeneratorArgs}
     * @return - this
     */
    public DtoGeneratorBuilder<T> setGenerator(@NonNull String fieldName,
                                               @NonNull IGenerator<?> typeGenerator,
                                               String... args) {
        Pair<String, String[]> fieldNameAndPath = splitPath(fieldName);
        dtoGeneratorBuildersTree.getBuilderLazy(fieldNameAndPath.getRight())
                .getFieldGeneratorsProvider()
                .setGeneratorBuilderForField(fieldNameAndPath.getLeft(), typeGenerator, args);
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
                                                @NonNull ICustomRuleRemark ruleRemark,
                                                ICustomRuleRemark... ruleRemarks) {
        Pair<String, String[]> fieldNameAndPath = splitPath(fieldName);
        RemarksHolderCustom customRemarks = dtoGeneratorBuildersTree.getBuilderLazy(fieldNameAndPath.getRight())
                .getRemarksHolder()
                .getCustomRemarks();
        customRemarks.addRemark(fieldNameAndPath.getLeft(), ruleRemark);
        for (ICustomRuleRemark remark : ruleRemarks) {
            customRemarks.addRemark(fieldNameAndPath.getLeft(), remark);
        }
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

    public Configuration getConfig() {
        return configuration;
    }

    public Configuration getStaticConfig() {
        return DtoGeneratorStaticConfig.getInstance();
    }

    public DtoGeneratorBuilder<T> setTypeGeneratorConfig(@NonNull String fieldName,
                                                         @NonNull ConfigDto generatorConfig) {
        Pair<String, String[]> fieldNameAndPath = splitPath(fieldName);
        dtoGeneratorBuildersTree.getBuilderLazy(fieldNameAndPath.getRight())
                .getFieldGeneratorsProvider()
                .setGeneratorConfigForField(fieldNameAndPath.getLeft(), generatorConfig);
        return this;
    }

    public <U> DtoGeneratorBuilder<T> setTypeGeneratorConfig(@NonNull Class<U> generatedType,
                                                             @NonNull ConfigDto configDto) {
        fieldGeneratorsProvider.setGeneratorConfigForType(generatedType, configDto);
        return this;
    }

    public DtoGeneratorBuilder<T> generateKnownTypes() {
        configuration.getDtoGeneratorConfig().setGenerateAllKnownTypes(true);
        return this;
    }
}
