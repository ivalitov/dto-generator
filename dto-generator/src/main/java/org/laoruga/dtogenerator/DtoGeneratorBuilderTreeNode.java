package org.laoruga.dtogenerator;

import lombok.Getter;

import java.util.LinkedList;
import java.util.List;

import static org.laoruga.dtogenerator.DtoGeneratorBuildersTree.ROOT;

/**
 * @author Il'dar Valitov
 * Created on 31.01.2023
 */
public class DtoGeneratorBuilderTreeNode extends DtoGeneratorBuilder<Object> {

    @Getter
    private final String fieldName;
    @Getter
    private final List<DtoGeneratorBuilderTreeNode> children = new LinkedList<>();

    private DtoGeneratorBuilderTreeNode(DtoGeneratorBuilder<?> toCopy, String fieldName) {
        super(toCopy.getConfiguration(),
                toCopy.getTypeGeneratorsProvider(),
                toCopy.getDtoGeneratorBuildersTree(),
                toCopy.getFieldGroupFilter());
        this.fieldName = fieldName;
    }

    public DtoGeneratorBuilderTreeNode(DtoGeneratorBuilder<?> toCopy, String fieldName, String[] pathFromRootDto) {
        super(toCopy.getConfiguration(),
                new TypeGeneratorsProvider(toCopy.getTypeGeneratorsProvider(), pathFromRootDto),
                toCopy.getDtoGeneratorBuildersTree(),
                toCopy.getFieldGroupFilter());
        this.fieldName = fieldName;
    }

    public static DtoGeneratorBuilderTreeNode createRootNode(DtoGeneratorBuilder<?> dtoGenBuilder) {
        return new DtoGeneratorBuilderTreeNode(dtoGenBuilder, ROOT);
    }

    public static DtoGeneratorBuilderTreeNode createNode(DtoGeneratorBuilder<?> dtoGenBuilder,
                                                         String fieldName,
                                                         String[] pathFromRootDto) {
        return new DtoGeneratorBuilderTreeNode(dtoGenBuilder, fieldName, pathFromRootDto);
    }
}
