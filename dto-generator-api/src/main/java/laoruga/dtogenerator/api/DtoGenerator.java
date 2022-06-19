package laoruga.dtogenerator.api;

import laoruga.dtogenerator.api.exceptions.DtoGeneratorException;
import laoruga.dtogenerator.api.markup.generators.ICollectionGenerator;
import laoruga.dtogenerator.api.markup.generators.ICustomGeneratorDtoDependent;
import laoruga.dtogenerator.api.markup.generators.ICustomGeneratorRemarkable;
import laoruga.dtogenerator.api.markup.generators.IGenerator;
import laoruga.dtogenerator.api.markup.rules.*;
import laoruga.dtogenerator.api.util.ReflectionUtils;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.commons.math3.util.Pair;

import java.lang.annotation.Annotation;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static laoruga.dtogenerator.api.util.ReflectionUtils.createInstance;

/**
 * @author Il'dar Valitov
 * Created on 16.04.2022
 */

@Slf4j
public class DtoGenerator {

    private Object dtoInstance;

    @Getter(AccessLevel.PACKAGE)
    private final String[] fieldsFromRoot;
    @Getter(AccessLevel.PACKAGE)
    private final GeneratorBuildersProvider genBuildersProvider;
    @Getter(AccessLevel.PACKAGE)
    private final Map<Field, IGenerator<?>> fieldGeneratorMap = new LinkedHashMap<>();
    @Getter(AccessLevel.PACKAGE)
    private final FieldGroupFilter fieldsGroupFilter;
    @Getter(AccessLevel.PACKAGE)
    private final DtoGeneratorBuilder builderInstance;

    private final Map<Field, Exception> errors = new HashMap<>();

    protected DtoGenerator(String[] fieldsFromRoot,
                           GeneratorBuildersProvider generatorBuildersProvider,
                           FieldGroupFilter fieldsGroupFilter,
                           DtoGeneratorBuilder dtoGeneratorBuilder) {
        this.fieldsFromRoot = fieldsFromRoot;
        this.genBuildersProvider = generatorBuildersProvider;
        this.fieldsGroupFilter = fieldsGroupFilter;
        this.builderInstance = dtoGeneratorBuilder;
    }

    public static DtoGeneratorBuilder builder() {
        return new DtoGeneratorBuilder();
    }

    public <T> T generateDto(Class<T> dtoClass) {
        dtoInstance = createInstance(dtoClass);
        prepareGeneratorsRecursively(dtoClass);
        applyGenerators();
        return (T) dtoInstance;
    }

    public <T> T generateDto(T dtoInstance) {
        this.dtoInstance = dtoInstance;
        prepareGenerators(dtoInstance.getClass());
        applyGenerators();
        return dtoInstance;
    }

    private void prepareGeneratorsRecursively(Class<?> dtoClass) {
        if (dtoClass.getSuperclass() != null) {
            prepareGeneratorsRecursively(dtoClass.getSuperclass());
        }
        prepareGenerators(dtoClass);
    }

    /**
     * Check whether DTO is ready for using CustomGeneratorDtoDependent or not.
     * There is limited attempts to prevent infinite loops.
     *
     * @param generator   - generator to check
     * @param attempts    - attempts counter
     * @param maxAttempts - max limit
     * @return - doesn't DTO ready?
     * @throws DtoGeneratorException - throws if all attempts are spent
     */
    private boolean doesDtoDependentGeneratorNotReady(IGenerator<?> generator,
                                                      AtomicInteger attempts,
                                                      AtomicInteger maxAttempts) throws DtoGeneratorException {
        if (generator instanceof ICustomGeneratorDtoDependent) {
            if (!((ICustomGeneratorDtoDependent<?, ?>) generator).isDtoReady()) {
                if (attempts.get() < maxAttempts.get() - 1) {
                    log.debug("Object is not ready to generate dependent field value");
                    return true;
                } else {
                    throw new DtoGeneratorException("Generator " + generator.getClass() +
                            " wasn't prepared in " + attempts + " attempts");
                }
            }
        }
        return false;
    }

