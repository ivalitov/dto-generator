package laoruga.markup;

public interface IObjectDependentCustomGenerator<GENERATED_TYPE, DEPENDENT_TYPE> extends IGenerator<GENERATED_TYPE> {
    void setDependentObject(DEPENDENT_TYPE generatedDto);
    boolean isObjectReady();
}
