package laoruga.dtogenerator.api;

import laoruga.dtogenerator.api.exceptions.DtoGeneratorException;
import laoruga.dtogenerator.api.factory.SimpleTypeGeneratorsFactory;
import laoruga.dtogenerator.api.generators.NestedDtoGenerator;
import laoruga.dtogenerator.api.generators.basictypegenerators.BasicGeneratorsBuilders;
import laoruga.dtogenerator.api.markup.generators.*;
import laoruga.dtogenerator.api.markup.remarks.IRuleRemark;
import laoruga.dtogenerator.api.markup.rules.CustomGenerator;
import laoruga.dtogenerator.api.markup.rules.ListRule;
import laoruga.dtogenerator.api.markup.rules.NestedDtoRules;
import laoruga.dtogenerator.api.markup.rules.SetRule;
import laoruga.dtogenerator.api.markup.rules.meta.Rule;
import laoruga.dtogenerator.api.markup.rules.meta.RuleForCollection;
import laoruga.dtogenerator.api.markup.rules.meta.Rules;
import laoruga.dtogenerator.api.markup.rules.meta.RulesForCollection;
import laoruga.dtogenerator.api.util.ReflectionUtils;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

import static laoruga.dtogenerator.api.RuleType.*;
import static laoruga.dtogenerator.api.util.ReflectionUtils.createCollectionFieldInstance;
import static laoruga.dtogenerator.api.util.ReflectionUtils.createInstance;

/**
 * @author Il'dar Valitov
 * Created on 15.05.2022
 */

@Slf4j
@Getter(AccessLevel.PACKAGE)
public class TypeGeneratorsProvider<DTO_TYPE> {

