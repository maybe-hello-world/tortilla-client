package kell.guacamole.small.service;

import com.google.gson.Gson;
import kell.guacamole.small.domain.UserInfoRestModel;
import org.apache.guacamole.GuacamoleException;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UserInfoService {
    public static Logger logger = LoggerFactory.getLogger(UserInfoService.class);

    private String requstUrl; ///key
    private String sesskey;
    private GetProcessor getProcessor = new GetProcessor();

    public UserInfoService(String requstUrl, String sesskey) {
        this.requstUrl = requstUrl;
        this.sesskey = sesskey;
    }

    public UserInfoRestModel getUserInfo() throws GuacamoleException {
        RequestBuilder requestBuilder = RequestBuilder.get().setUri(requstUrl);
        HttpUriRequest request =  requestBuilder.build();
        logger.debug("Get userInfo with params : {url:" + requstUrl + ",sesskey:" + sesskey +"}");

        request.setHeader("Cookie", "sesskey="+sesskey);
        String result = getProcessor.performGet(request);
        UserInfoRestModel userInfoRestModel = new Gson().fromJson(result, UserInfoRestModel.class);
        if (userInfoRestModel == null) {
            logger.error("Error while parsing. User info is null.");
        }
        return userInfoRestModel;
    }

    @Override
    public String toString() {
        return "UserInfoService{" +
                "requstUrl='" + requstUrl + '\'' +
                ", sesskey='" + sesskey + '\'' +
                ", getProcessor=" + getProcessor +
                '}';
    }

    public void setGetProcessor(GetProcessor getProcessor) {
        this.getProcessor = getProcessor;
    }
}
