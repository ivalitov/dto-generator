package laoruga.dtogenerator.api;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.lang.annotation.Annotation;

import static laoruga.dtogenerator.api.RulesInfoHelper.*;

/**
 * @author Il'dar Valitov
 * Created on 11.11.2022
 */
@RequiredArgsConstructor
class AnnotationErrorsHandler {

    private final Annotation[] annotations;
    private final ResultDto resultDto = new ResultDto();

    void count() {
        for (Annotation annotation : annotations) {

            if (isItRule(annotation)) {
                resultDto.generalRule++;
            }

            if (isItMultipleRules(annotation)) {
                resultDto.groupOfGeneralRules++;
            }

            if (isItCollectionRule(annotation)) {
                resultDto.collectionRule++;
            }

            if (isItCollectionRules(annotation)) {
                resultDto.groupOfCollectionRules++;
            }
        }
    }

    public ResultDto validate() {
        count();
        int idx = 0;

        if (resultDto.generalRule > 1) {
            resultDto.resultString
                    .append(++idx)
                    .append(". Found '")
                    .append(resultDto.generalRule)
                    .append("' @Rule annotations for various types, ")
                    .append("expected 1 or 0.")
                    .append("\n");
        }

        if (resultDto.groupOfGeneralRules > 1) {
            resultDto.resultString
                    .append(++idx)
                    .append(". Found '")
                    .append(resultDto.generalRule)
                    .append("' @Rules annotations for various types, expected @Rules for single type only.")
                    .append("\n");
        }

        if (resultDto.collectionRule > 1) {
            resultDto.resultString
                    .append(++idx)
                    .append(". Found '")
                    .append(resultDto.collectionRule)
                    .append("' @CollectionRule annotations for various collection types, expected 1 or 0.")
                    .append("\n");
        }

        if (resultDto.groupOfGeneralRules > 1) {
            resultDto.resultString
                    .append(++idx)
                    .append(". Found '")
                    .append(resultDto.generalRule)
                    .append("' @CollectionRules annotations for various collection types, ")
                    .append("expected @CollectionRules for single collection type only.")
                    .append("\n");
        }

        if ((resultDto.getSumOfCollectionRules() > 0) &&
                (resultDto.getSumOfCollectionRules() != resultDto.getSumOfGeneralRules())) {
            resultDto.resultString
                    .append(++idx)
                    .append(". Missed @Rule annotation for item of collection.")
                    .append("\n");
        }

        return resultDto;
    }

    @Getter
    static class ResultDto {

        private final StringBuilder resultString = new StringBuilder();
        private int generalRule = 0;
        private int groupOfGeneralRules = 0;
        private int collectionRule = 0;
        private int groupOfCollectionRules = 0;

        public String getResultString() {
            return resultString.toString();
        }

        int getSumOfCollectionRules() {
            return collectionRule + groupOfCollectionRules;
        }

        int getSumOfGeneralRules() {
            return generalRule + groupOfGeneralRules;
        }
    }

}
