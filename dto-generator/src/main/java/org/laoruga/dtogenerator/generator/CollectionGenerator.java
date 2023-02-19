package org.laoruga.dtogenerator.generator;

import lombok.AllArgsConstructor;
import org.apache.commons.math3.random.RandomDataGenerator;
import org.laoruga.dtogenerator.api.generators.ICollectionGenerator;
import org.laoruga.dtogenerator.api.generators.IGenerator;
import org.laoruga.dtogenerator.api.remarks.IRuleRemark;
import org.laoruga.dtogenerator.config.DtoGeneratorStaticConfig;
import org.laoruga.dtogenerator.constants.RuleRemark;
import org.laoruga.dtogenerator.exceptions.DtoGeneratorException;
import org.laoruga.dtogenerator.generator.builder.builders.CollectionGeneratorBuilder;

import java.util.Collection;
import java.util.function.Supplier;

/**
 * @author Il'dar Valitov
 * Created on 19.05.2022
 */

@AllArgsConstructor
public class CollectionGenerator implements ICollectionGenerator<Object> {

    private final int minSize;
    private final int maxSize;
    private final Supplier<Collection<Object>> collectionInstanceSupplier;
    private final IGenerator<Object> elementGenerator;
    private final IRuleRemark ruleRemark;

    public static CollectionGeneratorBuilder<?> builder() {
        return new CollectionGeneratorBuilder<>();
    }

    @Override
    public Collection<Object> generate() {
        Collection<Object> collectionInstance = collectionInstanceSupplier.get();
        int maxAttempts = DtoGeneratorStaticConfig.getInstance().getMaxCollectionGenerationCycles();
        int size;
        switch ((RuleRemark) ruleRemark) {
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
        while (collectionInstance.size() < size) {
            prevSize = collectionInstance.size();
            collectionInstance.add(elementGenerator.generate());
            if (prevSize == collectionInstance.size()) {
                ineffectiveAttempts++;
                if (ineffectiveAttempts == maxAttempts) {
                    throw new DtoGeneratorException("Expected size: '" + size + "' of collection: '"
                            + collectionInstance.getClass() + "' can't be reached. After '" + ineffectiveAttempts
                            + "' attempts collection size is: '"
                            + collectionInstance.size() + "'");
                }
            }
        }
        return collectionInstance;
    }

    public IGenerator<Object> getElementGenerator() {
        return elementGenerator;
    }

}