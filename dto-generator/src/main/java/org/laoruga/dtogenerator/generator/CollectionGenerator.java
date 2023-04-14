package org.laoruga.dtogenerator.generator;

import lombok.AllArgsConstructor;
import org.laoruga.dtogenerator.api.RuleRemark;
import org.laoruga.dtogenerator.api.generators.Generator;
import org.laoruga.dtogenerator.api.generators.ListGenerator;
import org.laoruga.dtogenerator.config.dto.DtoGeneratorStaticConfig;
import org.laoruga.dtogenerator.constants.BoundaryConfig;
import org.laoruga.dtogenerator.exceptions.DtoGeneratorException;
import org.laoruga.dtogenerator.generator.config.dto.CollectionConfig;
import org.laoruga.dtogenerator.util.RandomUtils;

import java.util.Collection;
import java.util.Objects;
import java.util.function.Supplier;

/**
 * @author Il'dar Valitov
 * Created on 19.05.2022
 */

@AllArgsConstructor
public class CollectionGenerator implements ListGenerator {

    private final int minSize;
    private final int maxSize;
    private final Supplier<Collection<Object>> collectionInstanceSupplier;
    private final Generator<Object> elementGenerator;
    private final RuleRemark ruleRemark;

    @SuppressWarnings("unchecked")
    public CollectionGenerator(CollectionConfig collectionConfig) {
        minSize = collectionConfig.getMinSize();
        maxSize = collectionConfig.getMaxSize();
        collectionInstanceSupplier = (Supplier<Collection<Object>>) Objects.requireNonNull(collectionConfig.getCollectionInstanceSupplier(), "Collection instance must be set.");
        elementGenerator = (Generator<Object>) Objects.requireNonNull(collectionConfig.getElementGenerator(), "Collection element generator must be set");
        ruleRemark = Objects.requireNonNull(collectionConfig.getRuleRemark(), "Unexpected error, rule remark haven't set.");
    }

    @Override
    public Collection<Object> generate() {
        Collection<Object> collectionInstance = collectionInstanceSupplier.get();
        int maxAttempts = DtoGeneratorStaticConfig.getInstance().getDtoGeneratorConfig().getMaxCollectionGenerationCycles();
        int size;
        switch ((BoundaryConfig) ruleRemark) {

            case MIN_VALUE:
                size = minSize;
                break;

            case MAX_VALUE:
                size = maxSize;
                break;

            case RANDOM_VALUE:
            case NOT_DEFINED:
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

    public Generator<Object> getElementGenerator() {
        return elementGenerator;
    }

}
