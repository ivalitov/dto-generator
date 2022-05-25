package laoruga.dtogenerator.api.markup.generators;

/**
 * @author Il'dar Valitov
 * Created on 28.04.2022
 */

public interface ICollectionGenerator<GENERATED_TYPE> extends IGenerator<GENERATED_TYPE> {

    IGenerator<?> getInnerGenerator();
}
