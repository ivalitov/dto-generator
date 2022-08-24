package laoruga.dtogenerator.api;

import laoruga.dtogenerator.api.constants.BasicRuleRemark;
import laoruga.dtogenerator.api.constants.Group;
import laoruga.dtogenerator.api.exceptions.DtoGeneratorException;
import laoruga.dtogenerator.api.markup.generators.IGenerator;
import laoruga.dtogenerator.api.markup.generators.IGeneratorBuilder;
import laoruga.dtogenerator.api.markup.remarks.CustomRuleRemarkWrapper;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.apache.commons.math3.util.Pair;

import java.lang.annotation.Annotation;
import java.util.*;

import static laoruga.dtogenerator.api.util.ReflectionUtils.createInstance;

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
public class DtoGeneratorBuilder<DTO_TYPE> {

    private final TypeGeneratorsProvider<DTO_TYPE> typeGeneratorsProvider;
    private final GeneratorBuildersTree generatorBuildersTree;
    private final FieldGroupFilter fieldGroupFilter;

    /**
     * key - field name;
     * if key == null - rule remark is passing to all basic fields generators;
     * if key != null - rule remarks is passing to field with this name.
     */

//    DtoGeneratorBuilder() {
//        this(null);
//    }

    DtoGeneratorBuilder(DTO_TYPE dtoInstance) {
        this.fieldGroupFilter = new FieldGroupFilter();
        this.generatorBuildersTree = new GeneratorBuildersTree(this);
        this.typeGeneratorsProvider = new TypeGeneratorsProvider<DTO_TYPE>(
                new GeneratorRemarksProvider(),
                fieldGroupFilter,
                new String[]{GeneratorBuildersTree.ROOT},
                generatorBuildersTree);
        this.typeGeneratorsProvider.setDtoInstance(dtoInstance);
    }

    /**
     * Constructor to copy builder for creating Builder for nested DTOs generating.
     *
     * @param toCopy         from
     * @param fieldsFromRoot - path to nested DTO field
     */
    private DtoGeneratorBuilder(DtoGeneratorBuilder<?> toCopy, String[] fieldsFromRoot) {
        this.typeGeneratorsProvider = new TypeGeneratorsProvider<>(toCopy.typeGeneratorsProvider, fieldsFromRoot);
        this.generatorBuildersTree = toCopy.generatorBuildersTree;
        this.fieldGroupFilter = toCopy.fieldGroupFilter;
    }

    /**
     * @param rules - not collection only
     * @param generatorBuilder - builder of not collection type
     */
    public DtoGeneratorBuilder<DTO_TYPE> setGenerator(@NonNull Class<? extends Annotation> rules,
                                                      @NonNull IGeneratorBuilder<IGenerator<?>> generatorBuilder) throws DtoGeneratorException {
        typeGeneratorsProvider.overrideGenerator(rules, generatorBuilder);
        return this;
    }

    /**
     * @param fieldName name of field to generate value
     * @param generatorBuilder builder of generator of any type
     */
    public DtoGeneratorBuilder<DTO_TYPE> setGeneratorForField(@NonNull String fieldName,
                                                              @NonNull IGeneratorBuilder<IGenerator<?>> generatorBuilder) throws DtoGeneratorException {
        Pair<String, String[]> fieldAndPath = splitPathToField(fieldName);
        DtoGeneratorBuilder<?> dtoGeneratorBuilder = getBuilderFromTreeOrThis(fieldAndPath.getSecond());
        dtoGeneratorBuilder.typeGeneratorsProvider.setGeneratorForField(fieldAndPath.getFirst(), generatorBuilder);
        return this;
    }

    /*
     * Basic Rule Remarks
     */

    public DtoGeneratorBuilder<DTO_TYPE> setRuleRemarkForField(@NonNull String fieldName,
                                                               @NonNull BasicRuleRemark ruleRemark) throws DtoGeneratorException {
        Pair<String, String[]> fieldAndPath = splitPathToField(fieldName);
        DtoGeneratorBuilder<?> fieldAndBuilder = getBuilderFromTreeOrThis(fieldAndPath.getSecond());
        fieldAndBuilder.typeGeneratorsProvider.getGeneratorRemarksProvider().setBasicRuleRemarkForField(
                fieldAndPath.getFirst(), ruleRemark);
        return this;
    }

    public DtoGeneratorBuilder<DTO_TYPE> setRuleRemarkForFields(@NonNull BasicRuleRemark basicRuleRemark) throws DtoGeneratorException {
        this.typeGeneratorsProvider.getGeneratorRemarksProvider().setBasicRuleRemarkForFields(basicRuleRemark);
        return this;
    }

    /*
     * Custom Rule Remarks
     */

    public DtoGeneratorBuilder<DTO_TYPE> addRuleRemarkForField(@NonNull String fieldName,
                                                               @NonNull CustomRuleRemarkWrapper... ruleRemark) {
        Pair<String, String[]> fieldAndPath = splitPathToField(fieldName);
        DtoGeneratorBuilder<?> fieldAndBuilder = getBuilderFromTreeOrThis(fieldAndPath.getSecond());
        fieldAndBuilder.typeGeneratorsProvider.getGeneratorRemarksProvider().addCustomRuleRemarkForField(
                fieldAndPath.getFirst(), ruleRemark);
        return this;
    }

    public DtoGeneratorBuilder<DTO_TYPE> addRuleRemarkForFields(@NonNull CustomRuleRemarkWrapper... ruleRemarks) {
        this.typeGeneratorsProvider.getGeneratorRemarksProvider().addRuleRemarkForAllFields(ruleRemarks);
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
    public DtoGeneratorBuilder<DTO_TYPE> includeGroups(String... groups) {
        if (groups != null && groups.length != 0) {
            this.typeGeneratorsProvider.getRulesInfoExtractor().getFieldsGroupFilter().includeGroups(groups);
        }
        return this;
    }

    /*
     * Build
     */

    /**
     * @return dto builder instance
     */
    public DtoGenerator<DTO_TYPE> build() {
        if (fieldGroupFilter.getGroupsCount() == 0) {
            fieldGroupFilter.includeGroups(Group.DEFAULT);
        }
        return new DtoGenerator<DTO_TYPE>(typeGeneratorsProvider, this);
    }

    DtoGenerator<?> buildNestedFieldGenerator(String[] pathToNestedDtoField, Class<?> generatedType) {
        DtoGeneratorBuilder<?> nestedDtoGenBuilder = getBuilderFromTreeOrThis(pathToNestedDtoField);
        nestedDtoGenBuilder.typeGeneratorsProvider.setDtoInstance(createInstance(generatedType));
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
