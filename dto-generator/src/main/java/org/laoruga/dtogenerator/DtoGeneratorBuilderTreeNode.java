package org.laoruga.dtogenerator;

import lombok.Getter;

import java.util.LinkedList;
import java.util.List;

import static org.laoruga.dtogenerator.DtoGeneratorBuildersTree.ROOT;

/**
 * @author Il'dar Valitov
 * Created on 31.01.2023
 */
public class DtoGeneratorBuilderTreeNode  {

    @Getter
    private final String fieldName;
    @Getter
    private final DtoGeneratorBuilder<?> dtoGeneratorBuilder;
    @Getter
    private final List<DtoGeneratorBuilderTreeNode> children = new LinkedList<>();

    private DtoGeneratorBuilderTreeNode(DtoGeneratorBuilder<?> toCopy, String fieldName) {
        this.dtoGeneratorBuilder = toCopy;
        this.fieldName = fieldName;
    }

    public static DtoGeneratorBuilderTreeNode createRootNode(DtoGeneratorBuilder<?> dtoGenBuilder) {
        return new DtoGeneratorBuilderTreeNode(dtoGenBuilder, ROOT);
    }

    public static DtoGeneratorBuilderTreeNode createNode(DtoGeneratorBuilderTreeNode dtoGenBuilder,
                                                         String fieldName,
                                                         String[] pathFromRootDto) {
        return new DtoGeneratorBuilderTreeNode(
                new DtoGeneratorBuilder<>(dtoGenBuilder.getDtoGeneratorBuilder(), pathFromRootDto),
                fieldName);
    }
}
