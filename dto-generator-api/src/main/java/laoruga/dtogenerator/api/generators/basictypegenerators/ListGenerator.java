package laoruga.dtogenerator.api.generators.basictypegenerators;

import laoruga.dtogenerator.api.markup.generators.ICollectionGenerator;
import laoruga.dtogenerator.api.markup.generators.IGenerator;
import laoruga.dtogenerator.api.markup.generators.IGeneratorBuilder;
import org.apache.commons.math3.random.RandomDataGenerator;

import java.util.List;

public class ListGenerator<ITEM_TYPE> implements ICollectionGenerator<List<ITEM_TYPE>> {

    private final int size;
    private final List<ITEM_TYPE> listInstance;
    private final IGenerator<ITEM_TYPE> itemGenerator;


    public ListGenerator(int minSize, int maxSize, List<ITEM_TYPE> listInstance, IGenerator<ITEM_TYPE> itemGenerator) {
        this.listInstance = listInstance;
        this.itemGenerator = itemGenerator;
        this.size = new RandomDataGenerator().nextInt(minSize, maxSize);
    }

    @Override
    public List<ITEM_TYPE> generate() {
        while (listInstance.size() < size) {
            listInstance.add(itemGenerator.generate());
        }
        return listInstance;
    }

    @Override
    public IGenerator<?> getInnerGenerator() {
        return itemGenerator;
    }

    public static ListGeneratorBuilder<?> builder() {
        return new ListGeneratorBuilder<>();
    }

    public static final class ListGeneratorBuilder<ITEM_TYPE> implements IGeneratorBuilder {
        private int minSize;
        private int maxSize;
        private List<ITEM_TYPE> listInstance;
        private IGenerator<ITEM_TYPE> itemGenerator;

        private ListGeneratorBuilder() {
        }

        public ListGeneratorBuilder<ITEM_TYPE> minSize(int minSize) {
            this.minSize = minSize;
            return this;
        }

        public ListGeneratorBuilder<ITEM_TYPE> maxSize(int maxSize) {
            this.maxSize = maxSize;
            return this;
        }

        public ListGeneratorBuilder<ITEM_TYPE> listInstance(List<ITEM_TYPE> listInstance) {
            this.listInstance = listInstance;
            return this;
        }

        public ListGeneratorBuilder<ITEM_TYPE> itemGenerator(IGenerator<ITEM_TYPE> itemGenerator) {
            this.itemGenerator = itemGenerator;
            return this;
        }

        public ListGenerator<ITEM_TYPE> build() {
            return new ListGenerator<>(minSize, maxSize, listInstance, itemGenerator);
        }
    }
}
