package org.laoruga.dtogenerator.typegenerators.executors;

import lombok.extern.slf4j.Slf4j;
import org.laoruga.dtogenerator.api.generators.IGenerator;
import org.laoruga.dtogenerator.api.generators.custom.ICustomGeneratorDtoDependent;
import org.laoruga.dtogenerator.exceptions.DtoGeneratorException;
import org.laoruga.dtogenerator.typegenerators.CustomGenerator;

import java.lang.reflect.Field;
import java.util.function.Supplier;

/**
 * @author Il'dar Valitov
 * Created on 09.11.2022
 */
@Slf4j
public class ExecutorOfDtoDependentGenerator extends ExecutorOfGenerator {

    public ExecutorOfDtoDependentGenerator(Supplier<?> dtoInstanceSupplier,
                                           AbstractExecutor nextGenerator) {
        super(dtoInstanceSupplier, nextGenerator);
    }

    @Override
    public boolean execute(Field field, IGenerator<?> generator) {
        if (isItDtoDependentGenerator(generator)) {
            if (isDtoReadyForFieldGeneration(generator)) {
                return super.execute(field, generator);
            }
            return false;
        }
        return executeNext(field, generator);
    }

    /**
     * Check whether DTO is ready for using CustomGeneratorDtoDependent or not.
     * There is limited attempts to prevent infinite loops.
     *
     * @param generator - generator to check
     * @return - doesn't DTO ready?
     * @throws DtoGeneratorException - throws if all attempts are spent
     */
    protected boolean isDtoReadyForFieldGeneration(IGenerator<?> generator) throws DtoGeneratorException {
        IGenerator<?> usersGeneratorInstance = ((CustomGenerator) generator).getUsersGeneratorInstance();
        boolean dtoReady = ((ICustomGeneratorDtoDependent<?, ?>) usersGeneratorInstance).isDtoReady();
        log.debug("Object {} ready to generate dependent field value", dtoReady ? "is" : "isn't");
        return dtoReady;
    }

    protected boolean isItDtoDependentGenerator(IGenerator<?> generator) {
        if (generator instanceof CustomGenerator) {
            return ((CustomGenerator) generator).getUsersGeneratorInstance()
                    instanceof ICustomGeneratorDtoDependent;
        }
        return false;
    }
}
