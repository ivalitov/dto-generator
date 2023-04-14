package org.laoruga.dtogenerator;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import org.apache.commons.lang3.tuple.Pair;
import org.laoruga.dtogenerator.api.generators.Generator;
import org.laoruga.dtogenerator.api.generators.custom.CustomGeneratorArgs;
import org.laoruga.dtogenerator.api.generators.custom.CustomGeneratorConfigMap;
import org.laoruga.dtogenerator.api.generators.custom.CustomGeneratorRemark;
import org.laoruga.dtogenerator.config.Configuration;
import org.laoruga.dtogenerator.config.ConfigurationHolder;
import org.laoruga.dtogenerator.config.CustomGeneratorsConfigurationHolder;
import org.laoruga.dtogenerator.config.TypeGeneratorsConfigForFiled;
import org.laoruga.dtogenerator.config.dto.DtoGeneratorInstanceConfig;
import org.laoruga.dtogenerator.config.dto.DtoGeneratorStaticConfig;
import org.laoruga.dtogenerator.config.types.TypeGeneratorsConfigLazy;
import org.laoruga.dtogenerator.constants.BoundaryConfig;
import org.laoruga.dtogenerator.exceptions.DtoGeneratorException;
import org.laoruga.dtogenerator.generator.config.dto.ConfigDto;
import org.laoruga.dtogenerator.util.dummy.DummyCustomGenerator;

import java.util.Arrays;
import java.util.function.Supplier;

import static org.laoruga.dtogenerator.DtoGeneratorBuildersTree.ROOT;
import static org.laoruga.dtogenerator.util.StringUtils.splitPath;

