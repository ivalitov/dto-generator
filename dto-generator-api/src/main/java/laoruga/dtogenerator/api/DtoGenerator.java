package laoruga.dtogenerator.api;

import laoruga.dtogenerator.api.exceptions.DtoGeneratorException;
import laoruga.dtogenerator.api.generatorsexecutor.BatchGeneratorsExecutor;
import laoruga.dtogenerator.api.generatorsexecutor.ExecutorOfCollectionGenerator;
import laoruga.dtogenerator.api.generatorsexecutor.ExecutorOfDtoDependentGenerator;
import laoruga.dtogenerator.api.generatorsexecutor.ExecutorOfGenerator;
import laoruga.dtogenerator.api.markup.generators.IGenerator;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static laoruga.dtogenerator.api.util.ReflectionUtils.createInstance;

/**
 * @author Il'dar Valitov
 * Created on 16.04.2022
 */

@Slf4j
public class DtoGenerator<DTO_TYPE> {

    private final DTO_TYPE dtoInstance;

    @Getter(AccessLevel.PACKAGE)
    private final TypeGeneratorsProvider typeGeneratorsProvider;
    @Getter(AccessLevel.PACKAGE)
    private final Map<Field, IGenerator<?>> fieldGeneratorMap = new LinkedHashMap<>();
    @Getter(AccessLevel.PACKAGE)
    private final DtoGeneratorBuilder<DTO_TYPE> builderInstance;


    private final Map<Field, Exception> errors = new HashMap<>();

    protected DtoGenerator(TypeGeneratorsProvider typeGeneratorsProvider, DtoGeneratorBuilder<DTO_TYPE> dtoGeneratorBuilder) {
        this.typeGeneratorsProvider = typeGeneratorsProvider;
        this.builderInstance = dtoGeneratorBuilder;
        this.dtoInstance = (DTO_TYPE) typeGeneratorsProvider.getDtoInstance();
    }

    public static <T> DtoGeneratorBuilder<T> builder(Class<T> dtoClass) {
        return new DtoGeneratorBuilder<>(createInstance(dtoClass));
    }

    public static <T> DtoGeneratorBuilder<T> builder(T dtoInstance) {
        return new DtoGeneratorBuilder<>(dtoInstance);
    }

    public DTO_TYPE generateDto() {
        prepareGeneratorsRecursively(dtoInstance.getClass());
        applyGenerators();
        return dtoInstance;
    }

    private void prepareGeneratorsRecursively(Class<?> dtoClass) {
        if (dtoClass.getSuperclass() != null) {
            prepareGeneratorsRecursively(dtoClass.getSuperclass());
        }
        prepareGenerators(dtoClass);
    }

    void applyGenerators() {
        // TODO move into params
        int maxAttempts = 100;

        ExecutorOfDtoDependentGenerator executorsChain =
                new ExecutorOfDtoDependentGenerator(
                        new ExecutorOfCollectionGenerator(
                                new ExecutorOfGenerator(dtoInstance)));

        BatchGeneratorsExecutor batchGeneratorsExecutor = new BatchGeneratorsExecutor(
                executorsChain, getFieldGeneratorMap(), maxAttempts);

        batchGeneratorsExecutor.execute();
    }

    void prepareGenerators(Class<?> dtoClass) {
        for (Field field : dtoClass.getDeclaredFields()) {
            IGenerator<?> generator = null;
            try {
                generator = getTypeGeneratorsProvider().getGenerator(field);
            } catch (Exception e) {
                errors.put(field, e);
            }
            if (generator != null) {
                getFieldGeneratorMap().put(field, generator);
            }
        }
        if (!errors.isEmpty()) {
            final AtomicInteger counter = new AtomicInteger(0);
            String formattedErrors = errors.entrySet().stream()
                    .map(fieldExceptionEntry ->
                            "- [" + counter.incrementAndGet() + "] Field: '" + fieldExceptionEntry.getKey().toString() + "'\n" +
                                    "- [" + counter.get() + "] Exception:\n" +
                                    ExceptionUtils.getStackTrace(fieldExceptionEntry.getValue())
                    )
                    .collect(Collectors.joining("\n"));
            log.error("{} error(s) while generators preparation. See problems below: \n" + formattedErrors, errors.size());
            throw new DtoGeneratorException("Error while generators preparation (see log above)");
        }
        if (getFieldGeneratorMap().isEmpty()) {
            log.debug("No generators have been found");
        } else {
            log.debug(getFieldGeneratorMap().size() + " generators was created for fields: " + getFieldGeneratorMap().keySet());
        }
    }
}
