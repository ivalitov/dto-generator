package org.laoruga.dtogenerator;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.laoruga.dtogenerator.api.generators.*;
import org.laoruga.dtogenerator.api.remarks.IRuleRemark;
import org.laoruga.dtogenerator.api.rules.CustomRule;
import org.laoruga.dtogenerator.api.rules.ListRule;
import org.laoruga.dtogenerator.api.rules.SetRule;
import org.laoruga.dtogenerator.exceptions.DtoGeneratorException;
import org.laoruga.dtogenerator.generators.NestedDtoGenerator;
import org.laoruga.dtogenerator.generators.SimpleTypeGeneratorsFactory;
import org.laoruga.dtogenerator.generators.basictypegenerators.BasicGeneratorsBuilders;
import org.laoruga.dtogenerator.util.ReflectionUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

import static org.laoruga.dtogenerator.util.ReflectionUtils.*;

/**
 * @author Il'dar Valitov
 * Created on 15.05.2022
 */

@Slf4j
@Getter(AccessLevel.PACKAGE)
public class TypeGeneratorsProvider<T> {

    private T dtoInstance;
    private final String[] fieldsFromRoot;
    private final DtoGeneratorBuilder.GeneratorBuildersTree generatorBuildersTree;
    private final GeneratorRemarksProvider generatorRemarksProvider;
    private final SimpleTypeGeneratorsFactory simpleTypeGeneratorsFactory = SimpleTypeGeneratorsFactory.getInstance();
    private final Map<Class<? extends Annotation>, IGeneratorBuilder<IGenerator<?>>> overriddenBuilders;
    // TODO copy these maps via the constructor
    private final Map<String, IGeneratorBuilder<IGenerator<?>>> overriddenBuildersForFields = new HashMap<>();
    private final Map<String, IGeneratorBuilder<ICollectionGenerator<?>>> overriddenCollectionBuildersForFields = new HashMap<>();

    @Getter
    private final RulesInfoExtractor rulesInfoExtractor;

    TypeGeneratorsProvider(GeneratorRemarksProvider generatorRemarksProvider,
                           FieldGroupFilter fieldGroupFilter,
                           String[] fieldsFromRoot,
                           DtoGeneratorBuilder.GeneratorBuildersTree generatorBuildersTree) {
        this.generatorRemarksProvider = generatorRemarksProvider;
        this.fieldsFromRoot = fieldsFromRoot;
        this.overriddenBuilders = new ConcurrentHashMap<>();
        this.rulesInfoExtractor = new RulesInfoExtractor(fieldGroupFilter);
        this.generatorBuildersTree = generatorBuildersTree;
    }

    /**
     * Constructor to copy
     *
     * @param copyFrom source object
     */
    TypeGeneratorsProvider(TypeGeneratorsProvider<?> copyFrom, String[] fieldsFromRoot) {
        this.generatorRemarksProvider = copyFrom.getGeneratorRemarksProvider().copy();
        this.fieldsFromRoot = fieldsFromRoot;
        this.overriddenBuilders = copyFrom.getOverriddenBuilders();
        this.rulesInfoExtractor = copyFrom.getRulesInfoExtractor();
        this.generatorBuildersTree = copyFrom.getGeneratorBuildersTree();
    }

    /**
     * Setter for field delayed field initialisation.
     * When nested DTO generation params are filling in {@link DtoGeneratorBuilder},
     * we don't know type of nested field yet.
     *
     * @param dtoInstance dto instance to build
     */
    void setDtoInstance(Object dtoInstance) {
        if (this.dtoInstance != null) {
            throw new DtoGeneratorException("Dto instance has already been set: '" + this.dtoInstance.getClass() + "'");
        }
        try {
            this.dtoInstance = (T) dtoInstance;
        } catch (ClassCastException e) {
            throw new DtoGeneratorException("Unexpected error", e);
        }
    }

