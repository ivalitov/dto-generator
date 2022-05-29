package laoruga.dtogenerator.api.util;

import laoruga.dtogenerator.api.exceptions.DtoGeneratorException;
import org.apache.commons.math3.util.Pair;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author Il'dar Valitov
 * Created on 25.05.2022
 */
public class ReflectionUtils {

    List list_1;
    List<String> list_2;
    List<?> list_3;
    List<? extends String> list_4;
    List<List<String>> list_5;
    List<Set> list_6;
    Map map_1;
    Map<String, Integer> map_2;
    Map<String, Map<String, ?>> map_22;
    Map<String, Map<String, String>> map_33;
    Map<?, ?> map_3;
    String string;

    public static void main(String[] args) {
        System.out.println();
    }

    public static Class<?> getGenericType(Field field) throws DtoGeneratorException {
        return (Class<?>) getGenericTypeOrPair(field);
    }

    public static Pair<Class<?>, Class<?>> getGenericTypesPair(Field field) throws DtoGeneratorException {
        return (Pair<Class<?>, Class<?>>) getGenericTypeOrPair(field);
    }

    static Object getGenericTypeOrPair(Field field) throws DtoGeneratorException {
        String typeName = field.getGenericType().getTypeName();
        char[] chars = typeName.toCharArray();
        Integer leftIdx = null;
        Integer rightIdx = null;
        boolean multipleTypes = false;
        for (int i = 0; i < chars.length; i++) {
            if (chars[i] == '<') {
                if (leftIdx == null) {
                    leftIdx = i + 1;
                } else {
                    throw new DtoGeneratorException("Can't generate via default generator," +
                            " please create custom generator for type: " + typeName);
                }
            } else if (chars[i] == '>') {
                rightIdx = i;
                break;
            } else if (chars[i] == ',') {
                if (multipleTypes) {
                    throw new DtoGeneratorException("Can't generate via default generator, " +
                            "please create custom generator for type: " + typeName);
                }
                multipleTypes = true;
            } else if (chars[i] == '?') {
                throw new DtoGeneratorException("Can't generate wildcard type via default generator," +
                        " please create custom generator for type: " + typeName);
            }
        }
        if (rightIdx == null || leftIdx == null) {
            throw new DtoGeneratorException("Can't generate raw type via default generator," +
                    " please create custom generator for type: " + typeName);
        } else {
            String className = typeName.substring(leftIdx, rightIdx);
            if (multipleTypes) {
                String[] split = className.split(",");
                return new Pair<>(getClass(split[0], typeName), getClass(split[1], typeName));
            } else {
                return getClass(className, typeName);
            }
        }
    }

    private static Class<?> getClass(String className, String typeName) {
        try {
            return Class.forName(className);
        } catch (ClassNotFoundException e) {
            throw new DtoGeneratorException("Can't create class for name: '" + className + "' obtained from" +
                    " type: '" + typeName + "'", e);
        }
    }
}
