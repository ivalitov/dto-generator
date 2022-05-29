package laoruga.dtogenerator.api.util;

import laoruga.dtogenerator.api.exceptions.DtoGeneratorException;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Modifier;

/**
 * @author Il'dar Valitov
 * Created on 30.05.2022
 */
@Slf4j
public class Utils {

    /**
     * 1. Filed type should be assignable from required collectionClass
     * 2. CollectionClass should not be an interface or abstract
     *
     * @param fieldType checking dto field type
     */
    public static <T> T createCollectionFieldInstance(Class<?> fieldType, Class<T> collectionClass) {
        if (!fieldType.isAssignableFrom(collectionClass)) {
            throw new DtoGeneratorException("CollectionClass from rules: '" + collectionClass + "' can't" +
                    " be assign to the field: " + fieldType);
        }
        if (collectionClass.isInterface() || Modifier.isAbstract(collectionClass.getModifiers())) {
            throw new DtoGeneratorException("Can't create instance of '" + collectionClass + "' because" +
                    " it is interface or abstract.");
        }
        T collectionInstance;
        try {
            collectionInstance = collectionClass.newInstance();
        } catch (Exception e) {
            log.error("Exception while creating Collection instance ", e);
            throw new DtoGeneratorException(e);
        }
        return collectionInstance;
    }
}