    private DTO_TYPE dtoInstance;
    private final String[] fieldsFromRoot;
    private final DtoGeneratorBuilder.GeneratorBuildersTree generatorBuildersTree;
    private final GeneratorRemarksProvider generatorRemarksProvider;
    private final SimpleTypeGeneratorsFactory simpleTypeGeneratorsFactory = SimpleTypeGeneratorsFactory.getInstance();
    private final Map<Class<? extends Annotation>, IGeneratorBuilder<IGenerator<?>>> overriddenBuilders;
    // TODO копировать эти карты тоже через конструктор
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
            throw new RuntimeException("Dto instance has already been set: '" + this.dtoInstance.getClass() + "'");
        }
        try {
            this.dtoInstance = (DTO_TYPE) dtoInstance;
        } catch (ClassCastException e) {
            throw new DtoGeneratorException("Unexpected error", e);
        }
    }

    /**
     * Returns generator Instance for field value generation
     *
     * @param field
     * @return generator instance or null if field should not be generated
     */
    IGenerator<?> getGenerator(Field field) {
        RulesInfo rulesInfo = rulesInfoExtractor.checkAndWrapRulesInfo(field);
        switch (rulesInfo.getAction()) {
            case GENERATE:
                IGenerator<?> generator = selectGenerator(field, rulesInfo);
                prepareCustomRemarks(generator);
                return generator;
            case SKIP:
                return null;
            default:
                throw new IllegalStateException("Unexpected value: " + rulesInfo.getAction());
        }
    }

    IGenerator<?> selectGenerator(Field field, RulesInfo rulesInfo) {
        String fieldName = field.getName();
        Annotation fieldRules = rulesInfo.getItemGenerationRules();

        if (rulesInfo.checkType(BASIC)) {
            if (isGeneratorOverridden(fieldName, fieldRules)) {
                return getOverriddenGenerator(fieldName, fieldRules);
            } else {
                return getBasicTypeGenerator(fieldName, field.getType(), fieldRules);
            }
        }

        if (rulesInfo.checkType(CUSTOM)) {
            return getCustomGenerator(fieldRules);
        }

        if (rulesInfo.checkType(NESTED)) {
            return getNestedDtoGenerator(field, getFieldsFromRoot());
        }

        if (rulesInfo.checkType(COLLECTION, BASIC)) {
            //TODO item generator cannot be overridden
            IGenerator<?> collectionItemGenerator = getBasicTypeGenerator(
                    fieldName + " " + field.getGenericType().getClass(),
                    ReflectionUtils.getGenericType(field),
                    fieldRules);
            return getCollectionTypeGenerator(
                    fieldName, field.getType(),
                    rulesInfo.getCollectionGenerationRules(),
                    collectionItemGenerator);
        }

        if (rulesInfo.checkType(COLLECTION, CUSTOM)) {
            IGenerator<?> collectionItemGenerator = getCustomGenerator(fieldRules);
            return getCollectionTypeGenerator(
                    fieldName, field.getType(),
                    rulesInfo.getCollectionGenerationRules(),
                    collectionItemGenerator);
        }

        throw new DtoGeneratorException("Unexpected error. Unable to create generator for field '" + field + "' depended on types:" +
                " '" + rulesInfo.getRuleType() + "'");
    }

    void prepareCustomRemarks(IGenerator<?> generator) {
        if (generator instanceof ICustomGeneratorRemarkable) {
            ICustomGeneratorRemarkable<?> remarkableGenerator = (ICustomGeneratorRemarkable<?>) generator;
            if (getGeneratorRemarksProvider().isCustomRuleRemarkExists(remarkableGenerator)) {
                remarkableGenerator
                        .setRuleRemarks(getGeneratorRemarksProvider()
                                .getCustomRuleRemarks(remarkableGenerator));
            }
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
            return getListGenerator(fieldName, fieldType, (ListRule) rules, itemGenerator);
        }

        return getSetGenerator(fieldName, fieldType, (SetRule) rules, itemGenerator);

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
        CustomGenerator customRules;
        try {
            customRules = (CustomGenerator) customGeneratorRules;
        } catch (ClassCastException e) {
            throw new DtoGeneratorException("Unexpected error. Unexpected annotation instead of: " + CustomGenerator.class, e);
        }
        Class<?> generatorClass = null;
        try {
            generatorClass = customRules.generatorClass();
            Object generatorInstance = createInstance(generatorClass);
            if (generatorInstance instanceof ICustomGeneratorArgs) {
                log.debug("Args {} have been obtained from Annotation: {}",
                        Arrays.asList(customRules.args()), customRules);
                ((ICustomGeneratorArgs<?>) generatorInstance).setArgs(customRules.args());
            }
            if (generatorInstance instanceof ICustomGeneratorDtoDependent) {
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

    /*
     * Collection generators providers
     */

    IGenerator<?> getListGenerator(String fieldName, Class<?> fieldType, ListRule listRule, IGenerator<?> listItemGenerator) {
        IRuleRemark remark = generatorRemarksProvider.isBasicRuleRemarkExists(fieldName) ?
                generatorRemarksProvider.getBasicRuleRemark(fieldName) :
                listRule.ruleRemark();
        return BasicGeneratorsBuilders.collectionBuilder()
                .minSize(listRule.minSize())
                .maxSize(listRule.maxSize())
                .listInstance(createCollectionFieldInstance(fieldType, listRule.listClass()))
                .itemGenerator(listItemGenerator)
                .ruleRemark(remark)
                .build();
    }

    IGenerator<?> getSetGenerator(String fieldName, Class<?> fieldType, SetRule setRule, IGenerator<?> listItemGenerator) {
        IRuleRemark remark = generatorRemarksProvider.isBasicRuleRemarkExists(fieldName) ?
                generatorRemarksProvider.getBasicRuleRemark(fieldName) :
                setRule.ruleRemark();
        return BasicGeneratorsBuilders.collectionBuilder()
                .minSize(setRule.minSize())
                .maxSize(setRule.maxSize())
                .listInstance(createCollectionFieldInstance(fieldType, setRule.setClass()))
                .itemGenerator(listItemGenerator)
                .ruleRemark(remark)
                .build();

    }

    /*
     * Utils
     */

    private boolean isGeneratorOverridden(String fieldName, Annotation rules) {
        return overriddenBuildersForFields.containsKey(fieldName) |
                overriddenCollectionBuildersForFields.containsKey(fieldName) ||
                overriddenBuilders.containsKey(rules.annotationType());
    }

    private IGenerator<?> getOverriddenGenerator(@NonNull String fieldName, Annotation rules) {
        if (rules != null) {
           return overriddenBuildersForFields.getOrDefault(fieldName, overriddenBuilders.get(rules.annotationType())).build();
        } else {
            return overriddenBuildersForFields.get(fieldName).build();
        }
    }

    private static boolean isItCollectionRule(Annotation ruleAnnotation) {
        return ruleAnnotation.annotationType().getDeclaredAnnotation(RuleForCollection.class) != null;
    }

    private static boolean isItCollectionRules(Annotation ruleAnnotation) {
        return ruleAnnotation.annotationType().getDeclaredAnnotation(RulesForCollection.class) != null;
    }

    private static boolean isItCustomRule(Annotation ruleAnnotation) {
        return ruleAnnotation.annotationType() == CustomGenerator.class;
    }

    private static boolean isItNestedRule(Annotation ruleAnnotation) {
        return ruleAnnotation.annotationType() == NestedDtoRules.class;
    }


    private static boolean isItRule(Annotation ruleAnnotation) {
        return ruleAnnotation.annotationType().getDeclaredAnnotation(Rule.class) != null;
    }

    private static boolean isItRules(Annotation ruleAnnotation) {
        return ruleAnnotation.annotationType().getDeclaredAnnotation(Rules.class) != null;
    }
}
