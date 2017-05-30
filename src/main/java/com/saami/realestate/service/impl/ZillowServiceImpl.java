package com.saami.realestate.service.impl;

import com.saami.realestate.model.ZillowData;
import com.saami.realestate.service.api.ZillowService;
import com.saami.realestate.util.RestUtil;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.XML;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by sasiddi on 5/1/17.
 */
@Component
@ConfigurationProperties("application.properties")
public class ZillowServiceImpl implements ZillowService {
    private static final Logger LOG = LoggerFactory.getLogger(ZillowServiceImpl.class);

    private static final String ID_PARAM = "zws-id" ;
    private static final String ADDRESS_PARAM = "address";
    private static final String CITY_PARAM = "citystatezip";
    private static final String RENT_PARAM = "rentzestimate";

    @Value("${api.zillow.id}")
    String id;

    @Value("${api.zillow.url}")
    String url;

    @Override
    public ZillowData getZillowData(String address, String city, String state) {
        if (StringUtils.isEmpty(address) || StringUtils.isEmpty(city) || StringUtils.isEmpty(state)) {
            throw new IllegalArgumentException("Address, City and State are required for Zillow getZillowData api");
        }

        Map<String, String> paramMap = new HashMap<>();
        paramMap.put(ID_PARAM, id);
        paramMap.put(ADDRESS_PARAM, encodeString(address));
        paramMap.put(CITY_PARAM, encodeString(city + ", " + state));
        paramMap.put(RENT_PARAM, "true");
        final String urlWithParams = appendParams(url, paramMap);
        CloseableHttpClient httpClient = null;
        HttpGet httpGet = null;
        CloseableHttpResponse response = null;

        try {

            httpClient = HttpClientBuilder.create().build();
            httpGet = new HttpGet(urlWithParams);
            httpGet.addHeader("content-type", "application/xml");

            LOG.debug("Hitting Zillow search results api: " + urlWithParams);
            response = httpClient.execute(httpGet);

            final int responseStatus = response.getStatusLine().getStatusCode();
            if (responseStatus < 200 || responseStatus > 300) {
                LOG.error("Request Code", responseStatus);
                LOG.error("Request Message", response.getStatusLine());
                throw new Exception(String.format("Call to Zillow search results api did not return 200. Status:[%s]", responseStatus));
            }
            LOG.debug("Zillow search results api call successful. Status Code: " + responseStatus);

            BufferedReader rd = new BufferedReader(
                    new InputStreamReader(response.getEntity().getContent()));

            StringBuffer result = new StringBuffer();
            String line = "";
            while ((line = rd.readLine()) != null) {
                result.append(line);
            }

            rd.close();

            return getZillowData(new JSONObject(XML.toJSONObject(result.toString()).toString()));

        } catch (Exception e) {
            LOG.error("Error getting from Zillow search results: " + urlWithParams, e);
            return null;
        } finally {
            if (httpGet != null) {
                httpGet.releaseConnection();
            }
        }
    }

    private String appendParams(String url, Map<String, String> params) {
        StringBuilder urlWithParams = new StringBuilder(url);
        urlWithParams.append("?");

        boolean firstParam = true;
        for (String param : params.keySet()) {
            if(!firstParam) {
                urlWithParams.append("&");
            }
            firstParam = false;
            urlWithParams.append(param);
            urlWithParams.append("=");
            urlWithParams.append(params.get(param));
        }

        return urlWithParams.toString();

    }

    private String encodeString(String param) {
        if (param == null) {
            throw new IllegalArgumentException("param to encode can not be null");
        }
        try {
            return URLEncoder.encode(param, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            LOG.error("error encoding pram: " + param, e);
            return null;
        }
    }

    private ZillowData getZillowData(JSONObject responseJson) {
        if (responseJson == null) {
            return null;
        }

        ZillowData zillowData = new ZillowData();

        JSONObject searchResults = responseJson.optJSONObject("SearchResults:searchresults");
        if (searchResults != null) {
            JSONObject response = searchResults.optJSONObject("response");
            if (response != null) {
                JSONObject results = response.optJSONObject("results");
                if (results != null) {
                    JSONObject resultJson = results.optJSONObject("result");
                        if (resultJson != null) {
                            JSONObject address = resultJson.optJSONObject("address");
                            if (address != null) {
                                zillowData.setZip(address.optLong("zipcode"));
                            }
                            JSONObject rentZestimate = resultJson.optJSONObject("rentzestimate");
                            if (rentZestimate != null) {
                                JSONObject amountJson = rentZestimate.optJSONObject("amount");
                                if (amountJson != null) {
                                    zillowData.setRentZestimate(amountJson.optDouble("content"));
                                }
                            }
                            JSONObject zestimate = resultJson.optJSONObject("zestimate");
                            if (zestimate != null) {
                                JSONObject amountJson = zestimate.optJSONObject("amount");
                                if (amountJson != null) {
                                    zillowData.setZestimate(amountJson.optDouble("content"));
                                }
                            }
                        }
                    }
                }
            }
        return zillowData;
    }

}
