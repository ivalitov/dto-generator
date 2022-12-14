package org.laoruga.dtogenerator;

import org.laoruga.dtogenerator.constants.Group;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Il'dar Valitov
 * Created on 18.06.2022
 */
public class FieldGroupFilter {

    private boolean isNew = true;

    public FieldGroupFilter(String... groups) {
        if (groups.length == 0) {
            include.add(Group.DEFAULT);
        } else {
            include.addAll(Arrays.asList(groups));
        }
    }

    private final Set<String> include = new HashSet<>();

    void includeGroups(String... groups) {
        if (isNew) {
            include.clear();
            isNew = false;
        }
        include.addAll(Arrays.asList(groups));
    }

    public boolean isContainsIncludeGroup(String group) {
        return include.contains(group);
    }

}
