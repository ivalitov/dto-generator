package laoruga.markup.bounds;

import laoruga.ChField;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface EnumFieldBounds {
    String[] possibleValues();
    String className();
}
