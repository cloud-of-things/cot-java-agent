package com.telekom.cot.device.agent.common.util;

import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.Base64;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class CotHttpClient {

    private OkHttpClient okHttpClient;
    private String baseUrl;
    private String encodedAuthString;
    
    public CotHttpClient(String hostname, String tenant, String user, String password, String proxyHost, String proxyPort) {
        OkHttpClient.Builder clientBuilder = new OkHttpClient.Builder()
                        .readTimeout(1, TimeUnit.MINUTES);

        // get and check proxy, set at client builder
        Proxy proxy = getProxy(proxyHost, proxyPort);
        if (Objects.nonNull(proxy)) {
            clientBuilder = clientBuilder.proxy(proxy);
        }
        
        okHttpClient = clientBuilder.build();
        baseUrl = "https://" + tenant + "." + hostname;

        try {
            encodedAuthString = Base64.getEncoder().encodeToString((user + ":" + password).getBytes("utf-8"));
        } catch (UnsupportedEncodingException e) {
        }
    }

    public byte[] getResponseAsByteArray(String api) {
        Request request = new Request.Builder()
                .addHeader("Authorization", "Basic " + encodedAuthString)
                .url(baseUrl + "/" + api)
                .build();

        Response response = null;
        try {
            response = okHttpClient.newCall(request).execute();
            if (response.isSuccessful()) {
                return response.body().bytes();
            }
        } catch (Exception e) {
        } finally {
            closeResponse(response);
        }
        
        return null;
    }
    
    public byte[] getBinary(String binaryId) {
        // check binary id
        if (StringUtils.isEmpty(binaryId)) {
            return null;
        }
        
        return getResponseAsByteArray("inventory/binaries/" + binaryId);        
    }
    
    private Proxy getProxy(String proxyHost, String proxyPort) {
        // check proxy host
        if (StringUtils.isEmpty(proxyHost)) {
            return null;
        }
        
        // get proxy port as integer
        int port;
        try {
            port = Integer.parseInt(proxyPort);
        } catch (Exception e) {
            return null;
        }
        
        return new Proxy(Proxy.Type.HTTP, new InetSocketAddress(proxyHost, port));
    }
    
    private void closeResponse(final Response response) {
        if (Objects.nonNull(response) && Objects.nonNull(response.body())) {
            response.body().close();
        }
    }
    
    
}
