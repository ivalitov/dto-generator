package laoruga.dtogenerator.api;

import laoruga.dtogenerator.api.constants.Group;
import laoruga.dtogenerator.api.exceptions.DtoGeneratorException;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Il'dar Valitov
 * Created on 18.06.2022
 */
class FieldGroupFilter {

    private final Set<Group> include = new HashSet<>();

    FieldGroupFilter validateGroups() {
        if (include.isEmpty()) {
            include.add(Group.DEFAULT);
        }
        return this;
    }

    void includeGroups(Group... groups) {
        include.addAll(Arrays.asList(groups));
    }

    boolean isContainsIncludeGroup(Group group) {
        return include.contains(group);
    }

}