    /**
     * Returns generator Instance for field value generation
     *
     * @param field - validated field
     * @return empty optional if:
     * - no rules annotations found
     * - rules annotations skipped by group
     * - no explicit generators attached for the field
     * else generator instance
     */
    Optional<IGenerator<?>> getGenerator(Field field) {
        String fieldName = field.getName();

        if (isGeneratorOverridden(fieldName)) {
            return Optional.of(getOverriddenGenerator(fieldName));
        }

        Optional<IRuleInfo> rulesInfo;
        try {
            AnnotationErrorsHandler.ResultDto validationResult =
                    new AnnotationErrorsHandler(field.getDeclaredAnnotations()).validate();
            if (!validationResult.getResultString().isEmpty()) {
                throw new DtoGeneratorException("Field annotated wrong:\n" + validationResult.getResultString());
            }
            rulesInfo = rulesInfoExtractor.checkAndWrapAnnotations(field, validationResult);
        } catch (Exception e) {
            throw new DtoGeneratorException("Error while extracting rule annotations from field: '" + field + "'", e);
        }

        if (rulesInfo.isPresent()) {
            boolean isCollectionGenerator = rulesInfo.get().isTypesEqual(RuleType.COLLECTION);
            // TODO by now, only non collection type generators may be overridden by Class
            Annotation nonCollectionRule = isCollectionGenerator ?
                    ((RuleInfoCollection) rulesInfo.get()).getItemRule().getRule() :
                    rulesInfo.get().getRule();
            IGenerator<?> generator = isGeneratorOverridden(nonCollectionRule) ?
                    getOverriddenGenerator(nonCollectionRule) : selectGenerator(field, rulesInfo.get());
            prepareCustomRemarks(generator, fieldName);
            return Optional.of(generator);
        } else {
            return Optional.empty();
        }
    }

    IGenerator<?> selectGenerator(Field field, IRuleInfo ruleInfo) {
        String fieldName = field.getName();
        Annotation fieldRules = ruleInfo.getRule();

        if (ruleInfo.isTypesEqual(RuleType.BASIC)) {
            return getBasicTypeGenerator(fieldName, field.getType(), fieldRules);
        }

        if (ruleInfo.isTypesEqual(RuleType.CUSTOM)) {
            return getCustomGenerator(fieldRules);
        }

        if (ruleInfo.isTypesEqual(RuleType.NESTED)) {
            return getNestedDtoGenerator(field, getFieldsFromRoot());
        }

        if (ruleInfo.isTypesEqual(RuleType.COLLECTION)) {
            RuleInfoCollection collectionRuleInfo = (RuleInfoCollection) ruleInfo;

            if (collectionRuleInfo.getItemRule().isTypesEqual(RuleType.BASIC)) {
                //TODO item generator cannot be overridden
                IGenerator<?> collectionItemGenerator = getBasicTypeGenerator(
                        fieldName + " " + field.getGenericType().getClass(),
                        ReflectionUtils.getSingleGenericType(field),
                        collectionRuleInfo.getItemRule().getRule());
                return getCollectionTypeGenerator(
                        fieldName, field.getType(),
                        collectionRuleInfo.getCollectionRule().getRule(),
                        collectionItemGenerator);
            }

            if (collectionRuleInfo.getItemRule().isTypesEqual(RuleType.CUSTOM)) {
                IGenerator<?> collectionItemGenerator = getCustomGenerator(
                        collectionRuleInfo.getItemRule().getRule());
                return getCollectionTypeGenerator(
                        fieldName, field.getType(),
                        collectionRuleInfo.getCollectionRule().getRule(),
                        collectionItemGenerator);
            }

        }

        throw new DtoGeneratorException("Unexpected error. Unable to create generator for field '" + field + "' depended on types:" +
                " '" + ruleInfo + "'");
    }

