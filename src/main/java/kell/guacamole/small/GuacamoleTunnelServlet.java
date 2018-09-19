package kell.guacamole.small;

import javax.servlet.http.HttpServletRequest;

import org.apache.guacamole.GuacamoleClientException;
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

        VMconnection conn =new VMconnection(request);
        String someUrl ="http://controller:5876/api/v1";

        GuacamoleSocket socket = new ConfiguredGuacamoleSocket(
                new InetGuacamoleSocket("localhost", 4822),
                conn.getConfig(someUrl)
        );

        // Return a new tunnel which uses the connected socket
        return new SimpleGuacamoleTunnel(socket);
    }
}