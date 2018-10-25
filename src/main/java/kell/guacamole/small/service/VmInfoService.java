package kell.guacamole.small.service;

import com.google.gson.Gson;
import kell.guacamole.small.domain.VmInfoRestModel;
import org.apache.guacamole.GuacamoleException;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VmInfoService {

    private String requstUrl; //"/vminfo"
    private String sesskey;
    private String vmid;
    private GetProcessor getProcessor = new GetProcessor();
    Logger logger = LoggerFactory.getLogger(VmInfoService.class);


    public VmInfoService(String requstUrl, String sesskey, String vmid) {
        this.requstUrl = requstUrl;
        this.sesskey = sesskey;
        this.vmid = vmid;
    }

    public VmInfoRestModel getVmInfo() throws GuacamoleException {
        RequestBuilder requestBuilder = RequestBuilder.get().setUri(requstUrl);
        requestBuilder.addParameter("vmid", vmid);
        HttpUriRequest request = requestBuilder.build();

        logger.debug("Get vmInfo with params : {url:" + requstUrl + ",sesskey:" + sesskey + ",vmid:" + vmid + "}");
        request.setHeader("Cookie", "sesskey=" + sesskey);
        String result = getProcessor.performGet(request);

        VmInfoRestModel vmInfoRestModel = new Gson().fromJson(result, VmInfoRestModel.class);
        logger.debug("Parse result :" + vmInfoRestModel);
        if (vmInfoRestModel == null) {
            logger.error("Error while parsing. VM info is null.");
        }
        return vmInfoRestModel;
    }

    @Override
    public String toString() {
        return "VmInfoService{" +
                "requstUrl='" + requstUrl + '\'' +
                ", sesskey='" + sesskey + '\'' +
                ", vmid='" + vmid + '\'' +
                ", getProcessor=" + getProcessor +
                '}';
    }
}
