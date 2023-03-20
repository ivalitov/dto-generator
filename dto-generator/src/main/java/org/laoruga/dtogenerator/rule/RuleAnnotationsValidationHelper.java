package org.laoruga.dtogenerator.rule;

import com.google.common.primitives.Primitives;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.laoruga.dtogenerator.api.rules.CollectionRule;
import org.laoruga.dtogenerator.api.rules.meta.Rule;
import org.laoruga.dtogenerator.api.rules.meta.Rules;
import org.laoruga.dtogenerator.constants.GeneratedTypes;
import org.laoruga.dtogenerator.exceptions.DtoGeneratorValidationException;
import org.laoruga.dtogenerator.util.ReflectionUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Arrays;

import static org.laoruga.dtogenerator.util.ReflectionUtils.getRepeatableAnnotations;

/**
 * Stateless helper.
 * Validates quantity of rule annotations of the field.
 * Validates that array of annotations contains:
 * - no more than one {@link Rule} annotation
 * - no more than one {@link Rules} annotation
 * - if there is Rules(-s) exists, then Rule(-s) also has to be (to providing collection item rules)
 *
 * @author Il'dar Valitov
 * Created on 11.11.2022
 */
@RequiredArgsConstructor
public class RuleAnnotationsValidationHelper {

    public static void validate(Field field) throws DtoGeneratorValidationException {

        Annotation[] annotations = field.getAnnotations();
        int idx = 0;
        ResultDto resultDto = new ResultDto();

        try {

            countAnnotations(annotations, resultDto);

            if (resultDto.rulesNumber > 1) {
                resultDto.getErrorsDescription()
                        .append(++idx)
                        .append(". Found @Rule annotations for '")
                        .append(resultDto.rulesNumber)
                        .append("' different types, expected 1 or 0.")
                        .append("\n");
            }

            if (resultDto.rulesNumberRepeatable > 1) {
                resultDto.getErrorsDescription()
                        .append(++idx)
                        .append(". Found repeatable @Rule annotations for '")
                        .append(resultDto.rulesNumberRepeatable)
                        .append("' different types. Expected repeatable @Rule annotations for single type only.")
                        .append("\n");
            }

            if (resultDto.rulesNumber == 1 && resultDto.rulesNumberRepeatable == 1) {
                resultDto.getErrorsDescription()
                        .append(++idx)
                        .append(". Found @Rule annotations for '2' ")
                        .append("different types (one repeatable). Expected @Rule for single type only.")
                        .append("\n");
            }

            if (resultDto.collectionRulesNumber > 0) {

                if (resultDto.collectionRulesNumber != resultDto.rulesNumber + resultDto.rulesNumberWithinRepeatable) {
                    resultDto.getErrorsDescription()
                            .append(++idx)
                            .append(". Missed @Rule annotation for collection element.")
                            .append("\n");
                }

                Class<?> collectionType = field.getType();
                boolean collectionMatch = false;
                for (Class<?> knownType : resultDto.generatedTypesCollection) {
                    if (knownType.isAssignableFrom(collectionType)) {
                        collectionMatch = true;
                        break;
                    }
                }

                if (!collectionMatch) {
                    resultDto.getErrorsDescription()
                            .append(++idx)
                            .append(". Unknown collection type: '")
                            .append(collectionType)
                            .append("'. Expected types:\n")
                            .append(Arrays.asList(resultDto.generatedTypesCollection))
                            .append("\n");
                }

                if (resultDto.generatedTypes != null) {
                    boolean elementMatch = false;
                    Class<?> collectionElementType = ReflectionUtils.getSingleGenericType(field);
                    for (Class<?> knownElementType : resultDto.generatedTypes) {
                        if (knownElementType.isAssignableFrom(collectionElementType)) {
                            elementMatch = true;
                            break;
                        }
                    }

                    if (!elementMatch) {
                        resultDto.getErrorsDescription()
                                .append(++idx)
                                .append(". Wrong collection element type: '")
                                .append(collectionElementType)
                                .append("'. Expected types:\n")
                                .append(Arrays.asList(resultDto.generatedTypes))
                                .append("\n");
                    }
                }
            } else if (resultDto.generatedTypes != null) {
                boolean match = false;
                Class<?> generatedType = Primitives.wrap(field.getType());
                for (Class<?> knownElementType : resultDto.generatedTypes) {
                    if (knownElementType.isAssignableFrom(generatedType)) {
                        match = true;
                        break;
                    }
                }

                if (!match) {
                    resultDto.getErrorsDescription()
                            .append(++idx)
                            .append(". Wrong field type: '")
                            .append(generatedType)
                            .append("'. Expected types:\n")
                            .append(Arrays.asList(resultDto.generatedTypes))
                            .append("\n");
                }
            }

            if (resultDto.collectionInstanceClass != null) {
                int modifiers = resultDto.collectionInstanceClass.getModifiers();

                if (Modifier.isInterface(modifiers) || Modifier.isAbstract(modifiers)) {
                    resultDto.getErrorsDescription()
                            .append(++idx)
                            .append(". In @CollectionRules interface or abstract 'collectionClass' is defined: '")
                            .append(resultDto.collectionInstanceClass)
                            .append("'. Please specify subclass of Collection interface to instantiation.")
                            .append("\n");
                }

            }

        } catch (Exception e) {
            resultDto.getErrorsDescription()
                    .append(++idx)
                    .append(". Unexpected error.\n'")
                    .append(ExceptionUtils.getStackTrace(e))
                    .append("\n");
        }

        if (resultDto.isValidationFailed()) {
            throw new DtoGeneratorValidationException("Field: '" + field.getType() +
                    " " + field.getName() + "' - annotated wrong. See problems below:\n"
                    + resultDto.getErrorsDescription());
        }

    }

