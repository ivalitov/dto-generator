package org.laoruga.dtogenerator.rules;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.laoruga.dtogenerator.api.rules.meta.Rule;
import org.laoruga.dtogenerator.api.rules.meta.RuleForCollection;
import org.laoruga.dtogenerator.api.rules.meta.Rules;
import org.laoruga.dtogenerator.api.rules.meta.RulesForCollection;
import org.laoruga.dtogenerator.exceptions.DtoGeneratorValidationException;

import java.lang.annotation.Annotation;

/**
 * Stateless helper.
 * Validates quantity of rule annotations of the field.
 * Validates that array of annotations contains:
 * - no more than one {@link Rule} annotation
 * - no more than one {@link Rules} annotation
 * - no more than one {@link RuleForCollection} annotation
 * - no more than one {@link RulesForCollection} annotation
 * - if there is RuleForCollection(-s) exists, then Rule(-s) also has to be (to providing collection item rules)
 *
 * @author Il'dar Valitov
 * Created on 11.11.2022
 */
@RequiredArgsConstructor
public class RuleAnnotationsValidationHelper {

    public static void validate(Annotation[] annotations) throws DtoGeneratorValidationException {

        ResultDto resultDto = countAnnotations(annotations);

        int idx = 0;

        if (resultDto.generalRule > 1) {
            resultDto.getErrorsDescription()
                    .append(++idx)
                    .append(". Found '")
                    .append(resultDto.generalRule)
                    .append("' @Rule annotations for various types, ")
                    .append("expected 1 or 0.")
                    .append("\n");
        }

        if (resultDto.groupOfGeneralRules > 1) {
            resultDto.getErrorsDescription()
                    .append(++idx)
                    .append(". Found '")
                    .append(resultDto.groupOfGeneralRules)
                    .append("' @Rules annotations for various types, expected @Rules for single type only.")
                    .append("\n");
        }

        if (resultDto.collectionRule > 1) {
            resultDto.getErrorsDescription()
                    .append(++idx)
                    .append(". Found '")
                    .append(resultDto.collectionRule)
                    .append("' @CollectionRule annotations for various collection types, expected 1 or 0.")
                    .append("\n");
        }

        if (resultDto.groupOfCollectionRules > 1) {
            resultDto.getErrorsDescription()
                    .append(++idx)
                    .append(". Found '")
                    .append(resultDto.groupOfCollectionRules)
                    .append("' @CollectionRules annotations for various collection types, ")
                    .append("expected @CollectionRules for single collection type only.")
                    .append("\n");
        }

        if ((resultDto.getSumOfCollectionRules() > 0) &&
                (resultDto.getSumOfCollectionRules() != resultDto.getSumOfGeneralRules())) {
            resultDto.getErrorsDescription()
                    .append(++idx)
                    .append(". Missed @Rule annotation for item of collection.")
                    .append("\n");
        }

        if (resultDto.isValidationFailed()) {
            throw new DtoGeneratorValidationException("Field annotated wrong:\n"
                    + resultDto.getErrorsDescription());
        }

    }

    private static ResultDto countAnnotations(Annotation[] annotations) {
        final ResultDto resultDto = new ResultDto();

        for (Annotation annotation : annotations) {

            switch (RulesInfoHelper.getHelperType(annotation)) {

                case RULE:
                    resultDto.generalRule++;
                    break;
                case RULE_FOR_COLLECTION:
                    resultDto.collectionRule++;
                    break;
                case RULES:
                    resultDto.groupOfGeneralRules++;
                    break;
                case RULES_FOR_COLLECTION:
                    resultDto.groupOfCollectionRules++;
                    break;
            }

        }

        return resultDto;
    }

    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ResultDto {

        private StringBuilder errorsDescription;
        private int generalRule = 0;
        private int groupOfGeneralRules = 0;
        private int collectionRule = 0;
        private int groupOfCollectionRules = 0;

        public StringBuilder getErrorsDescription() {
            if (errorsDescription == null) {
                errorsDescription = new StringBuilder();
            }
            return errorsDescription;
        }

        public boolean isValidationFailed() {
            return errorsDescription != null && errorsDescription.length() > 0;
        }

        public int getRulesAnnotationsNumber() {
            return generalRule + groupOfGeneralRules + collectionRule + groupOfCollectionRules;
        }

        int getSumOfCollectionRules() {
            return collectionRule + groupOfCollectionRules;
        }

        int getSumOfGeneralRules() {
            return generalRule + groupOfGeneralRules;
        }
    }

}
