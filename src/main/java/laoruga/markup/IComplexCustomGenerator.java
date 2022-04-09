package laoruga.markup;

import java.lang.annotation.Annotation;

public interface IComplexCustomGenerator<GENERATED_TYPE, GENERATION_RULES extends Annotation, GENERATED_DTO> extends IRulesDependentCustomGenerator<GENERATED_TYPE, GENERATION_RULES> {
    void setGeneratedDto(GENERATED_DTO generatedDto);
}
