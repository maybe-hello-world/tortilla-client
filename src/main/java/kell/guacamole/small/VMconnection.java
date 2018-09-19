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
import java.io.IOException;
import java.io.InputStreamReader;

public class VMconnection {
    private String m_sesskey;
    private String m_password;
    private String m_vmid;
    private String m_protocol;
    private String m_username;
    private String m_hostname;
    private String m_port;
    private String m_domain;


/*
    GuacamoleConfiguration config = new GuacamoleConfiguration();
        // get username/password from controller
        if (false) {
            throw new GuacamoleClientException("Internal error. See tunnel logs");
        }

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
 */




    public VMconnection(HttpServletRequest request) throws GuacamoleClientException {
        String l_sesskey = request.getParameter("sesskey").toLowerCase();
        String l_userkey = request.getParameter("userkey").toLowerCase();
        String l_vmid = request.getParameter("vmid").toLowerCase();
        if(l_sesskey.matches("([a-zA-Z]+|[0-9]+)+") &&
          l_vmid.matches("[a-zA-Z0-9]+(-[a-zA-Z0-9]+)*")){
            m_vmid=l_vmid;
            m_sesskey=l_sesskey;
        }
        else {
            throw RightException("500","internal","Parameters doesn't match requirements");
        }
    }

    /**
     * Получает недостающие параметры, заводит конфиг и возвращает его
     * @param appUrl
     * @return возвращает конфиг туннеля
     * @throws GuacamoleException
     */
    public GuacamoleConfiguration getConfig(String appUrl) throws GuacamoleException {
        findUser(appUrl);
        findVM(appUrl);
        GuacamoleConfiguration config = new GuacamoleConfiguration();
        if (m_protocol.equals("vmrpd")){
            config.setProtocol("rdp");
            config.setParameter("preconnection-blob", m_vmid);
        }
        else{
            throw RightException("500","internal", "Unknown protocol to VM");
        }

        config.setParameter("hostname", m_hostname);
        config.setParameter("port", m_port);
        config.setParameter("username", m_username);
        config.setParameter("domain", m_domain);
        config.setParameter("password", m_password);
        config.setParameter("ignore-cert", "true");
        config.setParameter("security", "any");

        return config;
    }

    /**
     * поучает и заполняет даннын о пользователе
     * @param appUrl
     * @throws GuacamoleException
     */
    private void findUser (String appUrl) throws GuacamoleException {
        RequestBuilder builder = RequestBuilder.get().setUri(appUrl +"/key");
        builder.setHeader("sesskey",m_sesskey);

        JsonObject obj = performGet(builder, null);
        String l_domain = obj.get("domain").getAsString();
        String l_username = obj.get("username").getAsString();
        String l_serverkey = obj.get("serverkey").getAsString();
        byte[] txt = m_password.getBytes();
        byte[] key = l_serverkey.getBytes();
        byte[] res = new byte[m_password.length()];
        for (int i = 0; i < txt.length; i++) {
            res[i] = (byte) (txt[i] ^ key[i % key.length]);
        }
        m_password= new String(res);
        m_username= l_username; //TODO some magic to get correct username
        m_domain =l_domain;
        m_port = obj.get("port").getAsString();
    }

    /**
     * Получает данный о расположении виртуальной машины
     * @param appUrl
     * @throws GuacamoleException
     */
    private void findVM(String appUrl) throws GuacamoleException {
        RequestBuilder builder = RequestBuilder.get().setUri(appUrl +"/vminfo");
        builder.setHeader("sesskey",m_sesskey);
        JsonObject obj = performGet(builder, new Pair<String, String>("vmid",m_vmid));
        m_hostname= obj.get("vmhost").getAsString();

         String l_vmprovider= obj.get("vmprovider").getAsString();
         if (l_vmprovider.equals("scvmm")){
             m_protocol=obj.get("protocol").getAsString();

         }
         else {
             throw RightException("500","internal", "unsupported vm manager");
         }


    }

    /**
     * Для выполнения гет-запросов
     * @param builder уже введены адрес и заголовки запроса
     * @param params
     * @return
     * @throws GuacamoleException
     */
    private JsonObject performGet(RequestBuilder builder, Pair<String,String>...params) throws GuacamoleException {
      if (params==null){
        for (Pair<String,String> param:params) {
            builder.addParameter(param.getKey(),param.getValue());
        }
      }
        HttpUriRequest request =  builder.build();

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

            throw RightException("500","internal","Some internal problems while performing GET request: " + e.getMessage());
        }

    }

    /**
     * Возвращает исключение с сообщением в виде Json в стандартном виде
     * @param status
     * @param reason
     * @param human
     * @return
     */
    private GuacamoleClientException RightException (String status, String reason, String human){
        JsonObject  err_json = new JsonObject();
        err_json.addProperty("status",status);
        err_json.addProperty("reason",reason);
        err_json.addProperty("human_reason",human);
        return new GuacamoleClientException(err_json.getAsString());
    }

}
