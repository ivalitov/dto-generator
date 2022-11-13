package laoruga.dtogenerator.api.generators.basictypegenerators;

import laoruga.dtogenerator.api.constants.BasicRuleRemark;
import laoruga.dtogenerator.api.exceptions.DtoGeneratorException;
import laoruga.dtogenerator.api.markup.generators.ICollectionGenerator;
import laoruga.dtogenerator.api.markup.generators.IGenerator;
import laoruga.dtogenerator.api.markup.generators.IGeneratorBuilder;
import laoruga.dtogenerator.api.markup.remarks.IRuleRemark;
import lombok.AllArgsConstructor;
import org.apache.commons.math3.random.RandomDataGenerator;

import java.util.Collection;

/**
 * @author Il'dar Valitov
 * Created on 19.05.2022
 */

@AllArgsConstructor
public class CollectionGenerator<T> implements ICollectionGenerator<T> {

    //TODO move to the properties
    public static int maxGenerationAttempts = 100;
    private final int minSize;
    private final int maxSize;
    private final Collection<T> listInstance;
    private final IGenerator<T> itemGenerator;
    private final IRuleRemark ruleRemark;

    @Override
    public Collection<T> generate() {
        int size;
        switch ((BasicRuleRemark) ruleRemark) {
            case MIN_VALUE:
                size = minSize;
                break;
            case MAX_VALUE:
                size = maxSize;
                break;
            case RANDOM_VALUE:
                size = new RandomDataGenerator().nextInt(minSize, maxSize);
                break;
            case NULL_VALUE:
                return null;
            default:
                throw new IllegalStateException("Unexpected value: " + ruleRemark);
        }
        int prevSize;
        int ineffectiveAttempts = 0;
        while (listInstance.size() < size) {
            prevSize = listInstance.size();
            listInstance.add(itemGenerator.generate());
            if (prevSize == listInstance.size()) {
                ineffectiveAttempts++;
                if (ineffectiveAttempts == maxGenerationAttempts) {
                    throw new DtoGeneratorException("Expected size of collection can't be reached");
                }
            }
        }
        return listInstance;
    }

    @Override
    public IGenerator<T> getItemGenerator() {
        return itemGenerator;
    }

    public static CollectionGeneratorBuilder<?> builder() {
        return new CollectionGeneratorBuilder<>();
    }

    public static final class CollectionGeneratorBuilder<V> implements IGeneratorBuilder<ICollectionGenerator<?>> {
        private int minSize;
        private int maxSize;
        private Collection<V> listInstance;
        private IGenerator<V> itemGenerator;
        private IRuleRemark ruleRemark;

        private CollectionGeneratorBuilder() {
        }

        public CollectionGeneratorBuilder<V> minSize(int minSize) {
            this.minSize = minSize;
            return this;
        }

        public CollectionGeneratorBuilder<V> maxSize(int maxSize) {
            this.maxSize = maxSize;
            return this;
        }

        public CollectionGeneratorBuilder<V> listInstance(Collection<V> listInstance) {
            this.listInstance = listInstance;
            return this;
        }

        public CollectionGeneratorBuilder<V> itemGenerator(IGenerator<V> itemGenerator) {
            this.itemGenerator = itemGenerator;
            return this;
        }

      public CollectionGeneratorBuilder<V> ruleRemark(IRuleRemark ruleRemark) {
            this.ruleRemark = ruleRemark;
            return this;
        }

        public CollectionGenerator<V> build() {
            return new CollectionGenerator<>(minSize, maxSize, listInstance, itemGenerator, ruleRemark);
        }
    }
}
