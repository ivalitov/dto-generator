package laoruga.dtogenerator.api;

import lombok.RequiredArgsConstructor;

import java.lang.annotation.Annotation;

import static laoruga.dtogenerator.api.RulesInfoHelper.*;

/**
 * @author Il'dar Valitov
 * Created on 11.11.2022
 */
@RequiredArgsConstructor
class AnnotationErrorsHandler {

    StringBuilder result = new StringBuilder();
    private final Annotation[] annotations;

    int generalRule = 0;
    int groupOfGeneralRules = 0;
    int collectionRule = 0;
    int groupOfCollectionRules = 0;

    void count() {
        for (Annotation annotation : annotations) {

            if (isItRule(annotation)) {
                generalRule++;
            }

            if (isItRules(annotation)) {
                groupOfGeneralRules++;
            }

            if (isItCollectionRule(annotation)) {
                collectionRule++;
            }

            if (isItCollectionRules(annotation)) {
                groupOfCollectionRules++;
            }
        }
    }

    public String validate() {
        count();
        int idx = 0;

        if (generalRule > 1) {
            result.append(++idx + ". Found '" + generalRule + "' @Rule annotations for various types, " +
                    "expected 1 or 0.").append("\n");
        }

        if (groupOfGeneralRules > 1) {
            result.append(++idx + ". Found '" + generalRule + "' @Rules annotations for various types, " +
                    "expected @Rules for single type only.").append("\n");
        }

        if (collectionRule > 1) {
            result.append(++idx + ". Found '" + collectionRule + "' @CollectionRule annotations for various collection types, " +
                    "expected 1 or 0.").append("\n");
        }

        if (groupOfGeneralRules > 1) {
            result.append(++idx + ". Found '" + generalRule + "' @CollectionRules annotations for various collection types, " +
                    "expected @CollectionRules for single collection type only.").append("\n");
        }

        if ((collectionRule + groupOfCollectionRules > 0) && (generalRule + groupOfGeneralRules == 0)) {
            result.append(++idx + ". Missed @Rule annotation for item of collection.").append("\n");
        }

        return result.toString();
    }

}
