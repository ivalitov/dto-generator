package org.laoruga.dtogenerator;

import lombok.Getter;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

/**
 * Tree for storing generators for nested DTOs.
 */
public class DtoGeneratorBuildersTree {

    public static final String ROOT = "%ROOT%";

    private final Node tree;

    public DtoGeneratorBuildersTree(DtoGeneratorBuilder<?> rootDtoGeneratorBuilder) {
        this.tree = new Node(rootDtoGeneratorBuilder, ROOT);
    }

    /**
     * @param fields path to the field in which nested DTO is,
     *               must begin with '%ROOT%'. Examples:
     *               ['%ROOT%', 'person'],
     *               ['%ROOT%', 'universe', 'sunSystem', 'earth'],
     * @return {@link DtoGeneratorBuilder} which was found in the tree.
     * If generator not found, it is instantiating lazy.
     */
    public DtoGeneratorBuilder<?> getBuilderLazy(String[] fields) {
        if (fields.length < 1) {
            throw new IllegalArgumentException(
                    "Path must contain at least 1 element, but was: " + Arrays.asList(fields));
        }
        return getBuilderLazy(fields, 0, tree).getDtoGeneratorBuilder();
    }

    private Node getBuilderLazy(String[] fields, int idx, Node node) {

        if (fields.length - 1 == idx) {
            return node;
        }

        idx = idx + 1;

        for (Node child : node.getChildren()) {
            if (Objects.equals(child.getFieldName(), fields[idx])) {
                return getBuilderLazy(fields, idx, child);
            }
        }

        Node newNode = new Node(
                new DtoGeneratorBuilder<>(tree.getDtoGeneratorBuilder(), fields),
                fields[idx]
        );

        node.getChildren().add(newNode);

        return newNode;
    }

    /**
     * Builder tree node.
     *
     * @author Il'dar Valitov
     * Created on 31.01.2023
     */
    private static class Node {

        @Getter
        private final String fieldName;
        @Getter
        private final DtoGeneratorBuilder<?> dtoGeneratorBuilder;
        @Getter
        private final List<Node> children = new LinkedList<>();

        private Node(DtoGeneratorBuilder<?> generatorBuilder, String fieldName) {
            this.dtoGeneratorBuilder = generatorBuilder;
            this.fieldName = fieldName;
        }
    }

}