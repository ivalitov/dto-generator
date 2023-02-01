package org.laoruga.dtogenerator.util;


import org.apache.commons.lang3.tuple.Pair;

import static org.laoruga.dtogenerator.DtoGeneratorBuildersTree.ROOT;

/**
 * @author Il'dar Valitov
 * Created on 31.01.2023
 */
public class StringUtils {

    /**
     * @param fieldsFromRoot - string may contain field name or fields sequence separated by dots.
     *                       For example: 'person.age'
     * @return left - field name; right - path to field started with '%ROOT%'.
     * For example: Left - 'age'; Right - ['%ROOT%', 'person']
     */
    public static Pair<String, String[]> splitPath(String fieldsFromRoot) {
        String fieldName;
        String[] path;
        if (fieldsFromRoot.contains(".")) {
            path = fieldsFromRoot.split("\\.");
            fieldName = path[path.length - 1];
            for (int i = path.length - 2; i >= 0; i--) {
                path[i + 1] = path[i];
            }
            path[0] = ROOT;
        } else {
            fieldName = fieldsFromRoot;
            path = new String[]{ROOT};
        }
        return Pair.of(fieldName, path);
    }
}
