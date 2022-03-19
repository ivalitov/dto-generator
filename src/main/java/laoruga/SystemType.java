package laoruga;

import java.util.Arrays;
import java.util.stream.Collectors;

public enum SystemType {
    ASSD,
    GP,
    NBCH;

    public static String[] getNames() {
        return Arrays.stream(SystemType.values()).map(Enum::name).toArray(String[]::new);
    }
}