/**
 * DtoGeneratorBuilder is supposed to instantiate configured {@link DtoGenerator} instances.
 * <p>
 * It allows to:
 * <ul>
 *     <li>add new type generators or replace default ones - {@link DtoGeneratorBuilder#setGenerator(Class, Generator)}</li>
 *     <li>set/override generators for specific fields by field name - {@link DtoGeneratorBuilder#setGenerator(String, Generator)}</li>
 *     <li>change default generators configuration - {@link DtoGeneratorBuilder#setGeneratorConfig(Class, ConfigDto)}</li>
 *     <li>change default generators configuration of specific field - {@link DtoGeneratorBuilder#setGeneratorConfig(String, ConfigDto)}</li>
 *     <li>inject arguments to specific {@link CustomGeneratorArgs} instances - {@link DtoGeneratorBuilder#setGeneratorArgs(Class, String...)}</li>
 *     <li>inject arguments to {@link CustomGeneratorArgs} of specific field - {@link DtoGeneratorBuilder#setGeneratorArgs(String, String...)}</li>
 *     <li>inject key-value parameters to specific {@link CustomGeneratorConfigMap} instances - {@link DtoGeneratorBuilder#addGeneratorParameter(Class, String, String, String...)}</li>
 *     <li>inject key-value parameters to {@link CustomGeneratorConfigMap} generator of specific field - {@link DtoGeneratorBuilder#addGeneratorParameter(String, String, String, String...)}</li>
 *     <li>inject key-value parameters to all {@link CustomGeneratorConfigMap} generators - {@link DtoGeneratorBuilder#addGeneratorParameter(String, String)}</li>
 *     <li>filter generators by their groups- {@link DtoGeneratorBuilder#includeGroups(String...)}</li>
 *     <li>change configuration of current {@link DtoGeneratorBuilder} instance - {@link DtoGeneratorBuilder#getConfig()}</li>
 *     <li>change configuration of any {@link DtoGeneratorBuilder} instance - {@link DtoGeneratorBuilder#getStaticConfig()}</li>
 * </ul>
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
    @Getter(AccessLevel.PROTECTED)
    private final CustomGeneratorsConfigMapHolder customGeneratorsConfigMapHolder;
    private final Class<?> dtoType;
    private final Supplier<?> dtoInstanceSupplier;

    DtoGeneratorBuilder(Class<T> dtoClass) {
        this(new DtoInstanceSupplier(dtoClass), dtoClass);
    }

    DtoGeneratorBuilder(T dtoInstance) {
        this(() -> dtoInstance, dtoInstance.getClass());
    }

    private DtoGeneratorBuilder(Supplier<?> dtoInstanceSupplier, Class<?> dtoType) {
        this.dtoInstanceSupplier = dtoInstanceSupplier;
        this.dtoType = dtoType;
        RemarksHolder remarksHolder = new RemarksHolder();
        CustomGeneratorsConfigMapHolder customGeneratorsConfigMapHolder = new CustomGeneratorsConfigMapHolder();
        ConfigurationHolder configurationHolder = new ConfigurationHolder(
                new DtoGeneratorInstanceConfig(),
                new TypeGeneratorsConfigLazy(),
                new TypeGeneratorsConfigForFiled(),
                new CustomGeneratorsConfigurationHolder(
                        dtoInstanceSupplier,
                        remarksHolder,
                        customGeneratorsConfigMapHolder
                )
        );
        this.fieldGeneratorsProvider = new FieldGeneratorsProvider(
                configurationHolder,
                remarksHolder,
                customGeneratorsConfigMapHolder,
                new FieldFilter(),
                new String[]{ROOT},
                this::getDtoGeneratorBuildersTree,
                dtoInstanceSupplier
        );
        this.configuration = configurationHolder;
        this.remarksHolder = remarksHolder;
        this.customGeneratorsConfigMapHolder = customGeneratorsConfigMapHolder;
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
                                  Supplier<?> dtoInstanceSupplier,
                                  Class<?> dtoType) {
        final DtoGeneratorBuildersTree dtoGeneratorBuildersTree = copyFrom.getDtoGeneratorBuildersTree();
        final RemarksHolder remarksHolder = new RemarksHolder(copyFrom.getRemarksHolder());
        final CustomGeneratorsConfigMapHolder customGeneratorsConfigMapHolder = new CustomGeneratorsConfigMapHolder(copyFrom.getCustomGeneratorsConfigMapHolder());
        final Supplier<?> rootDtoInstanceSupplier = dtoGeneratorBuildersTree
                .getBuilderLazy(ROOT)
                .getFieldGeneratorsProvider()
                .getDtoInstanceSupplier();
        final ConfigurationHolder configurationCopy = new ConfigurationHolder(
                copyFrom.getConfiguration().getDtoGeneratorConfig(),
                copyFrom.getConfiguration().getTypeGeneratorsConfig(),
                new CustomGeneratorsConfigurationHolder(
                        rootDtoInstanceSupplier,
                        remarksHolder,
                        customGeneratorsConfigMapHolder,
                        copyFrom.getConfiguration()
                                .getCustomGeneratorsConfigurators()
                                .getByGeneratorType()
                )
        );

        this.remarksHolder = remarksHolder;
        this.customGeneratorsConfigMapHolder = customGeneratorsConfigMapHolder;
        this.configuration = configurationCopy;
        this.fieldGeneratorsProvider = new FieldGeneratorsProvider(
                copyFrom.getFieldGeneratorsProvider(),
                remarksHolder,
                customGeneratorsConfigMapHolder,
                pathFromRootDto,
                dtoInstanceSupplier,
                configurationCopy
        );
        this.dtoGeneratorBuildersTree = dtoGeneratorBuildersTree;
        this.dtoType = dtoType;
        this.dtoInstanceSupplier = dtoInstanceSupplier;
    }


    /**
     * @return {@link DtoGenerator} instance
     */
    public DtoGenerator<T> build() {

        FieldGeneratorsPreparer fieldGeneratorsPreparer = new FieldGeneratorsPreparer();
        fieldGeneratorsPreparer.prepareGenerators(dtoType, fieldGeneratorsProvider);

        return new DtoGenerator<>(
                fieldGeneratorsPreparer.getFiledGenerators(), dtoInstanceSupplier
        );
    }

    /*
     * Generator Builders Overriding
     */

    /**
     * Overrides generator related to generated type.
     *
     * @param generatedType - type of generated class
     * @param typeGenerator - generator of provided generated type
     * @return - this
     */

    public <U> DtoGeneratorBuilder<T> setGenerator(@NonNull Class<U> generatedType,
                                                   @NonNull Generator<? super U> typeGenerator) {

        fieldGeneratorsProvider.setGenerator(generatedType, typeGenerator);
        return this;
    }

    /**
     * Overrides {@link CustomGeneratorArgs} generator related to generated type.
     *
     * @param generatedType - type of generated class
     * @param typeGenerator - generator of provided generated type
     * @param args          - params for custom generators with args {@link CustomGeneratorArgs}
     * @return - this
     */

    @SuppressWarnings("unchecked")
    public <U> DtoGeneratorBuilder<T> setGenerator(@NonNull Class<U> generatedType,
                                                   @NonNull CustomGeneratorArgs<? super U> typeGenerator,
                                                   String... args) {

        setGenerator(generatedType, typeGenerator);
        setGeneratorArgs(
                (Class<? extends CustomGeneratorArgs<?>>) typeGenerator.getClass(),
                args
        );

        return this;
    }

    /**
     * Overrides generator for the provided field only.
     * <p>
     * If the field is in a nested object, {@param fieldName} has to have a "path" leads
     * to the field - dots separated sequence of field names.
     * <p>
     * For example, if DTO contains 'person' object, path to the person's 'age' field
     * will the following: 'person.age'
     *
     * @param fieldName     - name of the field or path to the field separated by dots
     * @param typeGenerator - field value generator
     * @return - this
     */
    public DtoGeneratorBuilder<T> setGenerator(@NonNull String fieldName,
                                               @NonNull Generator<?> typeGenerator) {

        Pair<String, String[]> fieldNameAndPath = splitPath(fieldName);

        dtoGeneratorBuildersTree
                .getBuilderLazy(fieldNameAndPath.getRight())
                .getFieldGeneratorsProvider()
                .setGeneratorBuilderForField(fieldNameAndPath.getLeft(), typeGenerator);

        return this;
    }

    /**
     * Overrides {@link CustomGeneratorArgs} generator for the provided field only.
     * <p>
     * If the field is in a nested object, {@param fieldName} has to have a "path" leads
     * to the field - dots separated sequence of field names.
     * <p>
     * For example, if DTO contains 'person' object, path to the person's 'age' field
     * will the following: 'person.age'
     *
     * @param fieldName     name of the field or path to the field separated by dots
     * @param typeGenerator field value generator
     * @param args          params for custom generators with args {@link CustomGeneratorArgs}
     * @return this
     */
    public DtoGeneratorBuilder<T> setGenerator(@NonNull String fieldName,
                                               @NonNull CustomGeneratorArgs<?> typeGenerator,
                                               String... args) {

        setGenerator(fieldName, typeGenerator);
        setGeneratorArgs(fieldName, args);

        return this;
    }

    /**
     * Changes default generators configuration.
     * <p>
     * Configuration {@link ConfigDto} instance may contain null values,
     * but only non-null values will be used to override configuration.
     *
     * @param generatedType one of supported types
     * @param configDto     configuration to set
     * @return this
     */
    public <U> DtoGeneratorBuilder<T> setGeneratorConfig(@NonNull Class<U> generatedType,
                                                         @NonNull ConfigDto configDto) {
        fieldGeneratorsProvider.setGeneratorConfigForType(generatedType, configDto);
        return this;
    }

    /**
     * Changes default generators configuration.
     * <p>
     * If the field is in a nested object, {@param fieldName} has to have a "path" leads
     * to the field - dots separated sequence of field names.
     * <p>
     * Configuration {@link ConfigDto} instance may contain null values,
     * but only non-null values will be used to override configuration.
     *
     * @param fieldName       name of the field or path to the field separated by dots
     * @param generatorConfig configuration to set
     * @return this
     */
    public DtoGeneratorBuilder<T> setGeneratorConfig(@NonNull String fieldName,
                                                     @NonNull ConfigDto generatorConfig) {
        Pair<String, String[]> fieldNameAndPath = splitPath(fieldName);
        dtoGeneratorBuildersTree.getBuilderLazy(fieldNameAndPath.getRight())
                .getFieldGeneratorsProvider()
                .setGeneratorConfigForField(fieldNameAndPath.getLeft(), generatorConfig);
        return this;
    }

    /*
     * Boundary Config
     */

    /**
     * Sets boundary configuration parameter for all supported generators and all user's {@link CustomGeneratorRemark}.
     * <p>
     * BoundaryConfig parameters:
     * <ul>
     *    <li>MIN_VALUE</li>
     *    <li>MAX_VALUE</li>
     *    <li>RANDOM_VALUE</li>
     *    <li>NULL_VALUE</li>
     *    <li>NOT_DEFINED</li>
     * </ul>
     *
     * @param boundaryConfig parameter to set
     * @return this
     * @throws DtoGeneratorException throws when trying to overwrite boundaryConfig
     */
    public DtoGeneratorBuilder<T> setBoundaryConfig(@NonNull BoundaryConfig boundaryConfig) throws DtoGeneratorException {

        remarksHolder.setRuleRemarkForAnyField(boundaryConfig);

        return this;
    }

    /**
     * Sets boundary configuration parameter for specific field
     * <p>
     * If the field is in a nested object, {@param fieldName} has to have a "path" leads
     * to the field - dots separated sequence of field names.
     *
     * @param fieldName      name of the field or path to the field separated by dots
     * @param boundaryConfig parameter to set
     * @return this
     * @throws DtoGeneratorException throws when trying to overwrite boundaryConfig
     */

    public DtoGeneratorBuilder<T> setBoundaryConfig(@NonNull String fieldName,
                                                    @NonNull BoundaryConfig boundaryConfig) throws DtoGeneratorException {

        Pair<String, String[]> fieldNameAndPath = splitPath(fieldName);
        dtoGeneratorBuildersTree.getBuilderLazy(fieldNameAndPath.getRight())
                .getRemarksHolder()
                .setRuleRemarkForField(fieldNameAndPath.getLeft(), boundaryConfig);

        return this;
    }

    /*
     * Custom Generators Args
     */

    /**
     * Injects arguments array to {@link CustomGeneratorArgs} instances of passed type.
     *
     * @param customGeneratorClass args will be injected to generators of this type
     * @param args                 args
     * @return this
     */
    public DtoGeneratorBuilder<T> setGeneratorArgs(@NonNull Class<? extends CustomGeneratorArgs<?>> customGeneratorClass,
                                                   String... args) {

        configuration.getCustomGeneratorsConfigurators().setArgs(
                customGeneratorClass,
                args
        );
        return this;
    }

    /**
     * Injects arguments array to generator for specific field.
     * <p>
     * If the field is in a nested object, {@param fieldName} has to have a "path" leads
     * to the field - dots separated sequence of field names.
     *
     * @param fieldName name of the field or path to the field separated by dots
     * @param args      args
     * @return this
     */
    public DtoGeneratorBuilder<T> setGeneratorArgs(String fieldName, String... args) {

        Pair<String, String[]> fieldNameAndPath = splitPath(fieldName);
        dtoGeneratorBuildersTree.getBuilderLazy(fieldNameAndPath.getRight())
                .getConfiguration()
                .getCustomGeneratorsConfigurators()
                .setArgs(fieldNameAndPath.getLeft(), args);
        return this;
    }

    /*
     * Custom Rule Remarks
     */


    /**
     * Adds key-value parameters to {@link CustomGeneratorConfigMap} instances of specific type.
     *
     * @param customGeneratorClass parameters will be injected to generators of this type
     * @param parameterName        first parameter name
     * @param parameterValue       first parameter value
     * @param nameValuePairs       next parameters kay-value pairs
     * @return this
     */
    public DtoGeneratorBuilder<T> addGeneratorParameter(@NonNull Class<? extends CustomGeneratorConfigMap<?>> customGeneratorClass,
                                                        @NonNull String parameterName,
                                                        @NonNull String parameterValue,
                                                        @NonNull String... nameValuePairs) {

        if (nameValuePairs.length % 2 > 0) {
            throw new IllegalArgumentException("Even parameters number expected (key-value pairs), but passed: " +
                    Arrays.asList(nameValuePairs)
            );
        }

        customGeneratorsConfigMapHolder.addParameterForGeneratorType(
                customGeneratorClass,
                parameterName,
                parameterValue
        );

        for (int i = 0; i < nameValuePairs.length; i = i + 2) {
            customGeneratorsConfigMapHolder.addParameterForGeneratorType(
                    customGeneratorClass,
                    nameValuePairs[i],
                    nameValuePairs[i + 1]
            );
        }

        return this;
    }

    /**
     * Adds key-value parameters to {@link CustomGeneratorConfigMap} of specific field.
     * <p>
     * If the field is in a nested object, {@param fieldName} has to have a "path" leads
     * to the field - dots separated sequence of field names.
     *
     * @param fieldName      name of the field or path to the field separated by dots
     * @param parameterName  first parameter name
     * @param parameterValue first parameter value
     * @param nameValuePairs next parameters kay-value pairs
     * @return this
     */
    public DtoGeneratorBuilder<T> addGeneratorParameter(@NonNull String fieldName,
                                                        @NonNull String parameterName,
                                                        @NonNull String parameterValue,
                                                        @NonNull String... nameValuePairs) {

        if (nameValuePairs.length % 2 > 0) {
            throw new IllegalArgumentException("Even parameters number expected (key-value pairs), but passed: " +
                    Arrays.asList(nameValuePairs)
            );
        }

        Pair<String, String[]> fieldNameAndPath = splitPath(fieldName);

        CustomGeneratorsConfigMapHolder customGeneratorsConfigMapHolder = dtoGeneratorBuildersTree
                .getBuilderLazy(fieldNameAndPath.getRight())
                .getCustomGeneratorsConfigMapHolder();

        customGeneratorsConfigMapHolder.addParameterForField(fieldNameAndPath.getLeft(), parameterName, parameterValue);

        for (int i = 0; i < nameValuePairs.length; i = i + 2) {
            customGeneratorsConfigMapHolder.addParameterForField(fieldNameAndPath.getLeft(), nameValuePairs[i], nameValuePairs[i + 1]);
        }

        return this;
    }

    /**
     * Adds key-value parameters to all {@link CustomGeneratorConfigMap} generators.
     *
     * @param parameterName  first parameter name
     * @param parameterValue first parameter value
     * @return this
     */
    public DtoGeneratorBuilder<T> addGeneratorParameter(@NonNull String parameterName, @NonNull String parameterValue) {
        addGeneratorParameter(DummyCustomGenerator.class, parameterName, parameterValue);
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
     * @param groups groups by which @Rule will be filtered
     * @return this
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

    public DtoGeneratorBuilder<T> generateKnownTypes() {
        configuration.getDtoGeneratorConfig().setGenerateAllKnownTypes(true);
        return this;
    }
}
