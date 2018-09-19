package kell.guacamole.small;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import javafx.util.Pair;
import org.apache.guacamole.GuacamoleClientException;
import org.apache.guacamole.GuacamoleException;
import org.apache.guacamole.protocol.GuacamoleConfiguration;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;

import javax.servlet.http.HttpServletRequest;
import java.io.InputStreamReader;

public class VMconnection {
    private String mSesskey;
    private String mPassword;
    private String mVMid;
    private String mProtocol;
    private String mUsername;
    private String mHostname;
    private String mPort;
    private String mDomain;

    public VMconnection(HttpServletRequest request) throws GuacamoleClientException {
        String lSesskey = request.getParameter("sesskey");
        String lUserkey = request.getParameter("userkey");
        String lVMid = request.getParameter("vmid");
        if(lSesskey.matches("([a-zA-Z]+|[0-9]+)+") &&
          lVMid.matches("[a-zA-Z0-9]+(-[a-zA-Z0-9]+)*")){
            mVMid =lVMid;
            mSesskey =lSesskey;
            mPassword =lUserkey;
        }
        else {
            throw mRightException("500","internal","Parameters doesn't match requirements");
        }
    }

    /**
     * gets needed parameters, creates config and returns it
     * @param appUrl
     * @return the tunnel config
     * @throws GuacamoleException
     */
    public GuacamoleConfiguration getConfig(String appUrl) throws GuacamoleException {
        mFindUser(appUrl);
        mFindVM(appUrl);
        GuacamoleConfiguration config = new GuacamoleConfiguration();
        if (mProtocol.equals("vmrpd")){
            config.setProtocol("rdp");
            config.setParameter("preconnection-blob", mVMid);
        }
        else{
            throw mRightException("500","internal", "Unknown protocol to VM");
        }

        config.setParameter("hostname", mHostname);
        config.setParameter("port", mPort);
        config.setParameter("username", mUsername);
        config.setParameter("domain", mDomain);
        config.setParameter("password", mPassword);
        config.setParameter("ignore-cert", "true");
        config.setParameter("security", "any");

        return config;
    }

    /**
     * Gets and fills user data
     * @param pAppUrl
     * @throws GuacamoleException
     */
    private void mFindUser(String pAppUrl) throws GuacamoleException {
        RequestBuilder builder = RequestBuilder.get().setUri(pAppUrl +"/key");
        builder.setHeader("sesskey", mSesskey);

        JsonObject obj = mPerformGet(builder, null);
        String l_domain = obj.get("domain").getAsString();
        String l_username = obj.get("username").getAsString();
        String l_serverkey = obj.get("serverkey").getAsString();
        byte[] txt = mPassword.getBytes();
        byte[] key = l_serverkey.getBytes();
        byte[] res = new byte[mPassword.length()];
        for (int i = 0; i < txt.length; i++) {
            res[i] = (byte) (txt[i] ^ key[i % key.length]);
        }
        mPassword = new String(res);
        mUsername = l_username; //TODO some magic to get correct username
        mDomain =l_domain;
        mPort = obj.get("port").getAsString();
    }

    /**
     * gets data about vm location
     * @param pAppUrl
     * @throws GuacamoleException
     */
    private void mFindVM(String pAppUrl) throws GuacamoleException {
        RequestBuilder builder = RequestBuilder.get().setUri(pAppUrl +"/vminfo");
        builder.setHeader("sesskey", mSesskey);
        JsonObject obj = mPerformGet(builder, new Pair<String, String>("vmid", mVMid));
        mHostname = obj.get("vmhost").getAsString();

         String lVMprovider= obj.get("vmprovider").getAsString();
         if (lVMprovider.equals("scvmm")){
             mProtocol =obj.get("protocol").getAsString();

         }
         else {
             throw mRightException("500","internal", "unsupported vm manager");
         }


    }

    /**
     * method to perform get-requests
     * @param pBuilder MUST countain url and headers
     * @param params
     * @return
     * @throws GuacamoleException
     */
    private JsonObject mPerformGet(RequestBuilder pBuilder, Pair<String,String>...params) throws GuacamoleException {
      if (params!=null){
        for (Pair<String,String> param:params) {
            pBuilder.addParameter(param.getKey(),param.getValue());
        }
      }
        HttpUriRequest request =  pBuilder.build();

        try (CloseableHttpClient client = HttpClientBuilder.create().build();
             CloseableHttpResponse response = client.execute(request)) {
            JsonObject object = new Gson().fromJson(new InputStreamReader(response.getEntity().getContent()), JsonObject.class);
            if (response.getStatusLine().getStatusCode() == 200) {
                return object;
            }
            else {
                throw new GuacamoleException(object.getAsString());
            }

        }catch (GuacamoleException e) {
            throw e;
        }
        catch (Exception e) {

            throw mRightException("500","internal","Some internal problems while performing GET request: " + e.getMessage());
        }

    }

    /**
     * Returns exception with json standard message
     * @param pStatus
     * @param pReason
     * @param pHuman
     * @return
     */
    private GuacamoleClientException mRightException(String pStatus, String pReason, String pHuman){
        JsonObject  err_json = new JsonObject();
        err_json.addProperty("status",pStatus);
        err_json.addProperty("reason",pReason);
        err_json.addProperty("human_reason",pHuman);
        return new GuacamoleClientException(err_json.getAsString());
    }

}
