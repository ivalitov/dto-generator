package org.laoruga.dtogenerator.generator;

import lombok.AllArgsConstructor;
import org.laoruga.dtogenerator.api.generators.ICollectionGenerator;
import org.laoruga.dtogenerator.api.generators.IGenerator;
import org.laoruga.dtogenerator.api.remarks.IRuleRemark;
import org.laoruga.dtogenerator.config.dto.DtoGeneratorStaticConfig;
import org.laoruga.dtogenerator.constants.RuleRemark;
import org.laoruga.dtogenerator.exceptions.DtoGeneratorException;
import org.laoruga.dtogenerator.generator.builder.builders.CollectionGeneratorBuilder;
import org.laoruga.dtogenerator.util.RandomUtils;

import java.util.Collection;
import java.util.function.Supplier;

/**
 * @author Il'dar Valitov
 * Created on 19.05.2022
 */

@AllArgsConstructor
public class CollectionGenerator<T> implements ICollectionGenerator<T> {

    private final int minSize;
    private final int maxSize;
    private final Supplier<Collection<T>> collectionInstanceSupplier;
    private final IGenerator<T> elementGenerator;
    private final IRuleRemark ruleRemark;

    public static CollectionGeneratorBuilder builder() {
        return new CollectionGeneratorBuilder();
    }

    @Override
    public Collection<T> generate() {
        Collection<T> collectionInstance = collectionInstanceSupplier.get();
        int maxAttempts = DtoGeneratorStaticConfig.getInstance().getDtoGeneratorConfig().getMaxCollectionGenerationCycles();
        int size;
        switch ((RuleRemark) ruleRemark) {
            case MIN_VALUE:
                size = minSize;
                break;
            case MAX_VALUE:
                size = maxSize;
                break;
            case RANDOM_VALUE:
                size = RandomUtils.nextInt(minSize, maxSize);
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
                            + "' attempts collection has size: '"
                            + collectionInstance.size() + "'");
                }
            }
        }
        return collectionInstance;
    }

    public IGenerator<T> getElementGenerator() {
        return elementGenerator;
    }

}
