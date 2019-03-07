package kell.guacamole.small;

import kell.guacamole.small.domain.UserInfoRestModel;
import kell.guacamole.small.domain.VmInfoRestModel;
import kell.guacamole.small.exceptions.TortillaException;
import kell.guacamole.small.service.UserInfoService;
import kell.guacamole.small.service.VmInfoService;
import org.apache.guacamole.GuacamoleClientException;
import org.apache.guacamole.GuacamoleException;
import org.apache.guacamole.protocol.GuacamoleConfiguration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


import javax.servlet.http.HttpServletRequest;

public class VMconnection {
    private UserInfoService userInfoService;
    private VmInfoService vmInfoService;

    private String sesskey;
    private String mPassword;
    private String mVMid;

    private Integer screenWidth;
    private Integer screenHeight;


    private Logger logger = LogManager.getLogger(VMconnection.class);


    public VMconnection(HttpServletRequest request, String appUrl) throws GuacamoleClientException {

        String lSesskey = request.getParameter("sesskey");
        String lUserkey = request.getParameter("userkey");
        String lVMid = request.getParameter("vmid");
        String widthStr = request.getParameter("width");
        String heightStr = request.getParameter("height");

        try {
            screenWidth = Integer.parseInt(widthStr);
            screenHeight =  Integer.parseInt(heightStr);

            if ( screenWidth <= 0 || screenHeight <= 0) {
                logger.warn("Parameters don't match requirements and have been ignored. Screen width/height must be decimal digits");
            }
        } catch (NumberFormatException ex) {
            logger.warn("Parameters don't match requirements and have been ignored. Screen width/height must be decimal digits");
            screenWidth = null;
            screenHeight = null;
        }
        if (lSesskey == null || lUserkey == null || lVMid == null) {
            logger.error("Parameters don't match requirements and have been ignored. Some parameter is null");
        }

        mVMid = lVMid;
        sesskey = lSesskey;
        mPassword = lUserkey;

        userInfoService = new UserInfoService(appUrl + "/key", sesskey);
        vmInfoService = new VmInfoService(appUrl + "/vminfo", sesskey, mVMid);
    }

    /**
     * Gets needed parameters, creates config and returns it
     *
     * @param appUrl target vm URL
     * @return the tunnel config
     * @throws GuacamoleException
     */
    public GuacamoleConfiguration getConfig(String appUrl) throws GuacamoleException {

        UserInfoRestModel userInfo = userInfoService.getUserInfo();
        userInfo.setUserkey(mPassword);

        VmInfoRestModel vmInfo = vmInfoService.getVmInfo();
        logger.debug("Creating Guacamole configuration for VM:{} \n User:{}", vmInfo, userInfo);

        GuacamoleConfiguration config = new GuacamoleConfiguration();

        if (vmInfo.getProtocol().equals("vmrdp")) {
            config.setProtocol("rdp");
            config.setParameter("preconnection-blob", mVMid);
        } else {
            throw new TortillaException("500", "internal", "Unknown protocol to VM");
        }
        if (!vmInfo.getVmprovider().equals("scvmm")) {
            throw new TortillaException("500", "internal", "unsupported vm manager");
        }

        config.setParameter("hostname", vmInfo.getVmhost());
        config.setParameter("port", vmInfo.getPort());
        config.setParameter("username", userInfo.getUsername());
        config.setParameter("domain", userInfo.getDomain());
        config.setParameter("password", userInfo.getPassword());
        config.setParameter("ignore-cert", "true");
        config.setParameter("security", "any");
        if( screenWidth != null && screenHeight != null) {
            config.setParameter("width", screenWidth.toString());
            config.setParameter("height", screenHeight.toString());
        }

        return config;
    }

}
