package com.ximo.springwebfluxinaction;

import org.junit.jupiter.api.Test;
import org.springframework.http.codec.ClientCodecConfigurer;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.function.Consumer;

/**
 * @author xikl
 * @date 2019/11/25
 */
public class WebClientTest {

    @Test
    void testWebClientByBuilder() {
        Consumer<ClientCodecConfigurer> configConsumer =
                config -> config.defaultCodecs().enableLoggingRequestDetails(true);

        final WebClient webClient = WebClient.builder()
                .exchangeStrategies(ExchangeStrategies.builder().codecs(configConsumer).build())
                .baseUrl("http://localhost:8080")
                .build();
    }
}
