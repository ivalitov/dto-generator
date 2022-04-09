package laoruga.markup;

import java.lang.annotation.Annotation;

public interface IRulesDependentCustomGenerator<GENERATED_TYPE, GENERATION_RULES extends Annotation> extends IGenerator<GENERATED_TYPE> {
    void prepareGenerator(GENERATION_RULES generationRules);
}
