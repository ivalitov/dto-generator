package org.laoruga.dtogenerator;

import org.laoruga.dtogenerator.rules.RulesInfoExtractor;

/**
 * @author Il'dar Valitov
 * Created on 05.02.2023
 */
public class UtilsRoot {

    public static RulesInfoExtractor getExtractorInstance(String... groups) {
        FieldGroupFilter fieldGroupFilter;
        if (groups.length == 0) {
            fieldGroupFilter = new FieldGroupFilter();
        } else {
            fieldGroupFilter = new FieldGroupFilter();
            fieldGroupFilter.includeGroups(groups);
        }
        return new RulesInfoExtractor(fieldGroupFilter);
    }

}
