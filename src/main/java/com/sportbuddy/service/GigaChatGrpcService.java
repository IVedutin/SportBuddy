package com.sportbuddy.service;
import lombok.extern.slf4j.Slf4j;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sportbuddy.config.GigaChatConfig;
import com.sportbuddy.grpc.*;

import io.grpc.*;
import io.grpc.netty.shaded.io.grpc.netty.GrpcSslContexts;
import io.grpc.netty.shaded.io.grpc.netty.NettyChannelBuilder;
import io.grpc.netty.shaded.io.netty.handler.ssl.SslContext;
import io.grpc.netty.shaded.io.netty.handler.ssl.util.InsecureTrustManagerFactory;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;

import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.apache.hc.client5.http.ssl.NoopHostnameVerifier;
import org.apache.hc.client5.http.ssl.SSLConnectionSocketFactoryBuilder;
import org.apache.hc.core5.ssl.SSLContextBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import javax.net.ssl.SSLContext;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.UUID;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class GigaChatGrpcService {

    @Autowired
    private GigaChatConfig config;

    private ManagedChannel channel;
    private ChatServiceGrpc.ChatServiceBlockingStub chatStub;

    private String accessToken;
    private long tokenExpiryTime;

    private RestTemplate restTemplate;

    private final ObjectMapper mapper = new ObjectMapper();

    // ================= INIT =================

    @PostConstruct
    public void init() throws Exception {
        log.info("🚀 Инициализация GigaChat gRPC...");

        // 🔐 1. Попытка загрузить российский CA для HTTPS (RestTemplate)
        boolean certLoaded = false;
        InputStream certInput = getClass().getClassLoader().getResourceAsStream("certs/russian_trusted_root_ca.pem");

        if (certInput != null && certInput.available() > 100) {
            try {
                CertificateFactory cf = CertificateFactory.getInstance("X.509");
                Certificate ca = cf.generateCertificate(certInput);

                KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
                keyStore.load(null, null);
                keyStore.setCertificateEntry("russian_ca", ca);

                javax.net.ssl.TrustManagerFactory tmf = javax.net.ssl.TrustManagerFactory.getInstance(
                        javax.net.ssl.TrustManagerFactory.getDefaultAlgorithm());
                tmf.init(keyStore);

                SSLContext sslCtx = SSLContext.getInstance("TLS");
                sslCtx.init(null, tmf.getTrustManagers(), null);

                restTemplate = buildRestTemplate(sslCtx);
                certLoaded = true;
                log.info("✅ Российский CA сертификат загружен");
            } catch (Exception e) {
                log.error("⚠️ Ошибка загрузки сертификата: " + e.getMessage());
            }
        }

        if (!certLoaded) {
            log.error("⚠️ Сертификат не найден или некорректен — используем trust-all для HTTPS");
            restTemplate = buildTrustAllRestTemplate();
        }

        // 🔑 2. Получаем токен
        refreshTokenIfNeeded();

        if (accessToken == null) {
            throw new RuntimeException("❌ Нет токена — проверьте authorization key");
        }

        // 🔐 3. gRPC SSL
        SslContext sslContextGrpc;
        InputStream certInputGrpc = getClass().getClassLoader().getResourceAsStream("certs/russian_trusted_root_ca.pem");

        if (certInputGrpc != null && certInputGrpc.available() > 100 && certLoaded) {
            sslContextGrpc = GrpcSslContexts.forClient().trustManager(certInputGrpc).build();
            log.info("✅ gRPC SSL: российский CA");
        } else {
            // Trust-all для gRPC (как buildTrustAllRestTemplate для REST)
            sslContextGrpc = GrpcSslContexts.forClient()
                    .trustManager(InsecureTrustManagerFactory.INSTANCE)
                    .build();
            log.info("⚠️ gRPC SSL: trust-all");
        }

        // 🚀 4. Создаём gRPC канал
        channel = NettyChannelBuilder
                .forAddress(config.getHost(), config.getPort())
                .sslContext(sslContextGrpc)
                .build();

        chatStub = ChatServiceGrpc.newBlockingStub(channel)
                .withCallCredentials(new BearerTokenCredential());

        log.info("✅ GigaChat gRPC готов! Host: " + config.getHost() + ":" + config.getPort());
    }

    // ================= TOKEN =================

    private synchronized void refreshTokenIfNeeded() {
        if (accessToken == null || System.currentTimeMillis() > tokenExpiryTime - 60_000) {
            obtainAccessToken();
        }
    }

    private void obtainAccessToken() {
        try {
            log.info("🔑 Получение токена GigaChat...");

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            headers.set("Accept", "application/json");
            headers.set("RqUID", UUID.randomUUID().toString());
            headers.set("Authorization", "Basic " + config.getAuthorizationKey());

            MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
            body.add("scope", config.getScope());

            HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);

            ResponseEntity<String> response = restTemplate.postForEntity(config.getAuthUrl(), request, String.class);
            String responseBody = response.getBody();

            if (responseBody == null) throw new RuntimeException("Пустой ответ OAuth");

            JsonNode json = mapper.readTree(responseBody);
            JsonNode tokenNode = json.get("access_token");
            if (tokenNode == null) {
                log.error("❌ Ответ без access_token: " + responseBody);
                throw new RuntimeException("Нет access_token в ответе GigaChat");
            }

            this.accessToken = tokenNode.asText();

            JsonNode expiresNode = json.has("expires_in") ? json.get("expires_in") : json.get("expires_at");
            long expiresIn = (expiresNode != null) ? expiresNode.asLong() : 1800;

            // expires_at приходит в миллисекундах, expires_in — в секундах
            this.tokenExpiryTime = (expiresIn > 1_000_000_000L)
                    ? expiresIn                                          // уже timestamp ms
                    : System.currentTimeMillis() + expiresIn * 1000;    // duration in seconds

            log.info("✅ Токен получен, действует " + expiresIn + " сек/ms");

        } catch (Exception e) {
            log.error("❌ Ошибка получения токена: " + e.getMessage());
            throw new RuntimeException("Ошибка получения токена GigaChat", e);
        }
    }

    // ================= CHAT =================

    /**
     * Отправляет сообщение с системным промптом (история игр пользователя и т.д.)
     */
    public String sendMessage(String systemPrompt, String userMessage) {
        try {
            refreshTokenIfNeeded();

            ChatRequest.Builder requestBuilder = ChatRequest.newBuilder()
                    .setModel("GigaChat")
                    .setOptions(ChatOptions.newBuilder()
                            .setTemperature(0.7f)
                            .setMaxTokens(800)
                            .build());

            if (systemPrompt != null && !systemPrompt.isBlank()) {
                requestBuilder.addMessages(Message.newBuilder()
                        .setRole("system")
                        .setContent(systemPrompt)
                        .build());
            }

            requestBuilder.addMessages(Message.newBuilder()
                    .setRole("user")
                    .setContent(userMessage)
                    .build());

            ChatResponse response = chatStub.chat(requestBuilder.build());

            if (response.getAlternativesCount() > 0) {
                return response.getAlternatives(0).getMessage().getContent();
            }

            return "Извините, GigaChat не вернул ответ";

        } catch (StatusRuntimeException e) {
            log.error("gRPC ошибка: " + e.getStatus());
            return null; // ChatController применит fallback
        } catch (Exception e) {
            log.error("Ошибка sendMessage: " + e.getMessage());
            return null;
        }
    }

    // ================= SSL HELPERS =================

    private RestTemplate buildRestTemplate(SSLContext sslCtx) throws Exception {
        CloseableHttpClient httpClient = HttpClients.custom()
                .setConnectionManager(PoolingHttpClientConnectionManagerBuilder.create()
                        .setSSLSocketFactory(SSLConnectionSocketFactoryBuilder.create()
                                .setSslContext(sslCtx)
                                .build())
                        .build())
                .build();
        return new RestTemplate(new HttpComponentsClientHttpRequestFactory(httpClient));
    }

    private RestTemplate buildTrustAllRestTemplate() throws Exception {
        SSLContext sslCtx = SSLContextBuilder.create()
                .loadTrustMaterial(null, (X509Certificate[] chain, String authType) -> true)
                .build();
        CloseableHttpClient httpClient = HttpClients.custom()
                .setConnectionManager(PoolingHttpClientConnectionManagerBuilder.create()
                        .setSSLSocketFactory(SSLConnectionSocketFactoryBuilder.create()
                                .setSslContext(sslCtx)
                                .setHostnameVerifier(NoopHostnameVerifier.INSTANCE)
                                .build())
                        .build())
                .build();
        return new RestTemplate(new HttpComponentsClientHttpRequestFactory(httpClient));
    }

    // ================= SHUTDOWN =================

    @PreDestroy
    public void shutdown() {
        if (channel != null) {
            channel.shutdown();
            try {
                channel.awaitTermination(5, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    // ================= BEARER TOKEN CREDENTIALS =================

    private class BearerTokenCredential extends CallCredentials {

        @Override
        public void applyRequestMetadata(RequestInfo requestInfo, Executor appExecutor, MetadataApplier applier) {
            appExecutor.execute(() -> {
                try {
                    refreshTokenIfNeeded();
                    if (accessToken == null) {
                        applier.fail(Status.UNAUTHENTICATED.withDescription("No token"));
                        return;
                    }
                    Metadata headers = new Metadata();
                    headers.put(Metadata.Key.of("Authorization", Metadata.ASCII_STRING_MARSHALLER),
                            "Bearer " + accessToken);
                    applier.apply(headers);
                } catch (Exception e) {
                    applier.fail(Status.UNAUTHENTICATED.withDescription(e.getMessage()));
                }
            });
        }

        @Override
        public void thisUsesUnstableApi() {}
    }
}
