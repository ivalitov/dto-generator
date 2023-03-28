package org.laoruga.dtogenerator.generator.providers;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.laoruga.dtogenerator.DtoGenerator;
import org.laoruga.dtogenerator.api.generators.ICollectionGenerator;
import org.laoruga.dtogenerator.api.generators.IGenerator;
import org.laoruga.dtogenerator.api.generators.custom.ICustomGeneratorRemarkable;
import org.laoruga.dtogenerator.api.generators.custom.ICustomGeneratorRemarkableArgs;
import org.laoruga.dtogenerator.constants.RuleType;
import org.laoruga.dtogenerator.exceptions.DtoGeneratorException;
import org.laoruga.dtogenerator.generator.CustomGenerator;
import org.laoruga.dtogenerator.generator.MapGenerator;
import org.laoruga.dtogenerator.generator.config.GeneratorConfiguratorByAnnotation;
import org.laoruga.dtogenerator.generator.config.dto.ConfigDto;
import org.laoruga.dtogenerator.generator.providers.suppliers.GeneratorSuppliers;
import org.laoruga.dtogenerator.generator.providers.suppliers.GeneratorSuppliersDefault;
import org.laoruga.dtogenerator.rule.IRuleInfo;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * @author Il'dar Valitov
 * Created on 24.11.2022
 */
@Slf4j
public class GeneratorsProviderByAnnotation {

    @Getter
    private final GeneratorConfiguratorByAnnotation configuratorByAnnotation;
    private final GeneratorsProviderByType generatorsProviderByType;
    private final GeneratorSuppliers userGeneratorSuppliers;
    private final GeneratorSuppliers defaultGeneratorSuppliers;

    public GeneratorsProviderByAnnotation(GeneratorConfiguratorByAnnotation configuratorByAnnotation,
                                          GeneratorsProviderByType generatorsProviderByType,
                                          GeneratorSuppliers userGeneratorSuppliers) {
        this.configuratorByAnnotation = configuratorByAnnotation;
        this.generatorsProviderByType = generatorsProviderByType;
        this.userGeneratorSuppliers = userGeneratorSuppliers;
        this.defaultGeneratorSuppliers = GeneratorSuppliersDefault.getInstance();
    }

    IGenerator<?> getGenerator(IRuleInfo ruleInfo,
                               Supplier<?> dtoInstanceSupplier,
                               Supplier<DtoGenerator<?>> nestedDtoGeneratorSupplier) {

        String fieldName = ruleInfo.getField().getName();
        Class<?> requiredType = ruleInfo.getRequiredType();

        Optional<Function<ConfigDto, IGenerator<?>>> maybeUserGeneratorSupplier = getUserGeneratorSupplier(requiredType);

        boolean isUserGenerator = maybeUserGeneratorSupplier.isPresent();

        Function<ConfigDto, IGenerator<?>> generatorSupplier = isUserGenerator ?
                maybeUserGeneratorSupplier.get() :
                getDefaultGeneratorSupplier(ruleInfo.getRule(), requiredType);

        ConfigDto config = configuratorByAnnotation.createGeneratorConfig(
                ruleInfo.getRule(),
                requiredType,
                fieldName,
                dtoInstanceSupplier,
                nestedDtoGeneratorSupplier);

        return generatorSupplier.apply(config);
    }

    Function<ConfigDto, IGenerator<?>> getDefaultGeneratorSupplier(Annotation rules, Class<?> generatedType) {
        Optional<Function<ConfigDto, IGenerator<?>>> maybeBuilder;
        switch (RuleType.getType(rules)) {
            case CUSTOM:
            case NESTED:
                maybeBuilder = defaultGeneratorSuppliers.getGeneratorSupplier(rules);
                break;
            default:
                maybeBuilder = defaultGeneratorSuppliers.getGeneratorSupplier(generatedType);
        }

        return maybeBuilder.orElseThrow(() ->
                new DtoGeneratorException("General generator builder not found. Rules: '"
                        + rules.annotationType().getName() + "', Genrated type: '" + generatedType.getName() + "'")
        );
    }

    Optional<Function<ConfigDto, IGenerator<?>>> getUserGeneratorSupplier(Class<?> generatedType) {
        return userGeneratorSuppliers.getGeneratorSupplier(generatedType);
    }


    void prepareCustomRemarks(IGenerator<?> generator, String fieldName) {
        if (generator instanceof CustomGenerator) {
            IGenerator<?> usersGeneratorInstance = ((CustomGenerator) generator).getUsersGeneratorInstance();
            if (usersGeneratorInstance instanceof ICollectionGenerator) {

                prepareCustomRemarks(
                        ((ICollectionGenerator<?>) usersGeneratorInstance).getElementGenerator(),
                        fieldName
                );

            } else if (usersGeneratorInstance instanceof MapGenerator) {

                prepareCustomRemarks(
                        ((MapGenerator) usersGeneratorInstance).getKeyGenerator(),
                        fieldName
                );

                prepareCustomRemarks(
                        ((MapGenerator) usersGeneratorInstance).getValueGenerator(),
                        fieldName
                );

            } else if (usersGeneratorInstance instanceof ICustomGeneratorRemarkableArgs) {

                ((ICustomGeneratorRemarkableArgs<?>) usersGeneratorInstance).setRuleRemarks(
                        configuratorByAnnotation.getRemarksHolder()
                                .getCustomRemarks()
                                .getRemarksWithArgs(fieldName, usersGeneratorInstance.getClass()));

            } else if (usersGeneratorInstance instanceof ICustomGeneratorRemarkable) {

                ((ICustomGeneratorRemarkable<?>) usersGeneratorInstance).setRuleRemarks(
                        configuratorByAnnotation.getRemarksHolder()
                                .getCustomRemarks()
                                .getRemarks(fieldName, usersGeneratorInstance.getClass())
                );

            }
        }

    }

    IGenerator<?> getGeneratorByType(Field field, Class<?> generatedType) {
        Optional<IGenerator<?>> generatorByType = generatorsProviderByType.getGenerator(field, generatedType);

        if (!generatorByType.isPresent()) {
            throw new DtoGeneratorException("Generator wasn't found by type: '" + generatedType + "'" +
                    " for field: '" + field.getType() + " " + field.getName() + "'");
        }

        return generatorByType.get();
    }

}
