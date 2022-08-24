package laoruga.dtogenerator.api.generators.basictypegenerators;

import laoruga.dtogenerator.api.exceptions.DtoGeneratorException;
import laoruga.dtogenerator.api.markup.generators.ICollectionGenerator;
import laoruga.dtogenerator.api.markup.generators.IGenerator;
import laoruga.dtogenerator.api.markup.generators.IGeneratorBuilder;
import laoruga.dtogenerator.api.constants.BasicRuleRemark;
import laoruga.dtogenerator.api.markup.remarks.IRuleRemark;
import lombok.AllArgsConstructor;
import org.apache.commons.math3.random.RandomDataGenerator;

import java.util.Collection;

/**
 * @author Il'dar Valitov
 * Created on 19.05.2022
 */

@AllArgsConstructor
public class CollectionGenerator<ITEM_TYPE> implements ICollectionGenerator<Collection<ITEM_TYPE>> {

    public static int MAX_GENERATION_ATTEMPTS = 100;
    private final int minSize;
    private final int maxSize;
    private final Collection<ITEM_TYPE> listInstance;
    private final IGenerator<ITEM_TYPE> itemGenerator;
    private final IRuleRemark ruleRemark;

    @Override
    public Collection<ITEM_TYPE> generate() {
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
                if (ineffectiveAttempts == MAX_GENERATION_ATTEMPTS) {
                    throw new DtoGeneratorException("Expected size of collection can't be reached");
                }
            }
        }
        return listInstance;
    }

    @Override
    public IGenerator<?> getItemGenerator() {
        return itemGenerator;
    }

    public static CollectionGeneratorBuilder<?> builder() {
        return new CollectionGeneratorBuilder<>();
    }

    public static final class CollectionGeneratorBuilder<ITEM_TYPE> implements IGeneratorBuilder<ICollectionGenerator<?>> {
        private int minSize;
        private int maxSize;
        private Collection<ITEM_TYPE> listInstance;
        private IGenerator<ITEM_TYPE> itemGenerator;
        private IRuleRemark ruleRemark;

        private CollectionGeneratorBuilder() {
        }

        public CollectionGeneratorBuilder<ITEM_TYPE> minSize(int minSize) {
            this.minSize = minSize;
            return this;
        }

        public CollectionGeneratorBuilder<ITEM_TYPE> maxSize(int maxSize) {
            this.maxSize = maxSize;
            return this;
        }

        public CollectionGeneratorBuilder<ITEM_TYPE> listInstance(Collection<ITEM_TYPE> listInstance) {
            this.listInstance = listInstance;
            return this;
        }

        public CollectionGeneratorBuilder<ITEM_TYPE> itemGenerator(IGenerator<ITEM_TYPE> itemGenerator) {
            this.itemGenerator = itemGenerator;
            return this;
        }

      public CollectionGeneratorBuilder<ITEM_TYPE> ruleRemark(IRuleRemark ruleRemark) {
            this.ruleRemark = ruleRemark;
            return this;
        }

        public CollectionGenerator<ITEM_TYPE> build() {
            return new CollectionGenerator<>(minSize, maxSize, listInstance, itemGenerator, ruleRemark);
        }
    }
}
