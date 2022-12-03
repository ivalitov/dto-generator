package org.laoruga.dtogenerator.generators.builders;

import org.laoruga.dtogenerator.GeneratorsProvider;
import org.laoruga.dtogenerator.api.generators.IGenerator;
import org.laoruga.dtogenerator.api.generators.IGeneratorBuilderConfigurable;
import org.laoruga.dtogenerator.api.remarks.IRuleRemark;
import org.laoruga.dtogenerator.config.DtoGeneratorInstanceConfig;
import org.laoruga.dtogenerator.config.DtoGeneratorStaticConfig;
import org.laoruga.dtogenerator.exceptions.DtoGeneratorException;
import org.laoruga.dtogenerator.generators.basictypegenerators.CollectionGenerator;
import org.laoruga.dtogenerator.generators.basictypegenerators.EnumGenerator;
import org.laoruga.dtogenerator.generators.basictypegenerators.IConfigDto;

import java.util.Optional;
import java.util.function.Supplier;

/**
 * @author Il'dar Valitov
 * Created on 24.11.2022
 */
public abstract class AbstractGeneratorBuildersProvider {

    private final DtoGeneratorInstanceConfig configuration;
    private AbstractGeneratorBuildersProvider nextProvider;
    volatile private IRuleRemark maybeRemark;

    protected AbstractGeneratorBuildersProvider(DtoGeneratorInstanceConfig configuration) {
        this.configuration = configuration;
    }

    public void addNextProvider(AbstractGeneratorBuildersProvider provider) {
        nextProvider = provider;
    }

    public Optional<AbstractGeneratorBuildersProvider> getNextProvider() {
        return Optional.ofNullable(nextProvider);
    }

    protected DtoGeneratorInstanceConfig getConfiguration() {
        return configuration;
    }

    abstract Optional<IGenerator<?>> selectOrCreateGenerator();


    public Optional<IGenerator<?>> getGenerator() {
        Optional<IGenerator<?>> maybeGenerator = selectOrCreateGenerator();
        if (maybeGenerator.isPresent()) {
            return maybeGenerator;
        } else {
            Optional<AbstractGeneratorBuildersProvider> nextProvider = getNextProvider();
            if (nextProvider.isPresent()) {
                return nextProvider.get().getGenerator();
            } else {
                return Optional.empty();
            }
        }
    }

    public void accept(GeneratorsProvider<?>.ProvidersVisitor visitor) {
        visitor.visit(this);
        if (getNextProvider().isPresent()) {
            getNextProvider().get().accept(visitor);
        }
    }

    public IRuleRemark getRuleRemark() {
        return maybeRemark;
    }

    public void setMaybeRemark(IRuleRemark maybeRemark) {
        this.maybeRemark = maybeRemark;
    }

    protected IGenerator<?> getGenerator(Supplier<IConfigDto> configDtoSupplier,
                                         Supplier<IGeneratorBuilderConfigurable> genBuildSupplier,
                                         FunctionTwo<IConfigDto, IGeneratorBuilderConfigurable, IGenerator<?>> generatorSupplier) {
        IGeneratorBuilderConfigurable genBuilder = genBuildSupplier.get();

        IConfigDto instanceConfig = getConfiguration().getGenBuildersConfig().getConfig(genBuilder.getClass());
        IConfigDto staticConfig = DtoGeneratorStaticConfig.getInstance().getGenBuildersConfig().getConfig(genBuilder.getClass());
        IConfigDto config = configDtoSupplier.get();

        if (staticConfig != null) {
            config.merge(staticConfig);
        }

        if (instanceConfig != null) {
            config.merge(instanceConfig);
        }

        if (getRuleRemark() != null) {
            config.setRuleRemark(getRuleRemark());
        }

        return generatorSupplier.apply(config, genBuilder);
    }

    protected FunctionTwo<
            IConfigDto,
            IGeneratorBuilderConfigurable,
            IGenerator<?>> enumGeneratorSupplier(Class<?> generatedType) {
        return (config, builder) -> {
            EnumGenerator.ConfigDto enumConfig = (EnumGenerator.ConfigDto) config;
            if (enumConfig.getEnumClass() == null) {
                if (generatedType.isEnum()) {
                    enumConfig.setEnumClass((Class<? extends Enum<?>>) generatedType);
                } else {
                    throw new DtoGeneratorException("Field type must be enum : '"
                            + generatedType + "'");
                }
            }
            return ((EnumGenerator.EnumGeneratorBuilder) builder).build(config, true);
        };
    }

    protected FunctionTwo<
            IConfigDto,
            IGeneratorBuilderConfigurable,
            IGenerator<?>> collectionGeneratorSupplier(IGenerator<?> elementGenerator) {
        return (config, builder) -> {

            ((CollectionGenerator.ConfigDto) config).setElementGenerator((IGenerator<Object>) elementGenerator);
            return builder.build(config, true);

        };
    }

    public interface FunctionTwo<T, V, R> {

        /**
         * Applies this function to the given argument.
         *
         * @param t the function first argument
         * @param v the function sconf argument
         * @return the function result
         */
        R apply(T t, V v);
    }
}
