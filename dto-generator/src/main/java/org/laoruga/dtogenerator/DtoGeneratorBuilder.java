package org.laoruga.dtogenerator;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.apache.commons.math3.util.Pair;
import org.laoruga.dtogenerator.api.generators.IGenerator;
import org.laoruga.dtogenerator.api.generators.IGeneratorBuilder;
import org.laoruga.dtogenerator.api.remarks.CustomRuleRemarkWrapper;
import org.laoruga.dtogenerator.constants.BasicRuleRemark;
import org.laoruga.dtogenerator.exceptions.DtoGeneratorException;

import java.lang.annotation.Annotation;
import java.util.*;

import static org.laoruga.dtogenerator.util.ReflectionUtils.createInstance;

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
 *
 * @author Il'dar Valitov
 * Created on 16.04.2022
 */
public class DtoGeneratorBuilder<T> {

    private final GeneratorsProvider<T> generatorsProvider;
    private final GeneratorBuildersTree generatorBuildersTree;
    private final FieldGroupFilter fieldGroupFilter;

    DtoGeneratorBuilder(T dtoInstance) {
        this.fieldGroupFilter = new FieldGroupFilter();
        this.generatorBuildersTree = new GeneratorBuildersTree(this);
        this.generatorsProvider = new GeneratorsProvider<>(
                new GeneratorRemarksProvider(),
                fieldGroupFilter,
                new String[]{GeneratorBuildersTree.ROOT},
                generatorBuildersTree);
        this.generatorsProvider.setDtoInstance(dtoInstance);
    }

    /**
     * Constructor to copy builder for creating Builder for nested DTOs generating.
     *
     * @param toCopy         from
     * @param fieldsFromRoot - path to nested DTO field
     */
    private DtoGeneratorBuilder(DtoGeneratorBuilder<T> toCopy, String[] fieldsFromRoot) {
        this.generatorsProvider = new GeneratorsProvider<>(toCopy.generatorsProvider, fieldsFromRoot);
        this.generatorBuildersTree = toCopy.generatorBuildersTree;
        this.fieldGroupFilter = toCopy.fieldGroupFilter;
    }

    /**
     * @param rules - not collection only
     * @param generatorBuilder - builder of not collection type
     */
    public DtoGeneratorBuilder<T> setGenerator(@NonNull Class<? extends Annotation> rules,
                                               @NonNull IGeneratorBuilder<IGenerator<?>> generatorBuilder) throws DtoGeneratorException {
        generatorsProvider.overrideGenerator(rules, generatorBuilder);
        return this;
    }

    /**
     * @param fieldName name of field to generate value
     * @param generatorBuilder builder of generator of any type
     */
    public DtoGeneratorBuilder<T> setGeneratorForField(@NonNull String fieldName,
                                                       @NonNull IGeneratorBuilder<IGenerator<?>> generatorBuilder) throws DtoGeneratorException {
        Pair<String, String[]> fieldAndPath = splitPathToField(fieldName);
        DtoGeneratorBuilder<?> dtoGeneratorBuilder = getBuilderFromTreeOrThis(fieldAndPath.getSecond());
        dtoGeneratorBuilder.generatorsProvider.setGeneratorForField(fieldAndPath.getFirst(), generatorBuilder);
        return this;
    }

    /*
     * Basic Rule Remarks
     */

    public DtoGeneratorBuilder<T> setRuleRemarkForField(@NonNull String fieldName,
                                                        @NonNull BasicRuleRemark ruleRemark) throws DtoGeneratorException {
        Pair<String, String[]> fieldAndPath = splitPathToField(fieldName);
        DtoGeneratorBuilder<?> fieldAndBuilder = getBuilderFromTreeOrThis(fieldAndPath.getSecond());
        fieldAndBuilder.generatorsProvider.getGeneratorRemarksProvider().setBasicRuleRemarkForField(
                fieldAndPath.getFirst(), ruleRemark);
        return this;
    }

    public DtoGeneratorBuilder<T> setRuleRemarkForFields(@NonNull BasicRuleRemark basicRuleRemark) throws DtoGeneratorException {
        this.generatorsProvider.getGeneratorRemarksProvider().setBasicRuleRemarkForFields(basicRuleRemark);
        return this;
    }

    /*
     * Custom Rule Remarks
     */

    public DtoGeneratorBuilder<T> addRuleRemarkForField(@NonNull String fieldName,
                                                        @NonNull CustomRuleRemarkWrapper... ruleRemark) {
        Pair<String, String[]> fieldAndPath = splitPathToField(fieldName);
        DtoGeneratorBuilder<?> fieldAndBuilder = getBuilderFromTreeOrThis(fieldAndPath.getSecond());
        fieldAndBuilder.generatorsProvider.getGeneratorRemarksProvider().addCustomRuleRemarkForField(
                fieldAndPath.getFirst(), ruleRemark);
        return this;
    }

    public DtoGeneratorBuilder<T> addRuleRemarkForFields(@NonNull CustomRuleRemarkWrapper... ruleRemarks) {
        this.generatorsProvider.getGeneratorRemarksProvider().addRuleRemarkForAllFields(ruleRemarks);
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
            this.generatorsProvider.getRulesInfoExtractor().getFieldsGroupFilter().includeGroups(groups);
        }
        return this;
    }

    /*
     * Build
     */

    /**
     * @return dto builder instance
     */
    public DtoGenerator<T> build() {
        return new DtoGenerator<>(generatorsProvider, this);
    }

    DtoGenerator<?> buildNestedFieldGenerator(String[] pathToNestedDtoField, Class<?> generatedType) {
        DtoGeneratorBuilder<?> nestedDtoGenBuilder = getBuilderFromTreeOrThis(pathToNestedDtoField);
        nestedDtoGenBuilder.generatorsProvider.setDtoInstance(createInstance(generatedType));
        return nestedDtoGenBuilder.build();
    }

    private DtoGeneratorBuilder<?> getBuilderFromTreeOrThis(String[] pathToField) {
        if (pathToField != null) {
            return generatorBuildersTree.getBuilder(pathToField);
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

    /**
     * Tree of generator builders when nested DTOs exist.
     */
    @RequiredArgsConstructor
    static class GeneratorBuildersTree {

        public static final String ROOT = "%ROOT%";

        private final Node tree;

        public GeneratorBuildersTree(DtoGeneratorBuilder<?> rootBuilder) {
            this.tree = new Node(ROOT, rootBuilder);
        }

        DtoGeneratorBuilder<?> getBuilder(String[] fields) {
            Node prev = tree;
            Node next = null;
            for (String field : fields) {
                if (!ROOT.equals(field)) {
                    Optional<Node> maybeNode = prev.getChildren().stream()
                            .filter(node -> field.equals(node.getFieldName()))
                            .findFirst();
                    if (maybeNode.isPresent()) {
                        next = maybeNode.get();
                    } else {
                        next = new Node(field, new DtoGeneratorBuilder<>(tree.getBuilder(), fields));
                        prev.getChildren().add(next);
                    }
                }
            }
            return Objects.requireNonNull(next, "Unexpected error").getBuilder();
        }

        @Getter
        @Setter
        @RequiredArgsConstructor
        static class Node {
            private final String fieldName;
            private final DtoGeneratorBuilder<?> builder;
            private List<Node> children = new LinkedList<>();
        }
    }
}
