package org.laoruga.dtogenerator.generator.providers;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.laoruga.dtogenerator.api.generators.IGenerator;
import org.laoruga.dtogenerator.api.generators.IGeneratorBuilder;
import org.laoruga.dtogenerator.api.generators.IGeneratorBuilderConfigurable;
import org.laoruga.dtogenerator.api.rules.ArrayRule;
import org.laoruga.dtogenerator.exceptions.DtoGeneratorException;
import org.laoruga.dtogenerator.generator.builder.builders.ArrayGeneratorBuilder;
import org.laoruga.dtogenerator.generator.configs.ArrayConfigDto;
import org.laoruga.dtogenerator.util.ReflectionUtils;

import java.lang.annotation.Annotation;


/**
 * @author Il'dar Valitov
 * Created on 24.11.2022
 */
@Slf4j
@Getter(AccessLevel.PRIVATE)
public class GeneratorsProviderByAnnotationForArray extends GeneratorsProviderByAnnotationForList {

    public GeneratorsProviderByAnnotationForArray(GeneratorsProviderByAnnotation generatorsProvider) {
        super(generatorsProvider);
    }

    @Override
    protected IGenerator<?> buildListGenerator(Annotation arrayRule,
                                               IGeneratorBuilder<?> arrayGenBuilder,
                                               IGenerator<?> elementGenerator,
                                               Class<?> fieldType,
                                               String fieldName) {
        Class<? extends Annotation> rulesClass = arrayRule.annotationType();

        if (arrayGenBuilder instanceof ArrayGeneratorBuilder) {

            ArrayConfigDto configDto;

            if (ArrayRule.class == rulesClass) {

                ArrayRule rule = (ArrayRule) arrayRule;

                Class<?> arrayElementType = ReflectionUtils.getArrayElementType(fieldType);

                configDto = new ArrayConfigDto(rule, arrayElementType);

                return generatorsProvider.getGenerator(
                        () -> configDto,
                        () -> (IGeneratorBuilderConfigurable<?>) arrayGenBuilder,
                        generatorsProvider.getArrayGeneratorSupplier(arrayElementType, elementGenerator),
                        fieldType,
                        fieldName);


            } else {
                throw new DtoGeneratorException("Unknown rules annotation class '" + rulesClass + "'");
            }
        }

        log.debug("Unknown array builder builds as is, without Rules annotation params passing.");

        return arrayGenBuilder.build();
    }
}