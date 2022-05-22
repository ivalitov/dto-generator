package laoruga.dtogenerator.api;

import laoruga.dtogenerator.api.exceptions.DtoGeneratorException;
import laoruga.dtogenerator.api.markup.generators.IGeneratorBuilder;
import laoruga.dtogenerator.api.markup.remarks.BasicRuleRemark;
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
 */
public class DtoGeneratorBuilder {

    private final GeneratorBuildersProvider gensBuildersProvider;
    private final BuildersTree buildersTree;

    /**
     * key - field name;
     * if key == null - rule remark is passing to all basic fields generators;
     * if key != null - rule remarks is passing to field with this name.
     */

    DtoGeneratorBuilder() {
        this.gensBuildersProvider = new GeneratorBuildersProvider(new GeneratorRemarksProvider());
        this.buildersTree = new BuildersTree(this);
    }

    private DtoGeneratorBuilder(DtoGeneratorBuilder toCopy) {
        GeneratorRemarksProvider generatorRemarksProvider = new GeneratorRemarksProvider(
                toCopy.gensBuildersProvider.getGeneratorRemarksProvider().getCustomRuleRemarksMap());
        this.gensBuildersProvider = new GeneratorBuildersProvider(
                generatorRemarksProvider, toCopy.gensBuildersProvider.getOverriddenBuilders());
        this.buildersTree = null;
    }

    public DtoGeneratorBuilder overrideBasicGenerator(@NonNull Class<? extends Annotation> rules,
                                                      @NonNull IGeneratorBuilder newGeneratorBuilder) throws DtoGeneratorException {
        gensBuildersProvider.overrideGenerator(rules, newGeneratorBuilder);
        return this;
    }

    public DtoGeneratorBuilder setGeneratorForField(@NonNull String fieldName,
                                                    @NonNull IGeneratorBuilder explicitGenerator) throws DtoGeneratorException {
        Pair<String, DtoGeneratorBuilder> fieldAndBuilder = getBuilderFromTreeOrThis(fieldName);
        fieldAndBuilder.getSecond().gensBuildersProvider.setGeneratorForFields(
                fieldAndBuilder.getFirst(), explicitGenerator);
        return this;
    }

    /*
     * Basic Rule Remarks
     */

    public DtoGeneratorBuilder setRuleRemarkForField(@NonNull String filedName,
                                                     @NonNull BasicRuleRemark ruleRemark) throws DtoGeneratorException {
        gensBuildersProvider.getGeneratorRemarksProvider().setBasicRuleRemarkForField(filedName, ruleRemark);
        return this;
    }

    public DtoGeneratorBuilder setRuleRemarkForFields(@NonNull BasicRuleRemark basicRuleRemark) throws DtoGeneratorException {
        gensBuildersProvider.getGeneratorRemarksProvider().setBasicRuleRemarkForField(null, basicRuleRemark);
        return this;
    }

    /*
     * Custom Rule Remarks
     */

    public DtoGeneratorBuilder addRuleRemarkForField(@NonNull String fieldName,
                                                     @NonNull CustomRuleRemarkWrapper... ruleRemark) {
        Pair<String, DtoGeneratorBuilder> fieldAndBuilder = getBuilderFromTreeOrThis(fieldName);
        fieldAndBuilder.getSecond().gensBuildersProvider.getGeneratorRemarksProvider().addCustomRuleRemarkForField(
                fieldAndBuilder.getFirst(), ruleRemark);
        return this;
    }

    public DtoGeneratorBuilder addRuleRemarkForFields(@NonNull CustomRuleRemarkWrapper... ruleRemarks) {
        gensBuildersProvider.getGeneratorRemarksProvider().addRuleRemarkForAllFields(ruleRemarks);
        return this;
    }

    public DtoGenerator build() {
        return new DtoGenerator(
                gensBuildersProvider,
                this);
    }

    private Pair<String, DtoGeneratorBuilder> getBuilderFromTreeOrThis(String fieldName) {
        if (fieldName.contains(".")) {
            String[] fieldsSequence = fieldName.split("//.");
            fieldName = fieldsSequence[fieldsSequence.length - 1];
            fieldsSequence = Arrays.copyOf(fieldsSequence, fieldsSequence.length - 1);
            return Pair.create(fieldName, buildersTree.getBuilder(fieldsSequence));
        }  else {
            return Pair.create(fieldName, this);
        }
    }

    @RequiredArgsConstructor
    static class BuildersTree {

        private final Node tree;

        public BuildersTree(DtoGeneratorBuilder rootBuilder) {
            this.tree = new Node("root", rootBuilder);
        }

        DtoGeneratorBuilder getBuilder(String[] fields) {
            Node prev = tree;
            Node next = null;
            for (String field : fields) {
                next = null;
                Optional<Node> maybeNode = prev.getChildren().stream()
                        .filter(node -> field.equals(node.getFieldName()))
                        .findFirst();
                next = maybeNode.orElseGet(() -> new Node(field, new DtoGeneratorBuilder(tree.getBuilder())));
                prev.getChildren().add(next);
            }
            return Objects.requireNonNull(next).getBuilder();
        }
    }

    @Getter
    @Setter
    @RequiredArgsConstructor
    static class Node {
        private final String fieldName;
        private final DtoGeneratorBuilder builder;
        private List<Node> children = new LinkedList<>();
    }

    /*
      Страна
         Город 1
            Магазин 1
              Соль
              Сахар
         Город 2
            Магазин 2
              Лук
              Чеснок
     1) если есть точка (точки) - добавить в дерево узел (узлы) содержащий:
     - имя поля для билдера
     - ремарки для поля
     - ремарки для всех полей
     - сам билдер
            -- каждый бидер надо дополлнять ремарками для всех полей (можно предеать ссылку на одну и ту же MAP)
            -- билдеры надо дополнять ремарками полей



     */
}