    void applyGenerators() {
        AtomicInteger attempts = new AtomicInteger(0);
        AtomicInteger maxAttempts = new AtomicInteger(100);
        while (!getFieldGeneratorMap().isEmpty() && attempts.get() < maxAttempts.get()) {
            attempts.incrementAndGet();
            log.debug("Attempt {} to generate field values", attempts);
            Iterator<Map.Entry<Field, IGenerator<?>>> iterator = getFieldGeneratorMap().entrySet().iterator();
            while (iterator.hasNext()) {

                Map.Entry<Field, IGenerator<?>> nextFieldAndGenerator = iterator.next();
                Field field = nextFieldAndGenerator.getKey();
                IGenerator<?> generator = nextFieldAndGenerator.getValue();
                try {
                    // if it's dto dependent generator - checking if dto is ready for this generator
                    if (generator instanceof ICustomGeneratorDtoDependent) {
                        if (doesDtoDependentGeneratorNotReady(generator, attempts, maxAttempts)) {
                            continue;
                        }
                    }
                    // if it's collection generator + dto dependent generator - checking if dto is ready for this generator
                    if (generator instanceof ICollectionGenerator) {
                        IGenerator<?> innerGenerator = ((ICollectionGenerator<?>) generator).getInnerGenerator();
                        if (innerGenerator instanceof ICustomGeneratorDtoDependent) {
                            if (doesDtoDependentGeneratorNotReady(innerGenerator, attempts, maxAttempts)) {
                                continue;
                            }
                        }
                    }
                    boolean isFieldAccessible = field.isAccessible();
                    try {
                        if (!isFieldAccessible) field.setAccessible(true);
                        field.set(dtoInstance, generator.generate());
                    } catch (IllegalAccessException e) {
                        log.error("Access error while generation value for a field: " + field, e);
                        errors.put(field, e);
                    } catch (Exception e) {
                        log.error("Error while generation value for the field: " + field, e);
                        errors.put(field, e);
                    } finally {
                        iterator.remove();
                        if (!isFieldAccessible) field.setAccessible(false);
                    }
                } catch (Exception e) {
                    log.error("Error while generation value for the field: " + field, e);
                    errors.put(field, e);
                    iterator.remove();
                }

            }
        }
        if (!getFieldGeneratorMap().isEmpty() || !errors.isEmpty()) {
            if (!getFieldGeneratorMap().isEmpty()) {
                log.error("Unexpected state. There {} unused generator(s) left. Fields vs Generators: " +
                        getFieldGeneratorMap(), getFieldGeneratorMap().size());
            }
            if (!errors.isEmpty()) {
                log.error("{} error(s) while generators execution. Fields vs Generators: " + errors, errors.size());
            }
            throw new RuntimeException("Error while generators execution (see log above)");
        }

    }

    void prepareGenerators(Class<?> dtoClass) {
        for (Field field : dtoClass.getDeclaredFields()) {
            IGenerator<?> generator = prepareGenerator(field);
            if (generator != null) {
                getFieldGeneratorMap().put(field, generator);
            }
        }
        if (!errors.isEmpty()) {
            final AtomicInteger counter = new AtomicInteger(0);
            String formattedErrors = errors.entrySet().stream()
                    .map(fieldExceptionEntry ->
                            "- [" + counter.incrementAndGet() + "] Field: '" + fieldExceptionEntry.getKey().toString() + "'\n" +
                                    "- [" + counter.get() + "] Exception:\n" +
                                    ExceptionUtils.getStackTrace(fieldExceptionEntry.getValue())
                    )
                    .collect(Collectors.joining("\n"));
            log.error("{} error(s) while generators preparation. See problems below: \n" + formattedErrors, errors.size());
            throw new DtoGeneratorException("Error while generators preparation (see log above)");
        }
        if (getFieldGeneratorMap().isEmpty()) {
            log.debug("No generators have been found");
        } else {
            log.debug(getFieldGeneratorMap().size() + " generators was created for fields: " + getFieldGeneratorMap().keySet());
        }
    }

    @Value
    static class RuleWrapper {
        Annotation collectionGenerationRules;
        Annotation itemGenerationRules;
    }

