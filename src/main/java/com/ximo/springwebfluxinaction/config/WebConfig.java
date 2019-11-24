package com.ximo.springwebfluxinaction.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * @author xikl
 * @date 2019/11/23
 */
@Configuration
public class WebConfig {

    @Bean
    public WebClient webClient() {
        return WebClient.create("http://localhost:8080");
    }


}