    void prepareCustomRemarks(IGenerator<?> generator, String fieldName) {
        if (generator instanceof ICollectionGenerator) {
            prepareCustomRemarks(((ICollectionGenerator<?>) generator).getItemGenerator(), fieldName);
        }
        if (generator instanceof ICustomGeneratorRemarkable) {
            ICustomGeneratorRemarkable<?> remarkableGenerator = (ICustomGeneratorRemarkable<?>) generator;
            getGeneratorRemarksProvider().getRemarks(fieldName, remarkableGenerator)
                    .ifPresent(remarkableGenerator::setRuleRemarks);
        }
    }

    void setGeneratorForField(String fieldName, IGeneratorBuilder<IGenerator<?>> genBuilder) throws DtoGeneratorException {
        if (overriddenBuildersForFields.containsKey(fieldName)) {
            throw new DtoGeneratorException("Generator has already been explicitly added for field: '" + fieldName + "'");
        }
        overriddenBuildersForFields.put(fieldName, genBuilder);
    }

    void overrideGenerator(Class<? extends Annotation> rulesClass, @NonNull IGeneratorBuilder<IGenerator<?>> genBuilder) {
        if (overriddenBuilders.containsKey(rulesClass)) {
            throw new DtoGeneratorException("Generator has already been explicitly added for Rules: '" + rulesClass + "'");
        }
        overriddenBuilders.put(rulesClass, genBuilder);
    }

    /*
     * Various generator providers
     */

    IGenerator<?> getBasicTypeGenerator(String fieldName, Class<?> fieldType, Annotation rules) {
        return simpleTypeGeneratorsFactory.getBasicTypeGenerator(
                fieldName,
                fieldType,
                rules,
                new AtomicReference<>(generatorRemarksProvider.isBasicRuleRemarkExists(fieldName) ?
                        generatorRemarksProvider.getBasicRuleRemark(fieldName) : null)
        );
    }

    IGenerator<?> getCollectionTypeGenerator(String fieldName, Class<?> fieldType, Annotation rules, IGenerator<?> itemGenerator) {
        Class<? extends Annotation> rulesClass = rules.annotationType();
        if (rulesClass != ListRule.class && rulesClass != SetRule.class) {
            throw new DtoGeneratorException("Field " + fieldName + " hasn't been mapped with any collection generator.");
        }

        //TODO collection generator may explicitly set only by field name
        if (isGeneratorOverridden(fieldName, rules)) {
            return getOverriddenGenerator(fieldName, rules);
        }

        if (ListRule.class == rulesClass) {
            ListRule listRule = (ListRule) rules;
            assertTypeCompatibility(fieldType, listRule.listClass());
            return getListGenerator(fieldName, listRule, itemGenerator);
        }

        SetRule setRule = (SetRule) rules;
        assertTypeCompatibility(fieldType, setRule.setClass());
        return getSetGenerator(fieldName, setRule, itemGenerator);
    }

    IGenerator<?> getNestedDtoGenerator(Field field, String[] fieldsPath) {
        String[] pathToNestedDtoField = Arrays.copyOf(fieldsPath, fieldsPath.length + 1);
        pathToNestedDtoField[fieldsPath.length] = field.getName();
        DtoGeneratorBuilder<?> dtoGeneratorBuilder = getGeneratorBuildersTree().getBuilder(pathToNestedDtoField);
        return new NestedDtoGenerator<>(
                dtoGeneratorBuilder.buildNestedFieldGenerator(pathToNestedDtoField, field.getType())
        );
    }

    IGenerator<?> getCustomGenerator(Annotation customGeneratorRules) throws DtoGeneratorException {
        CustomRule customRules;
        try {
            customRules = (CustomRule) customGeneratorRules;
        } catch (ClassCastException e) {
            throw new DtoGeneratorException("Unexpected error. Unexpected annotation instead of: " + CustomRule.class, e);
        }
        Class<?> generatorClass = null;
        try {
            generatorClass = customRules.generatorClass();
            Object generatorInstance = createInstance(generatorClass);
            if (generatorInstance instanceof ICustomGeneratorArgs) {
                log.debug("Args {} have been obtained from Annotation: {}", Arrays.asList(customRules.args()), customRules);
                ((ICustomGeneratorArgs<?>) generatorInstance).setArgs(customRules.args());
            }
            if (generatorInstance instanceof ICustomGeneratorDtoDependent) {
                setDto(generatorInstance);
            }
            if (generatorInstance instanceof ICustomGenerator) {
                return (ICustomGenerator<?>) generatorInstance;
            } else {
                throw new DtoGeneratorException("Failed to prepare custom generator. " +
                        "Custom generator must implements: '" + ICustomGenerator.class + "' or it's heirs.");
            }
        } catch (Exception e) {
            throw new DtoGeneratorException("Error while preparing custom generator from class: " + generatorClass, e);
        }

    }

