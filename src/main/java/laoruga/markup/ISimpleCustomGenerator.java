package laoruga.markup;

import java.lang.annotation.Annotation;

public interface ISimpleCustomGenerator<GENERATED_TYPE, GENERATOR_MARKER extends Annotation> extends IGenerator<GENERATED_TYPE> {
    void prepareGenerator(GENERATOR_MARKER generationRules);
}
