package org.laoruga.dtogenerator.generators.basictypegenerators;

import lombok.AllArgsConstructor;
import org.apache.commons.math3.random.RandomDataGenerator;
import org.laoruga.dtogenerator.api.generators.ICollectionGenerator;
import org.laoruga.dtogenerator.api.generators.IGenerator;
import org.laoruga.dtogenerator.api.generators.IGeneratorBuilder;
import org.laoruga.dtogenerator.api.remarks.IRuleRemark;
import org.laoruga.dtogenerator.config.DtoGeneratorConfig;
import org.laoruga.dtogenerator.constants.BasicRuleRemark;
import org.laoruga.dtogenerator.exceptions.DtoGeneratorException;

import java.util.Collection;

/**
 * @author Il'dar Valitov
 * Created on 19.05.2022
 */

@AllArgsConstructor
public class CollectionGenerator<T> implements ICollectionGenerator<T> {

    private final int minSize;
    private final int maxSize;
    private final Collection<T> listInstance;
    private final IGenerator<T> itemGenerator;
    private final IRuleRemark ruleRemark;

    @Override
    public Collection<T> generate() {
        int maxAttempts = DtoGeneratorConfig.getMaxCollectionGenerationCycles();
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
                if (ineffectiveAttempts == maxAttempts) {
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
