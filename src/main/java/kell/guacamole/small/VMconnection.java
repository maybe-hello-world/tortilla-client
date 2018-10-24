package kell.guacamole.small;

import kell.guacamole.small.domain.UserInfoRestModel;
import kell.guacamole.small.domain.VmInfoRestModel;
import kell.guacamole.small.exceptions.TortillaException;
import kell.guacamole.small.service.UserInfoService;
import kell.guacamole.small.service.VmInfoService;
import org.apache.guacamole.GuacamoleClientException;
import org.apache.guacamole.GuacamoleException;
import org.apache.guacamole.protocol.GuacamoleConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;

public class VMconnection {
    UserInfoService userInfoService;
    VmInfoService vmInfoService;

    private String sesskey;
    private String mPassword;
    private String mVMid;

    private Logger logger = LoggerFactory.getLogger(VMconnection.class);



    public VMconnection(HttpServletRequest request, String appUrl) throws GuacamoleClientException {

        String lSesskey = request.getParameter("sesskey");
        String lUserkey = request.getParameter("userkey");
        String lVMid = request.getParameter("vmid");
        if(lSesskey.matches("([a-zA-Z]+|[0-9]+)+") &&
                lVMid.matches("[a-zA-Z0-9]+(-[a-zA-Z0-9]+)*")){
            mVMid = lVMid;
            sesskey = lSesskey;
            mPassword = lUserkey;
        }
        else {
           throw new TortillaException("500","internal","Parameters doesn't match requirements");
        }
        userInfoService = new UserInfoService(appUrl + "/key",sesskey);
        vmInfoService = new VmInfoService(appUrl + "/vminfo", sesskey, mVMid);
    }

    /**
     * Gets needed parameters, creates config and returns it
     * @param appUrl target vm URL
     * @return the tunnel config
     * @throws GuacamoleException
     */
    public GuacamoleConfiguration getConfig(String appUrl) throws GuacamoleException {

        UserInfoRestModel userInfo = userInfoService.getUserInfo();
        userInfo.setUserkey(mPassword);

        VmInfoRestModel vmInfo = vmInfoService.getVmInfo();
        System.out.println("user: " + userInfo);
        System.out.println("vm: " + vmInfo);
        logger.debug("Creating Guacamole configuration for VM:{} \n User:{}", vmInfo, userInfo);

        GuacamoleConfiguration config = new GuacamoleConfiguration();

        if (vmInfo.getProtocol().equals("vmrdp")){
            config.setProtocol("rdp");
            config.setParameter("preconnection-blob", mVMid);
        }
        else{
            throw new TortillaException("500","internal", "Unknown protocol to VM");
        }
        if (!vmInfo.getVmprovider().equals("scvmm")){
            throw new TortillaException("500","internal", "unsupported vm manager");
        }

        config.setParameter("hostname", vmInfo.getVmhost());
        config.setParameter("port", vmInfo.getPort());
        config.setParameter("username", userInfo.getUsername());
        config.setParameter("domain", userInfo.getDomain());
        config.setParameter("password", userInfo.getPassword());
        config.setParameter("ignore-cert", "true");
        config.setParameter("security", "any");

        return config;
    }

}
