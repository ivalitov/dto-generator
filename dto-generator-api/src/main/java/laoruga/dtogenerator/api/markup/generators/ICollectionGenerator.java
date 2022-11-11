package laoruga.dtogenerator.api.markup.generators;

/**
 * @author Il'dar Valitov
 * Created on 28.04.2022
 */

public interface ICollectionGenerator<T> extends IGenerator<T> {

    IGenerator<?> getItemGenerator();
}
