package org.laoruga.dtogenerator;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.laoruga.dtogenerator.api.generators.Generator;
import org.laoruga.dtogenerator.generator.NestedDtoGenerator;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * @author Il'dar Valitov
 * Created on 11.04.2023
 */
@RequiredArgsConstructor
@Getter
public class FieldGenerators {

    private final Map<Supplier<?>, GeneratorEntry> fieldGenerators = new HashMap<>();

    private final List<NestedGeneratorEntry> nestedDtoGenerators = new LinkedList<>();

    public boolean isNestedFieldsExist() {
        return !nestedDtoGenerators.isEmpty();
    }

    public boolean isEmpty() {
        return fieldGenerators.isEmpty() && nestedDtoGenerators.isEmpty();
    }

    public int size() {
        return nestedDtoGenerators.size() + fieldGenerators.values().stream()
                .map(entry -> entry.fieldGeneratorMap.size())
                .reduce(Integer::sum)
                .orElse(0);
    }

    @Override
    public String toString() {
        int size = size();
        if (size == 0) {
            return "Generators not found";
        }
        StringBuilder resultComment = new StringBuilder(size + " generators for fields:\n");
        final AtomicInteger idx = new AtomicInteger(0);

        for (GeneratorEntry entry : fieldGenerators.values()) {
            resultComment.append(resultComment)
                    .append(entry.fieldGeneratorMap.keySet().stream()
                            .map(i -> idx.incrementAndGet() + ". " + i)
                            .collect(Collectors.joining("\n")));
        }

        return resultComment.toString();
    }

    public void addGenerator(Supplier<?> dtoInstanceSupplier,
                             Map<Field, Generator<?>> generatorMap) {

        fieldGenerators.put(dtoInstanceSupplier, new GeneratorEntry(dtoInstanceSupplier, generatorMap));
    }

    public void addGenerator(Field field,
                             Generator<?> generator,
                             Supplier<?> dtoInstanceSupplier) {
        fieldGenerators.putIfAbsent(dtoInstanceSupplier, new GeneratorEntry(dtoInstanceSupplier, new HashMap<>()));
        fieldGenerators.get(dtoInstanceSupplier).getFieldGeneratorMap().put(field, generator);
    }

    public void addNestedGenerator(Supplier<?> dtoInstanceSupplier,
                                   NestedDtoGenerator nestedDtoGenerator,
                                   Field field) {
        nestedDtoGenerators.add(new NestedGeneratorEntry(dtoInstanceSupplier, nestedDtoGenerator, field));
    }

    @RequiredArgsConstructor
    @Getter
    public static class NestedGeneratorEntry {
        final Supplier<?> dtoInstanceSupplier;
        final NestedDtoGenerator nestedDtoGenerator;
        final Field field;
    }

    @RequiredArgsConstructor
    @Getter
    public static class GeneratorEntry {
        final Supplier<?> dtoInstanceSupplier;
        final Map<Field, Generator<?>> fieldGeneratorMap;
    }

}
