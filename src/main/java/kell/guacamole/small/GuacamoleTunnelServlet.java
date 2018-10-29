package kell.guacamole.small;

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

    Logger logger = LogManager.getLogger(GuacamoleTunnelServlet.class);

    @Override
    protected GuacamoleTunnel doConnect(HttpServletRequest request)
            throws GuacamoleException {
        String appUrl = "http://controller:5876/api/v1";
        logger.debug("Http request received from {}. Content: {}", request.getSession().getId(), request.toString());
        VMconnection conn = new VMconnection(request, appUrl);
        GuacamoleSocket socket = new ConfiguredGuacamoleSocket(
                new InetGuacamoleSocket("guacd", 4822),
                conn.getConfig(appUrl)
        );

        logger.debug("Guacamole tunnel for {} is ready", request.getSession().getId());
        // Return a new tunnel which uses the connected socket
        return new SimpleGuacamoleTunnel(socket);
    }
}