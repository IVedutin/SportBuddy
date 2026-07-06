package com.sportbuddy.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GigaChatConfig {

    @Value("${gigachat.grpc.host:gigachat.devices.sberbank.ru}")
    private String host;

    @Value("${gigachat.grpc.port:443}")
    private int port;

    @Value("${gigachat.auth.url:https://ngw.devices.sberbank.ru:9443/api/v2/oauth}")
    private String authUrl;

    @Value("${gigachat.authorization.key}")
    private String authorizationKey;

    @Value("${gigachat.scope:GIGACHAT_API_PERS}")
    private String scope;

    public String getHost() { return host; }
    public int getPort() { return port; }
    public String getAuthUrl() { return authUrl; }
    public String getAuthorizationKey() { return authorizationKey; }
    public String getScope() { return scope; }
}
