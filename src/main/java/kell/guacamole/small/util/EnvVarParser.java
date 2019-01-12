package kell.guacamole.small.util;

import kell.guacamole.small.exceptions.TortillaException;

import java.util.Optional;

public class EnvVarParser {
    public static String getStringVar(String variable) throws TortillaException {
       return Optional.ofNullable(System.getenv(variable))
                      .orElseThrow(() -> TortillaException.generateEnvVarException(variable));
    }
    public static int getIntegerVar(String variable) throws TortillaException {
        try {
            return Integer.parseInt(getStringVar(variable));
        } catch (NumberFormatException e) {
            TortillaException wrappingException = TortillaException.generateEnvVarException(variable);
            wrappingException.initCause(e);
            throw wrappingException;
        }

    }
}
