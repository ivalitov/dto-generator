package org.laoruga.dtogenerator.generators;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.math3.random.RandomDataGenerator;
import org.laoruga.dtogenerator.api.generators.ICollectionGenerator;
import org.laoruga.dtogenerator.api.generators.IGenerator;
import org.laoruga.dtogenerator.api.generators.IGeneratorBuilder;
import org.laoruga.dtogenerator.api.generators.IGeneratorBuilderConfigurable;
import org.laoruga.dtogenerator.api.remarks.IRuleRemark;
import org.laoruga.dtogenerator.api.rules.ListRule;
import org.laoruga.dtogenerator.api.rules.SetRule;
import org.laoruga.dtogenerator.config.DtoGeneratorStaticConfig;
import org.laoruga.dtogenerator.constants.RuleRemark;
import org.laoruga.dtogenerator.exceptions.DtoGeneratorException;
import org.laoruga.dtogenerator.util.ReflectionUtils;

import java.util.Collection;
import java.util.Objects;
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

    public static class CollectionGeneratorBuilder<V> implements IGeneratorBuilderConfigurable {
        protected final ConfigDto configDto;

        public CollectionGeneratorBuilder() {
            this.configDto = new ConfigDto();
        }

        public CollectionGeneratorBuilder<V> minSize(int minSize) {
            configDto.minSize = minSize;
            return this;
        }

        public CollectionGeneratorBuilder<V> maxSize(int maxSize) {
            configDto.maxSize = maxSize;
            return this;
        }

        @SuppressWarnings("unchecked")
        public CollectionGeneratorBuilder<?> collectionInstance(Supplier<Collection<Object>> listInstance) {
            configDto.collectionInstance = listInstance;
            return this;
        }

        @SuppressWarnings("unchecked")
        public CollectionGeneratorBuilder<?> elementGenerator(IGenerator<?> itemGenerator) {
            configDto.elementGenerator = (IGenerator<Object>) itemGenerator;
            return this;
        }

        public CollectionGeneratorBuilder<?> ruleRemark(IRuleRemark ruleRemark) {
            configDto.ruleRemark = ruleRemark;
            return this;
        }

        public CollectionGenerator build() {
            return build(configDto, false);
        }

        @Override
        @SuppressWarnings("unchecked")
        public CollectionGenerator build(IConfigDto configDto, boolean merge) {
            if (merge) {
                configDto.merge(this.configDto);
            }
            ConfigDto collectionConfig = (ConfigDto) configDto;
            return new CollectionGenerator(
                    collectionConfig.minSize,
                    collectionConfig.maxSize,
                    Objects.requireNonNull(collectionConfig.collectionInstance, "Collection instance must be set."),
                    Objects.requireNonNull(collectionConfig.elementGenerator, "Collection element generator must be set"),
                    Objects.requireNonNull(collectionConfig.ruleRemark, "Unexpected error, rule remark haven't set."));
        }

    }

    @Getter
    @Setter
    @Builder
    @AllArgsConstructor
    public static class ConfigDto implements IConfigDto {
        private Integer minSize;
        private Integer maxSize;
        private Supplier<Collection<Object>> collectionInstance;
        private IGenerator<Object> elementGenerator;
        private IRuleRemark ruleRemark;

        public ConfigDto() {
        }

        @SuppressWarnings("unchecked")
        public ConfigDto(SetRule rule) {
            this.minSize = rule.minSize();
            this.maxSize = rule.maxSize();
            this.collectionInstance = () -> ReflectionUtils.createCollectionInstance(rule.setClass());
            this.ruleRemark = rule.ruleRemark();
        }

        @SuppressWarnings("unchecked")
        public ConfigDto(ListRule rule) {
            this.minSize = rule.minSize();
            this.maxSize = rule.maxSize();
            this.collectionInstance = () -> ReflectionUtils.createCollectionInstance(rule.listClass());
            this.ruleRemark = rule.ruleRemark();
        }

        public ConfigDto setCollectionInstance(Supplier<Collection<Object>> collectionInstance) {
            this.collectionInstance = collectionInstance;
            return this;
        }

        public ConfigDto setElementGenerator(IGenerator<Object> elementGenerator) {
            this.elementGenerator = elementGenerator;
            return this;
        }

        public void merge(ConfigDto from) {
            if (from.getMinSize() != null) this.minSize = from.getMinSize();
            if (from.getMaxSize() != null) this.maxSize = from.getMaxSize();
            if (from.getCollectionInstance() != null) this.collectionInstance = from.getCollectionInstance();
            if (from.getElementGenerator() != null) this.elementGenerator = from.getElementGenerator();
            if (from.getRuleRemark() != null) this.ruleRemark = from.getRuleRemark();
        }

        @Override
        public Class<? extends IGeneratorBuilder> getBuilderClass() {
            return CollectionGeneratorBuilder.class;
        }

        @Override
        public void merge(IConfigDto from) {
            ConfigDto fromConfig = (ConfigDto) from;
            if (fromConfig.getMinSize() != null) this.minSize = fromConfig.getMinSize();
            if (fromConfig.getMaxSize() != null) this.maxSize = fromConfig.getMaxSize();
            if (fromConfig.getCollectionInstance() != null)
                this.collectionInstance = fromConfig.getCollectionInstance();
            if (fromConfig.getElementGenerator() != null) this.elementGenerator = fromConfig.getElementGenerator();
            if (fromConfig.getRuleRemark() != null) this.ruleRemark = fromConfig.getRuleRemark();
        }
    }
}