    private void setDto(Object generatorInstance) {
        try {
            ((ICustomGeneratorDtoDependent) generatorInstance).setDto(dtoInstance);
        } catch (ClassCastException e) {
            throw new DtoGeneratorException("ClassCastException while trying to set basic DTO into " +
                    "DTO dependent custom generator. Perhaps there is wrong argument type is passing into " +
                    "'setDto' method of generator class. " +
                    "Generator class: '" + generatorInstance.getClass() + "', " +
                    "Passing argument type: '" + dtoInstance.getClass() + "'", e);
        } catch (Exception e) {
            throw new DtoGeneratorException("Exception was thrown while trying to set DTO into " +
                    "DTO dependent custom generator: " + generatorInstance.getClass(), e);
        }
    }

    /*
     * Collection generators providers
     */

    IGenerator<?> getListGenerator(String fieldName, ListRule listRule, IGenerator<?> listItemGenerator) {
        IRuleRemark remark = generatorRemarksProvider.isBasicRuleRemarkExists(fieldName) ?
                generatorRemarksProvider.getBasicRuleRemark(fieldName) :
                listRule.ruleRemark();
        return BasicGeneratorsBuilders.collectionBuilder()
                .minSize(listRule.minSize())
                .maxSize(listRule.maxSize())
                .listInstance(createCollectionFieldInstance(listRule.listClass()))
                .itemGenerator(listItemGenerator)
                .ruleRemark(remark)
                .build();
    }

    IGenerator<?> getSetGenerator(String fieldName, SetRule setRule, IGenerator<?> listItemGenerator) {
        IRuleRemark remark = generatorRemarksProvider.isBasicRuleRemarkExists(fieldName) ?
                generatorRemarksProvider.getBasicRuleRemark(fieldName) :
                setRule.ruleRemark();
        return BasicGeneratorsBuilders.collectionBuilder()
                .minSize(setRule.minSize())
                .maxSize(setRule.maxSize())
                .listInstance(createCollectionFieldInstance(setRule.setClass()))
                .itemGenerator(listItemGenerator)
                .ruleRemark(remark)
                .build();

    }

    /*
     * Utils
     */

    private boolean isGeneratorOverridden(String fieldName, Annotation rules) {
        return isGeneratorOverridden(fieldName) || isGeneratorOverridden(rules);
    }

    private boolean isGeneratorOverridden(String fieldName) {
        return overriddenBuildersForFields.containsKey(fieldName) ||
                overriddenCollectionBuildersForFields.containsKey(fieldName);
    }

    private boolean isGeneratorOverridden(Annotation rules) {
        return rules != null && overriddenBuilders.containsKey(rules.annotationType());
    }

    private IGenerator<?> getOverriddenGenerator(@NonNull String fieldName) {
        return overriddenBuildersForFields.get(fieldName).build();
    }

    private IGenerator<?> getOverriddenGenerator(Annotation rules) {
        return overriddenBuilders.get(rules.annotationType()).build();
    }

    private IGenerator<?> getOverriddenGenerator(@NonNull String fieldName, Annotation rules) {
        if (rules != null) {
            return overriddenBuildersForFields.getOrDefault(fieldName, overriddenBuilders.get(rules.annotationType())).build();
        } else {
            return getOverriddenGenerator(fieldName);
        }
    }
}