    IGenerator<?> prepareGenerator(Field field) {
        IGenerator<?> generator = null;
        try {
            Class<?> fieldType = field.getType();
            String fieldName = field.getName();
            Pair<RulesType, RuleWrapper> rulesInfo = checkAndWrapRulesInfo(field);
            RuleWrapper rulesInfoWrapper = rulesInfo.getSecond();

            switch (rulesInfo.getFirst()) {
                case BASIC:
                    generator = getGenBuildersProvider().getBasicTypeGenerator(field, rulesInfoWrapper.getItemGenerationRules());
                    break;
                case CUSTOM:
                    generator = getGenBuildersProvider().getCustomGenerator(rulesInfoWrapper.getItemGenerationRules(), dtoInstance);
                    break;
                case NESTED:
                    generator = getGenBuildersProvider().getNestedDtoGenerator(field, getFieldsFromRoot(), getBuilderInstance());
                    break;
                case COLLECTION_BASIC:
                    IGenerator<?> collectionItemGenerator = getGenBuildersProvider().getBasicTypeGenerator(
                            field.getName() + " " + field.getGenericType().getClass(),
                            ReflectionUtils.getGenericType(field),
                            rulesInfoWrapper.getItemGenerationRules());
                    generator = getGenBuildersProvider().getCollectionTypeGenerator(
                            fieldName, fieldType,
                            rulesInfoWrapper.getCollectionGenerationRules(),
                            collectionItemGenerator);
                    break;
                case COLLECTION_CUSTOM:
                    collectionItemGenerator = getGenBuildersProvider().getCustomGenerator(
                            rulesInfoWrapper.getItemGenerationRules(), dtoInstance);
                    generator = getGenBuildersProvider().getCollectionTypeGenerator(
                            fieldName, fieldType,
                            rulesInfoWrapper.getCollectionGenerationRules(),
                            collectionItemGenerator);
                    break;
                case NOT_ANNOTATED:
                case SKIP:
                    break;
                default:
                    throw new IllegalStateException("Unexpected value: " + rulesInfo);
            }
        } catch (Exception e) {
            errors.put(field, e);
        }
        prepareCustomRemarks(generator);
        return generator;
    }

    void prepareCustomRemarks(IGenerator<?> generator) {
        if (generator instanceof ICustomGeneratorRemarkable) {
            ICustomGeneratorRemarkable<?> remarkableGenerator = (ICustomGeneratorRemarkable<?>) generator;
            if (getGenBuildersProvider().getGeneratorRemarksProvider().isCustomRuleRemarkExists(remarkableGenerator)) {
                remarkableGenerator.setRuleRemarks(getGenBuildersProvider().getGeneratorRemarksProvider()
                        .getCustomRuleRemarks(remarkableGenerator));
            }
        }
    }

    enum RulesType {
        BASIC,
        CUSTOM,
        NESTED,
        COLLECTION_BASIC,
        COLLECTION_CUSTOM,
        NOT_ANNOTATED,
        SKIP
    }

    /**
     * Correctness checks and evaluation of field's annotations type.
     *
     * @param field field to check
     * @return type field's annotations
     */
    private Pair<RulesType, RuleWrapper> checkAndWrapRulesInfo(Field field) {
        Annotation[] annotations = field.getDeclaredAnnotations();
        String fieldName = field.getName();
        Class<?> fieldType = field.getType();
        if (annotations.length == 0) {
            return new Pair<>(RulesType.NOT_ANNOTATED, null);
        }
        List<Annotation> generationRules = new LinkedList<>();
        Annotation rule = null;
        Annotation rules = null;
        Annotation collectionRule = null;
        for (Annotation annotation : annotations) {
            if (isItRule(annotation)) {
                if (rule != null || rules != null) {
                    throw new DtoGeneratorException("Field '" + fieldName + "' annotated with different type rules annotations");
                }
                rule = annotation;
            } else if (isItRules(annotation)) {
                if (rule != null || rules != null) {
                    throw new DtoGeneratorException("Field '" + fieldName + "' annotated more then one rules annotation");
                }
                rules = annotation;
            } else if (isItCollectionRule(annotation)) {
                if (collectionRule != null) {
                    throw new DtoGeneratorException("Field '" + fieldName + "' annotated with more then one list rules annotations");
                }
                collectionRule = annotation;
            }
        }
        if (rule == null && rules == null) {
            if (collectionRule != null) {
                throw new DtoGeneratorException("Field '" + fieldName + "' annotated with collection generation rules, " +
                        "but not annotated with collection's item generation rules. There is also generation rules annotation expected.");

            }
            return new Pair<>(RulesType.NOT_ANNOTATED, null);
        }
        if (rules != null) {
            rule = getRuleByGroupOrNull(rules, fieldName);
            if (rule == null) {
                return new Pair<>(RulesType.SKIP, null);
            }
        }
        if (collectionRule == null) {
            if (skipDependingOnGroup(rule)) {
                return new Pair<>(RulesType.SKIP, null);
            }
            if (isItCustomRule(rule)) {
                return new Pair<>(RulesType.CUSTOM, new RuleWrapper(null, rule));
            }
            if (isItNestedRule(rule)) {
                return new Pair<>(RulesType.NESTED, new RuleWrapper(null, rule));
            }
            if (!checkGeneratorCompatibility(fieldType, rule)) {
                throw new DtoGeneratorException("Field '" + fieldName + "' annotated with inappropriate generation " +
                        "rule annotation: '" + rule.annotationType() + "'.");
            }
            return new Pair<>(RulesType.BASIC, new RuleWrapper(null, rule));
        } else {
            // TODO add group for collection
            if (skipDependingOnGroup(rule)) {
                return new Pair<>(RulesType.SKIP, null);
            }
            if (isItCustomRule(rule)) {
                return new Pair<>(RulesType.COLLECTION_CUSTOM, new RuleWrapper(collectionRule, rule));
            } else {
                return new Pair<>(RulesType.COLLECTION_BASIC, new RuleWrapper(collectionRule, rule));
            }
        }
    }

