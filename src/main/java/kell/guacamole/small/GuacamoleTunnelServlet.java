package kell.guacamole.small;

import kell.guacamole.small.util.EnvVarParser;
import org.apache.logging.log4j.LogManager;
import javax.servlet.http.HttpServletRequest;
import org.apache.guacamole.GuacamoleException;
import org.apache.guacamole.net.GuacamoleSocket;
import org.apache.guacamole.net.GuacamoleTunnel;
import org.apache.guacamole.net.InetGuacamoleSocket;
import org.apache.guacamole.net.SimpleGuacamoleTunnel;
import org.apache.guacamole.protocol.ConfiguredGuacamoleSocket;
import org.apache.guacamole.servlet.GuacamoleHTTPTunnelServlet;
import org.apache.logging.log4j.Logger;

public class GuacamoleTunnelServlet
        extends GuacamoleHTTPTunnelServlet {
    public static String CONTROLLER_URL_VAR = "CONTROLLER_URL";
    public static String GUACD_HOST_VAR =  "GUACD_HOST";
    public static String GUACD_PORT_VAR = "GUACD_PORT";

    Logger logger = LogManager.getLogger(GuacamoleTunnelServlet.class);

    @Override
    protected GuacamoleTunnel doConnect(HttpServletRequest request)
            throws GuacamoleException {
        logger.debug("Http request received from {}. Content: {}", request.getSession().getId(), request.toString());
        String appUrl = EnvVarParser.getStringVar(CONTROLLER_URL_VAR);
        String guacdHost = EnvVarParser.getStringVar(GUACD_HOST_VAR);
        int guacdPort = EnvVarParser.getIntegerVar(GUACD_PORT_VAR);

        VMconnection conn = new VMconnection(request, appUrl);
        GuacamoleSocket socket = new ConfiguredGuacamoleSocket(
                new InetGuacamoleSocket(guacdHost, guacdPort),
                conn.getConfig(appUrl)
        );

        logger.debug("Guacamole tunnel for {} is ready", request.getSession().getId());
        // Return a new tunnel which uses the connected socket
        return new SimpleGuacamoleTunnel(socket);
    }
}