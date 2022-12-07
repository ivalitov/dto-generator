package org.laoruga.dtogenerator;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

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

    public DtoGeneratorBuilder<?> getBuilder(String[] fields) {
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
