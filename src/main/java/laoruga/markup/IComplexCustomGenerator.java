package laoruga.markup;

import java.lang.annotation.Annotation;

public interface IComplexCustomGenerator<GENERATED_TYPE, GENERATOR_MARKER extends Annotation, DTO_TYPE>
        extends ISimpleCustomGenerator<GENERATED_TYPE, GENERATOR_MARKER> {
    void prepareGenerator(DTO_TYPE generationRules);
}
