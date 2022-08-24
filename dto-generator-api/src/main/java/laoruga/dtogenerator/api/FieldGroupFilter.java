package laoruga.dtogenerator.api;

import laoruga.dtogenerator.api.constants.Group;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Il'dar Valitov
 * Created on 18.06.2022
 */
class FieldGroupFilter {

    private final Set<String> include = new HashSet<>();

    FieldGroupFilter validateGroups() {
        if (include.isEmpty()) {
            include.add(Group.DEFAULT);
        }
        return this;
    }

    void includeGroups(String... groups) {
        include.addAll(Arrays.asList(groups));
    }

    boolean isContainsIncludeGroup(String group) {
        return include.contains(group);
    }

    int getGroupsCount() {
        return include.size();
    }

}
