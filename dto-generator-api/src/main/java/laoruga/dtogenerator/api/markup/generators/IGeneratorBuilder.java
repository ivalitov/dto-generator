package laoruga.dtogenerator.api.markup.generators;

/**
 * @author Il'dar Valitov
 * Created on 18.05.2022
 */

@FunctionalInterface
public interface IGeneratorBuilder<T extends IGenerator<?>> {

    T build();
}
