package kell.guacamole.small;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;

import org.apache.guacamole.GuacamoleException;
import org.apache.guacamole.net.GuacamoleSocket;
import org.apache.guacamole.net.GuacamoleTunnel;
import org.apache.guacamole.net.InetGuacamoleSocket;
import org.apache.guacamole.net.SimpleGuacamoleTunnel;
import org.apache.guacamole.protocol.ConfiguredGuacamoleSocket;
import org.apache.guacamole.servlet.GuacamoleHTTPTunnelServlet;

public class GuacamoleTunnelServlet
        extends GuacamoleHTTPTunnelServlet {

    Logger logger = LoggerFactory.getLogger(GuacamoleTunnelServlet.class);

    @Override
    protected GuacamoleTunnel doConnect(HttpServletRequest request)
            throws GuacamoleException {
        String appUrl ="http://127.0.0.1:5875/api/v1";
        logger.debug("Http request received from {}. Content: {}" ,request.getSession().getId(), request.toString());
        VMconnection conn =new VMconnection(request,appUrl);
        GuacamoleSocket socket = new ConfiguredGuacamoleSocket(
                new InetGuacamoleSocket("localhost", 4822),
                conn.getConfig(appUrl)
        );

        logger.debug("Guacamole tunnel for {} is ready", request.getSession().getId());
        // Return a new tunnel which uses the connected socket
        return new SimpleGuacamoleTunnel(socket);
    }
}