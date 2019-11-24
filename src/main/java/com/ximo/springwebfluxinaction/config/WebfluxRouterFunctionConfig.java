package com.ximo.springwebfluxinaction.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import static org.springframework.web.reactive.function.server.RequestPredicates.GET;
import static org.springframework.web.reactive.function.server.RequestPredicates.POST;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;
import static org.springframework.web.reactive.function.server.ServerResponse.ok;

/**
 * @author xikl
 * @date 2019/11/23
 */
@Slf4j
@Configuration
public class WebfluxRouterFunctionConfig {

    /**
     * webflux的函数式端点的方式
     *
     * @return 函数式端点方式
     */
    @Bean
    public RouterFunction<?> helloWebfluxRouterFunction() {
        return route(GET("/example"), this::listExamples)
                .andRoute(POST("/example"), this::saveSomething);
    }

    private Mono<ServerResponse> saveSomething(ServerRequest serverRequest) {
        return ok().body(Mono.just("save something"), String.class);
    }

    private Mono<ServerResponse> listExamples(ServerRequest serverRequest) {
        final String result = "hello webflux router function";
        log.info("hello: {}", result);
        return ok().body(Mono.just(result), String.class);
    }


}
