package org.laoruga.dtogenerator;

import org.laoruga.dtogenerator.constants.Group;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Container of groups of the generation rules that have to be called.
 *
 * @author Il'dar Valitov
 * Created on 18.06.2022
 */
public class FieldFilter {

    private boolean isNew = true;
    private final Set<String> includeGroups = new HashSet<>();

    public FieldFilter() {
        includeGroups.add(Group.DEFAULT);
    }

    void includeGroups(String... groups) {
        if (isNew) {
            isNew = false;
            includeGroups.clear();
        }
        includeGroups.addAll(Arrays.asList(groups));
    }

    public boolean isContainsIncludeGroup(String group) {
        return includeGroups.contains(group);
    }

}
