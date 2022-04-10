package dtogenerator.examples;

import java.util.Arrays;

public enum SystemType {
    EI,
    ASSD,
    GP,
    NBCH;

    public static String[] getNames() {
        return Arrays.stream(SystemType.values()).map(Enum::name).toArray(String[]::new);
    }
}
