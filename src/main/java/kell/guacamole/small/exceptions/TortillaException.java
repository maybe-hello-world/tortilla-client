package kell.guacamole.small.exceptions;

import com.google.gson.JsonObject;
import org.apache.guacamole.GuacamoleClientException;

public class TortillaException extends GuacamoleClientException {

    public TortillaException(String pStatus, String pReason, String pHuman) {
        super(getErrorJson(pStatus, pReason, pHuman));
    }

    public static String getErrorJson(String pStatus, String pReason, String pHuman) {
        JsonObject err_json = new JsonObject();
        err_json.addProperty("status", pStatus);
        err_json.addProperty("reason", pReason);
        err_json.addProperty("human_reason", pHuman);
        return err_json.toString();
    }

    /**
     * To generate exception if any environment variable is absent
     * @param variable variable name
     * @return generated exception
     */
    public static TortillaException generateEnvVarException( String variable){
        return new TortillaException("500", "internal", "Can't read environment variable. " +
                "Please specify " + variable + " environment variable");
    }
}
