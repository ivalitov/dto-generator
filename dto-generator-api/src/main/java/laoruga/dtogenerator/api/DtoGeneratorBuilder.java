package laoruga.dtogenerator.api;

import laoruga.dtogenerator.api.constants.BasicRuleRemark;
import laoruga.dtogenerator.api.constants.Group;
import laoruga.dtogenerator.api.exceptions.DtoGeneratorException;
import laoruga.dtogenerator.api.markup.generators.IGeneratorBuilder;
import laoruga.dtogenerator.api.markup.remarks.CustomRuleRemarkWrapper;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.apache.commons.math3.util.Pair;

import java.lang.annotation.Annotation;
import java.util.*;

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
public class DtoGeneratorBuilder {

    private final GeneratorBuildersProvider gensBuildersProvider;
    private final BuildersTree buildersTree;
    private final FieldGroupFilter fieldGroupFilter;

    /**
     * key - field name;
     * if key == null - rule remark is passing to all basic fields generators;
     * if key != null - rule remarks is passing to field with this name.
     */

    DtoGeneratorBuilder() {
        this.gensBuildersProvider = new GeneratorBuildersProvider(new GeneratorRemarksProvider());
        this.buildersTree = new BuildersTree(this);
        this.fieldGroupFilter = new FieldGroupFilter();
    }

    private DtoGeneratorBuilder(DtoGeneratorBuilder toCopy) {
        this.gensBuildersProvider = new GeneratorBuildersProvider(
                toCopy.gensBuildersProvider.getGeneratorRemarksProvider().copy(),
                toCopy.gensBuildersProvider.getOverriddenBuilders());
        this.buildersTree = toCopy.buildersTree;
        this.fieldGroupFilter = toCopy.fieldGroupFilter;
    }

    public DtoGeneratorBuilder overrideBasicGenerator(@NonNull Class<? extends Annotation> rules,
                                                      @NonNull IGeneratorBuilder newGeneratorBuilder) throws DtoGeneratorException {
        gensBuildersProvider.overrideGenerator(rules, newGeneratorBuilder);
        return this;
    }

    public DtoGeneratorBuilder setGeneratorForField(@NonNull String fieldName,
                                                    @NonNull IGeneratorBuilder explicitGenerator) throws DtoGeneratorException {
        Pair<String, String[]> fieldAndPath = splitPathToField(fieldName);
        DtoGeneratorBuilder builder = getBuilderFromTreeOrThis(fieldAndPath.getSecond());
        builder.gensBuildersProvider.setGeneratorForFields(fieldAndPath.getFirst(), explicitGenerator);
        return this;
    }

    /*
     * Basic Rule Remarks
     */

    public DtoGeneratorBuilder setRuleRemarkForField(@NonNull String fieldName,
                                                     @NonNull BasicRuleRemark ruleRemark) throws DtoGeneratorException {
        Pair<String, String[]> fieldAndPath = splitPathToField(fieldName);
        DtoGeneratorBuilder fieldAndBuilder = getBuilderFromTreeOrThis(fieldAndPath.getSecond());
        fieldAndBuilder.gensBuildersProvider.getGeneratorRemarksProvider().setBasicRuleRemarkForField(
                fieldAndPath.getFirst(), ruleRemark);
        return this;
    }

    public DtoGeneratorBuilder setRuleRemarkForFields(@NonNull BasicRuleRemark basicRuleRemark) throws DtoGeneratorException {
        gensBuildersProvider.getGeneratorRemarksProvider().setBasicRuleRemarkForFields(basicRuleRemark);
        return this;
    }

    /*
     * Custom Rule Remarks
     */

    public DtoGeneratorBuilder addRuleRemarkForField(@NonNull String fieldName,
                                                     @NonNull CustomRuleRemarkWrapper... ruleRemark) {
        Pair<String, String[]> fieldAndPath = splitPathToField(fieldName);
        DtoGeneratorBuilder fieldAndBuilder = getBuilderFromTreeOrThis(fieldAndPath.getSecond());
        fieldAndBuilder.gensBuildersProvider.getGeneratorRemarksProvider().addCustomRuleRemarkForField(
                fieldAndPath.getFirst(), ruleRemark);
        return this;
    }

    public DtoGeneratorBuilder addRuleRemarkForFields(@NonNull CustomRuleRemarkWrapper... ruleRemarks) {
        gensBuildersProvider.getGeneratorRemarksProvider().addRuleRemarkForAllFields(ruleRemarks);
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
    public DtoGeneratorBuilder includeGroups(String... groups) {
        if (groups != null && groups.length != 0) {
            fieldGroupFilter.includeGroups(groups);
        }
        return this;
    }

    /*
     * Build
     */

    public DtoGenerator build() {
        return new DtoGenerator(
                new String[]{BuildersTree.ROOT},
                gensBuildersProvider,
                fieldGroupFilter.validateGroups(),
                this);
    }

    private DtoGenerator build(String[] pathToField) {
        return new DtoGenerator(
                pathToField,
                gensBuildersProvider,
                fieldGroupFilter.validateGroups(),
                this);
    }

    DtoGenerator buildNestedFieldGenerator(String[] pathToNestedDtoField) {
        DtoGeneratorBuilder builderFromTreeOrThis = getBuilderFromTreeOrThis(pathToNestedDtoField);
        return builderFromTreeOrThis.build(pathToNestedDtoField);
    }

    private DtoGeneratorBuilder getBuilderFromTreeOrThis(String[] pathToField) {
        if (pathToField != null) {
            return buildersTree.getBuilder(pathToField);
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

    @RequiredArgsConstructor
    static class BuildersTree {

        public static final String ROOT = "%ROOT%";

        private final Node tree;

        public BuildersTree(DtoGeneratorBuilder rootBuilder) {
            this.tree = new Node(ROOT, rootBuilder);
        }

        DtoGeneratorBuilder getBuilder(String[] fields) {
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
                        next = new Node(field, new DtoGeneratorBuilder(tree.getBuilder()));
                        prev.getChildren().add(next);
                    }
                }
            }
            return Objects.requireNonNull(next).getBuilder();
        }

        @Getter
        @Setter
        @RequiredArgsConstructor
        static class Node {
            private final String fieldName;
            private final DtoGeneratorBuilder builder;
            private List<Node> children = new LinkedList<>();
        }
    }
}
