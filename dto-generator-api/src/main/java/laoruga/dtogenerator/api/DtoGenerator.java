package laoruga.dtogenerator.api;

import laoruga.dtogenerator.api.exceptions.DtoGeneratorException;
import laoruga.dtogenerator.api.markup.generators.ICollectionGenerator;
import laoruga.dtogenerator.api.markup.generators.ICustomGeneratorDtoDependent;
import laoruga.dtogenerator.api.markup.generators.ICustomGeneratorRemarkable;
import laoruga.dtogenerator.api.markup.generators.IGenerator;
import laoruga.dtogenerator.api.markup.rules.*;
import laoruga.dtogenerator.api.util.ReflectionUtils;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.NotImplementedException;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.commons.math3.util.Pair;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * @author Il'dar Valitov
 * Created on 16.04.2022
 */

@Slf4j
public class DtoGenerator {

    private Object dtoInstance;

    private final String[] fieldsFromRoot;
    private final GeneratorBuildersProvider generatorBuildersProvider;

    private final Map<Field, Exception> errors = new HashMap<>();
    private final Map<Field, IGenerator<?>> fieldGeneratorMap = new LinkedHashMap<>();

    private final DtoGeneratorBuilder builderInstance;

    protected DtoGenerator(String[] fieldsFromRoot,
                           GeneratorBuildersProvider generatorBuildersProvider,
                           DtoGeneratorBuilder dtoGeneratorBuilder) {
        this.fieldsFromRoot = fieldsFromRoot;
        this.generatorBuildersProvider = generatorBuildersProvider;
        this.builderInstance = dtoGeneratorBuilder;
    }

    public static DtoGeneratorBuilder builder() {
        return new DtoGeneratorBuilder();
    }

    GeneratorBuildersProvider getGenBuildersProvider() {
        return generatorBuildersProvider;
    }

    Map<Field, IGenerator<?>> getFieldGeneratorMap() {
        return fieldGeneratorMap;
    }

    public <T> T generateDto(Class<T> dtoClass) {
        createDtoInstance(dtoClass);
        prepareGenerators();
        applyGenerators();
        return (T) dtoInstance;
    }

    public <T> T generateDto(T dtoInstance) {
        this.dtoInstance = dtoInstance;
        prepareGenerators();
        applyGenerators();
        return dtoInstance;
    }