    private static void countAnnotations(Annotation[] annotations, final ResultDto resultDto) {

        for (Annotation annotation : annotations) {

            switch (RulesInfoHelper.getHelperType(annotation)) {

                case RULE:
                    resultDto.generatedTypes = GeneratedTypes.get(annotation.annotationType());
                case CUSTOM_RULE:
                    resultDto.rulesNumber++;
                    break;

                case RULES:
                    Annotation[] repeatableAnnotations = getRepeatableAnnotations(annotation);
                    resultDto.rulesNumberRepeatable++;
                    resultDto.rulesNumberWithinRepeatable = repeatableAnnotations.length;
                    resultDto.generatedTypes = GeneratedTypes.get(repeatableAnnotations[0].annotationType());
                    break;

                case RULE_FOR_COLLECTION:
                    CollectionRule collectionRule = (CollectionRule) annotation;
                    resultDto.collectionRulesNumber = 1;
                    resultDto.generatedTypesCollection = GeneratedTypes.get(collectionRule.annotationType());
                    resultDto.collectionInstanceClass = collectionRule.collectionClass();
                    break;

                case RULES_FOR_COLLECTION:
                    repeatableAnnotations = getRepeatableAnnotations(annotation);
                    resultDto.collectionRulesNumber = repeatableAnnotations.length;
                    collectionRule = (CollectionRule) repeatableAnnotations[0];
                    resultDto.generatedTypesCollection = GeneratedTypes.get(collectionRule.annotationType());
                    resultDto.collectionInstanceClass = collectionRule.collectionClass();
                    break;
            }

        }
    }

    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ResultDto {

        public Class<?>[] generatedTypes;
        public Class<?>[] generatedTypesCollection;
        public Class<?> collectionInstanceClass;
        private StringBuilder errorsDescription;
        private int rulesNumber = 0;
        private int rulesNumberRepeatable = 0;
        public int rulesNumberWithinRepeatable = 0;
        private int collectionRulesNumber = 0;

        public StringBuilder getErrorsDescription() {
            if (errorsDescription == null) {
                errorsDescription = new StringBuilder();
            }
            return errorsDescription;
        }

        public boolean isValidationFailed() {
            return errorsDescription != null && errorsDescription.length() > 0;
        }
    }

}
