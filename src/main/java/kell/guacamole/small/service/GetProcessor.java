package kell.guacamole.small.service;


import kell.guacamole.small.exceptions.TortillaException;
import org.apache.guacamole.GuacamoleException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpUriRequest;

import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class GetProcessor {
    Logger logger = LoggerFactory.getLogger(GetProcessor.class);


    public String performGet(HttpUriRequest request) throws GuacamoleException {
        logger.debug("Perform HTTP GET request: {}", request.toString());
        try (CloseableHttpClient client = HttpClientBuilder.create().build();
             CloseableHttpResponse response = client.execute(request)) {
            logger.debug("Perform HTTP GET request: {} \n result: {}", request.toString(), response.getStatusLine().getStatusCode());

            if (response.getStatusLine().getStatusCode() == 200) {
                String json = EntityUtils.toString(response.getEntity(), "UTF-8");
                return json;
            } else {
                throw new TortillaException("500", "internal", "Some internal problems while performing GET request");
            }
        } catch (Exception e) {
            throw new TortillaException("500", "internal", "Some internal problems while performing GET request: " + e.getMessage());
        }
    }

}
