package org.laoruga.dtogenerator.constants;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * Just string constants.
 * Every rule annotation contains {@link Group#DEFAULT} group by default.
 * Except the {@link Group#DEFAULT} there is no logic behind these particular values.
 *
 * @author Il'dar Valitov
 * Created on 31.05.2022
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class Group {

    public static final String DEFAULT = "DEFAULT";
    public static final String REQUIRED = "REQUIRED";
    public static final String GROUP_1 = "GROUP_1";
    public static final String GROUP_2 = "GROUP_2";
    public static final String GROUP_3 = "GROUP_3";
    public static final String GROUP_4 = "GROUP_4";
    public static final String GROUP_5 = "GROUP_5";

}
