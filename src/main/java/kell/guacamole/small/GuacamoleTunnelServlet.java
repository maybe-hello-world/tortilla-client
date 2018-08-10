package kell.guacamole.small;

import javax.servlet.http.HttpServletRequest;
import org.apache.guacamole.GuacamoleException;
import org.apache.guacamole.net.GuacamoleSocket;
import org.apache.guacamole.net.GuacamoleTunnel;
import org.apache.guacamole.net.InetGuacamoleSocket;
import org.apache.guacamole.net.SimpleGuacamoleTunnel;
import org.apache.guacamole.protocol.ConfiguredGuacamoleSocket;
import org.apache.guacamole.protocol.GuacamoleConfiguration;
import org.apache.guacamole.servlet.GuacamoleHTTPTunnelServlet;

public class GuacamoleTunnelServlet
        extends GuacamoleHTTPTunnelServlet {


    @Override
    protected GuacamoleTunnel doConnect(HttpServletRequest request)
            throws GuacamoleException {

        GuacamoleConfiguration config = new GuacamoleConfiguration();

        String domain = request.getParameter("domain").toLowerCase();
        String server = request.getParameter("server").toLowerCase();
        String username = request.getParameter("username").toLowerCase();
        String password = request.getParameter("password");
        String protocol = request.getParameter("protocol").toLowerCase();
        String port = request.getParameter("port");

        if (protocol.equals("vmrdp")) {
            protocol = "rdp";
            String VMID = request.getParameter("vmid");
            config.setParameter("preconnection-blob", VMID);
        }

        // Create our configuration
        config.setProtocol(protocol);
        config.setParameter("hostname", server);
        config.setParameter("port", port);
        config.setParameter("username", username);
        config.setParameter("domain", domain);
        config.setParameter("password", password);
        config.setParameter("ignore-cert", "true");
        config.setParameter("security", "any");

        // Connect to guacd - everything is hard-coded here.
        GuacamoleSocket socket = new ConfiguredGuacamoleSocket(
                new InetGuacamoleSocket("localhost", 4822),
                config
        );

        // Return a new tunnel which uses the connected socket
        return new SimpleGuacamoleTunnel(socket);
    }
}