    void createDtoInstance(Class<?> dtoClass) {
        try {
            Constructor<?>[] declaredConstructors = dtoClass.getDeclaredConstructors();
            if (declaredConstructors.length == 0) {
                throw new DtoGeneratorException("Failed to instantiate DTO class: '" + dtoClass + "'. " +
                        "Class don't have public constructors. It must have public no-args constructor.");
            }
            Optional<Constructor<?>> maybeNoArgsConstructor = Arrays.stream(declaredConstructors)
                    .filter(constructor -> constructor.getParameterCount() == 0)
                    .findAny();
            if (!maybeNoArgsConstructor.isPresent()) {
                throw new DtoGeneratorException("Failed to instantiate DTO class: '" + dtoClass + "'. " +
                        "Class must have public no-args constructor.");
            }
            Constructor<?> constructor = maybeNoArgsConstructor.get();
            boolean isAccessible = constructor.isAccessible();
            constructor.setAccessible(true);
            dtoInstance = constructor.newInstance();
            constructor.setAccessible(isAccessible);
        } catch (InstantiationException ie) {
            throw new DtoGeneratorException("Failed to instantiate DTO class: '" + dtoClass + "'. " +
                    "Maybe no-args constructor was not found.", ie);
        } catch (Exception e) {
            throw new DtoGeneratorException("Failed to instantiate DTO class: '" + dtoClass + "'.", e);
        }
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

    void prepareGenerators() {
        for (Field field : dtoInstance.getClass().getDeclaredFields()) {
            IGenerator<?> generator = prepareGenerator(field);
            if (generator != null) {
                getFieldGeneratorMap().put(field, generator);
            }
        }
        if (!errors.isEmpty()) {
            int i = 0;
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
                    generator = getGenBuildersProvider().getNestedDtoGenerator(field, fieldsFromRoot, builderInstance);
                    break;
                case COLLECTION_BASIC:
                    IGenerator<?> collectionItemGenerator = getGenBuildersProvider().getBasicTypeGenerator(
                            field.getName() + " " + field.getGenericType().getClass(),
                            ReflectionUtils.getGenericType(field),
                            rulesInfoWrapper.getItemGenerationRules());
                    generator = getGenBuildersProvider().getListGenerator(
                            fieldName, fieldType,
                            (ListRules) rulesInfoWrapper.getCollectionGenerationRules(),
                            collectionItemGenerator);
                    break;
                case MAP_BASIC:
                    throw new NotImplementedException();
//                    Pair<Class<?>, Class<?>> genericTypesPair = ReflectionUtils.getGenericTypesPair(field);
//                    IGenerator<?> mapKeyGenerator = getGeneratorBuildersProvider().getBasicTypeGenerator(
//                            field.getName() + " " + field.getGenericType().getClass(),
//                            genericTypesPair.getKey(),
//                            field.getDeclaredAnnotations()
//                    );
//                    IGenerator<?> mapValueGenerator = selectBasicGenerator(
//                            genericTypesPair.getValue(),
//                            field.getName() + " " + field.getGenericType().getClass(),
//                            field.getDeclaredAnnotations()
//                    );
//                    generator = selectMapGenerator(field, mapKeyGenerator, mapValueGenerator);
                case COLLECTION_CUSTOM:
                    collectionItemGenerator = getGenBuildersProvider().getCustomGenerator(
                            rulesInfoWrapper.getItemGenerationRules(), dtoInstance);
                    generator = getGenBuildersProvider().getListGenerator(
                            fieldName, fieldType,
                            (ListRules) rulesInfoWrapper.getCollectionGenerationRules(),
                            collectionItemGenerator);
                    break;
                case NOT_ANNOTATED:
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
        MAP_BASIC,
        NOT_ANNOTATED
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
        List<Annotation> generationRules = Arrays.stream(annotations)
                .filter(annotation -> annotation.annotationType().getDeclaredAnnotation(Rule.class) != null)
                .collect(Collectors.toList());
        if (generationRules.isEmpty()) {
            return new Pair<>(RulesType.NOT_ANNOTATED, null);
        }
        if (generationRules.size() == 1) {
            Annotation ruleAnnotation = generationRules.get(0);
            Class<? extends Annotation> rulesAnnotationClass = ruleAnnotation.annotationType();
            if (isItCustomRule(ruleAnnotation)) {
                return new Pair<>(RulesType.CUSTOM, new RuleWrapper(null, ruleAnnotation));
            }
            if (isItNestedRule(ruleAnnotation)) {
                return new Pair<>(RulesType.NESTED, new RuleWrapper(null, ruleAnnotation));
            }
            if (isItCollectionRule(ruleAnnotation)) {
                throw new DtoGeneratorException("Field '" + fieldName + "' annotated with collection generation rules, " +
                        "but not annotated with collection's item generation rules. There is also generation rules annotation expected.");
            }
            if (!checkGeneratorCompatibility(fieldType, rulesAnnotationClass)) {
                throw new DtoGeneratorException("Field '" + fieldName + "' annotated with inappropriate generation " +
                        "rule annotation: '" + rulesAnnotationClass + "'.");
            }
            return new Pair<>(RulesType.BASIC, new RuleWrapper(null, ruleAnnotation));
        }
        if (generationRules.size() == 2) {
            generationRules.sort(Comparator.comparing(DtoGenerator::isItCollectionRule));
            Annotation itemRule = generationRules.get(0);
            Annotation collectionRule = generationRules.get(1);
            if (isItCollectionRule(itemRule)) {
                throw new DtoGeneratorException("Field '" + fieldName + "'annotated with 2 collection generation rules." +
                        " There are no more than one RuleForCollection annotation expected");
            }
            if (!isItCollectionRule(collectionRule)) {
                throw new DtoGeneratorException("Field '" + fieldName + "'annotated with 2 generation rules " +
                        " non of which is RuleForCollection annotation.");
            }
            if (isItCustomRule(itemRule)) {
                return new Pair<>(RulesType.COLLECTION_CUSTOM, new RuleWrapper(collectionRule, itemRule));
            } else {
                return new Pair<>(RulesType.COLLECTION_BASIC, new RuleWrapper(collectionRule, itemRule));
            }

        } else {
            throw new DtoGeneratorException("Field '" + fieldName + "'  annotated with '" + generationRules.size() +
                    "' generation rules" +
                    " annotations. No more than '2' expected.");
        }
    }

    private boolean checkGeneratorCompatibility(Class<?> fieldType, Class<? extends Annotation> rulesAnnotationClass) {
        try {
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

}
