package laoruga.dtogenerator.api.generatorsexecutor;

import laoruga.dtogenerator.api.exceptions.DtoGeneratorException;
import laoruga.dtogenerator.api.markup.generators.ICustomGeneratorDtoDependent;
import laoruga.dtogenerator.api.markup.generators.IGenerator;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Field;

/**
 * @author Il'dar Valitov
 * Created on 09.11.2022
 */
@Slf4j
public class ExecutorOfDtoDependentGenerator extends AbstractExecutor {

    public ExecutorOfDtoDependentGenerator(AbstractExecutor nextGenerators) {
        super(nextGenerators);
    }

    @Override
    public boolean execute(Field field, IGenerator<?> generator) {
        if (!isDtoReadyForFieldGeneration(generator)) {
            return false;
        }
        return executeNext(field, generator);
    }

    /**
     * Check whether DTO is ready for using CustomGeneratorDtoDependent or not.
     * There is limited attempts to prevent infinite loops.
     *
     * @param generator   - generator to check
     * @return - doesn't DTO ready?
     * @throws DtoGeneratorException - throws if all attempts are spent
     */
    protected boolean isDtoReadyForFieldGeneration(IGenerator<?> generator) throws DtoGeneratorException {
        if (generator instanceof ICustomGeneratorDtoDependent) {
            boolean dtoReady = ((ICustomGeneratorDtoDependent<?, ?>) generator).isDtoReady();
            log.debug("Object {} ready to generate dependent field value", dtoReady ? "is" : "isn't");
            return dtoReady;
        }
        return true;
    }
}