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

    private Set<Group> include;
    private Set<Group> exclude;

    void includeGroups(Group... groups) {
        if (include == null) {
            include = new HashSet<>();
        }
        include.addAll(Arrays.asList(groups));
    }

    void excludeGroups(Group... groups) {
        if (exclude == null) {
            exclude = new HashSet<>();
        }
        exclude.addAll(Arrays.asList(groups));
    }

    int includesCount() {
        if (include == null) {
            return 0;
        } else {
            return include.size();
        }
    }

    boolean isContainsIncludeGroup(Group group) {
        if (include != null) {
            return include.contains(group);
        } else {
            return false;
        }
    }

    boolean isContainsExcludeGroup(Group group) {
        if (exclude != null) {
            return exclude.contains(group);
        } else {
            return false;
        }
    }

}
