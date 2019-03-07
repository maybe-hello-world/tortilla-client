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

    private final Integer screenWidth;
    private final Integer screenHeight;


    private Logger logger = LogManager.getLogger(VMconnection.class);


    public VMconnection(HttpServletRequest request, String appUrl) throws GuacamoleClientException {

        String lSesskey = request.getParameter("sesskey");
        String lUserkey = request.getParameter("userkey");
        String lVMid = request.getParameter("vmid");
        String widthStr = request.getParameter("width");
        String heightStr = request.getParameter("height");

        try {
            screenWidth = widthStr != null ? Integer.parseInt(widthStr): null;
            screenHeight = widthStr != null ? Integer.parseInt(heightStr): null;
        } catch (Exception ex) {
            throw new TortillaException("500", "internal", "Parameters don't match requirements. Screen width/height must be decimal digits");
        }
        validateParams(lSesskey, lVMid, lUserkey, screenWidth, screenHeight);

        mVMid = lVMid;
        sesskey = lSesskey;
        mPassword = lUserkey;

        userInfoService = new UserInfoService(appUrl + "/key", sesskey);
        vmInfoService = new VmInfoService(appUrl + "/vminfo", sesskey, mVMid);
    }

    /**
     * Validate input params
     * @param pSesskey string of letters and digits
     * @param pVMid string of  letters and digits
     * @param pUserKey no restrictions yet
     * @param pScreenWidth  must be positive integer value or null
     * @param pScreenHeight must be positive integer value or null
     * @throws TortillaException
     */
    private void validateParams(String pSesskey, String pVMid, String pUserKey, Integer pScreenWidth, Integer pScreenHeight) throws TortillaException {
        if (!pSesskey.matches("([a-zA-Z]+|[0-9]+)+") ||
                !pVMid.matches("[a-zA-Z0-9]+(-[a-zA-Z0-9]+)*")) {
            throw new TortillaException("500", "internal", "Parameters don't match requirements");
        }

        if ((pScreenWidth != null && pScreenWidth <= 0) | (pScreenHeight != null && pScreenHeight <= 0)) {
            throw new TortillaException("500", "internal", "Parameters don't match requirements. Screen width/height must be decimal digits");
        }
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
