package laoruga.markup;

import java.lang.annotation.Annotation;

public interface ICustomGenerator<GENERATED_TYPE, GENERATOR_MARKER extends Annotation> extends IGenerator<GENERATED_TYPE>{
    void prepareGenerator(GENERATOR_MARKER generationRules);
}
