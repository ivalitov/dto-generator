package org.laoruga.dtogenerator;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.util.*;

/**
 * Tree of generator builders when nested DTOs exist.
 */
@RequiredArgsConstructor
public class DtoGeneratorBuildersTree {

    public static final String ROOT = "%ROOT%";

    private final Node tree;

    public DtoGeneratorBuildersTree(DtoGeneratorBuilder<?> rootBuilder) {
        this.tree = new Node(ROOT, rootBuilder);
    }

    public DtoGeneratorBuilder<?> getBuilderLazy(String[] fields) {
        if (fields.length < 2) {
            throw new IllegalArgumentException(
                    "Field path must contain at least 1 element, but was: " + Arrays.asList(fields));
        }
        return getBuilderLazy(fields, 1, tree);
    }

    public DtoGeneratorBuilder<?> getBuilderLazy(String[] fields, int idx, Node node) {
        if (fields.length == idx) {
            return node.getBuilder();
        }

        for (Node child : node.getChildren()) {
            if (Objects.equals(child.getFieldName(), fields[idx])) {
                return getBuilderLazy(fields, idx + 1, child);
            }
        }

        Node newNode = new Node(fields[idx], new DtoGeneratorBuilder<>(tree.getBuilder(), fields));
        node.getChildren().add(newNode);
        return newNode.getBuilder();
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
