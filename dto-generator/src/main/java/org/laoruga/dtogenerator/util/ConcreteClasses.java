package org.laoruga.dtogenerator.util;

import org.laoruga.dtogenerator.exceptions.DtoGeneratorException;

import java.lang.reflect.Modifier;
import java.util.*;

/**
 * @author Il'dar Valitov
 * Created on 20.03.2023
 */
public final class ConcreteClasses {
    public static Class<?> getConcreteCollectionClass(Class<? extends Collection<?>> fieldType) {

        if (!Modifier.isInterface(fieldType.getModifiers()) && !Modifier.isAbstract(fieldType.getModifiers())) {
            return fieldType;
        }

        if (List.class.isAssignableFrom(fieldType)) {
            return ArrayList.class;
        } else if (Set.class.isAssignableFrom(fieldType)) {
            return HashSet.class;
        } else if (Queue.class.isAssignableFrom(fieldType)) {
            return PriorityQueue.class;
        } else if (Collection.class.isAssignableFrom(fieldType)) {
            return ArrayList.class;
        } else {
            throw new DtoGeneratorException("Unsupported collection type: '" + fieldType.getTypeName() + "'");
        }

    }

    public static Class<?> getConcreteMapClass(Class<? extends Map<?, ?>> fieldType) {

        if (!Modifier.isInterface(fieldType.getModifiers()) && !Modifier.isAbstract(fieldType.getModifiers())) {
            return fieldType;
        }

        if (SortedMap.class.isAssignableFrom(fieldType)) {
            return TreeMap.class;
        } else if (Map.class.isAssignableFrom(fieldType)) {
            return HashMap.class;
        } else {
            throw new DtoGeneratorException("Unsupported map type: '" + fieldType.getTypeName() + "'");
        }
    }

}