    private static boolean checkGeneratorCompatibility(Class<?> fieldType, Annotation rules) {
        try {
            Class<? extends Annotation> rulesAnnotationClass = rules.annotationType();
            Class<?>[] applicableTypes = (Class<?>[]) rulesAnnotationClass.getField("APPLICABLE_TYPES")
                    .get(rulesAnnotationClass);
            for (Class<?> applicableType : applicableTypes) {
                if (applicableType == fieldType ||
                        fieldType.isAssignableFrom(applicableType) || applicableType.isAssignableFrom(fieldType)) {
                    return true;
                }
            }
        } catch (NoSuchFieldException | IllegalAccessException | ClassCastException e) {
            throw new DtoGeneratorException("Unexpected exception. Can't get APPLICABLE_TYPES from rules annotation", e);
        }
        return false;
    }

    private static boolean isItCollectionRule(Annotation ruleAnnotation) {
        return ruleAnnotation.annotationType().getDeclaredAnnotation(RuleForCollection.class) != null;
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

    private boolean skipDependingOnGroup(Annotation rules) {
        if (this.getFieldsGroupFilter() == null) {
            return false;
        } else {
            try {
                String checkedGroup = (String) rules.annotationType().getMethod("group").invoke(rules);
                return !getFieldsGroupFilter().isContainsIncludeGroup(checkedGroup);
            } catch (IllegalAccessException | ClassCastException | NoSuchMethodException | InvocationTargetException e) {
                throw new DtoGeneratorException("Unexpected exception. Can't get 'group' from rules annotation", e);
            }
        }
    }

    private Annotation getRuleByGroupOrNull(Annotation rules, String fieldName) {
        try {
            LinkedList<Object> uniqueGroups = new LinkedList<>();
            Object ruleAnnotationsArray = rules.getClass().getMethod("value").invoke(rules);
            int length = Array.getLength(ruleAnnotationsArray);
            Annotation matched = null;
            for (int i = 0; i < length; i++) {
                Annotation rule = (Annotation) Array.get(ruleAnnotationsArray, i);
                String checkedGroup = (String) rule.annotationType().getMethod("group").invoke(rule);
                if (uniqueGroups.contains(rule)) {
                    throw new DtoGeneratorException("Rule group '" + checkedGroup + "' is repeating for field.");
                } else {
                    uniqueGroups.add(rule);
                }
                if (getFieldsGroupFilter().isContainsIncludeGroup(checkedGroup)) {
                    if (matched == null) {
                        matched = rule;
                    } else {
                        throw new DtoGeneratorException("Ambiguous grouping of the field: '" + fieldName + "'." +
                                " Check groups of generators and include filters.");
                    }
                }
            }
            return matched;
        } catch (IllegalAccessException | ClassCastException | NoSuchMethodException | InvocationTargetException e) {
            throw new DtoGeneratorException("Unexpected exception. Can't get 'group' from rules annotation", e);
        }

    }

}
