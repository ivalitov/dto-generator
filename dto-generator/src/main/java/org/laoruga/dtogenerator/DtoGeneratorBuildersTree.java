package org.laoruga.dtogenerator;

import java.util.Arrays;
import java.util.Objects;

/**
 * Tree for storing generators for nested DTOs.
 */
public class DtoGeneratorBuildersTree {

    public static final String ROOT = "%ROOT%";

    private final DtoGeneratorBuilderTreeNode tree;

    public DtoGeneratorBuildersTree(DtoGeneratorBuilderTreeNode rootNode) {
        this.tree = rootNode;
    }

    public DtoGeneratorBuilder<?> getBuilderLazy(String[] fields) {
        if (fields.length < 1) {
            throw new IllegalArgumentException(
                    "Field path must contain at least 1 element, but was: " + Arrays.asList(fields));
        }
        return getBuilderLazy(fields, 0, tree).getDtoGeneratorBuilder();
    }

    private DtoGeneratorBuilderTreeNode getBuilderLazy(String[] fields, int idx, DtoGeneratorBuilderTreeNode node) {

        if (fields.length - 1 == idx) {
            return node;
        }

        idx = idx + 1;

        for (DtoGeneratorBuilderTreeNode child : node.getChildren()) {
            if (Objects.equals(child.getFieldName(), fields[idx])) {
                return getBuilderLazy(fields, idx, child);
            }
        }

        DtoGeneratorBuilderTreeNode newNode = DtoGeneratorBuilderTreeNode.createNode(tree, fields[idx], fields);
        node.getChildren().add(newNode);
        return newNode;
    }

}