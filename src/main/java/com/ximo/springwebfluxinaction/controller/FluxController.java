package com.ximo.springwebfluxinaction.controller;

import com.ximo.springwebfluxinaction.config.WebConfig;
import com.ximo.springwebfluxinaction.domain.ExampleDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.stream.IntStream;

import static java.util.stream.Collectors.toList;

/**
 * @author xikl
 * @date 2019/11/21
 */
@Slf4j
@RestController
@RequestMapping("/flux")
public class FluxController {

    /**
     * 自己请求自己项目的webclient
     *
     * @see WebConfig#webClient()
     */
    @Autowired
    private WebClient webClient;

    @GetMapping("/example")
    public Flux<ExampleDTO> examples() {
        final List<ExampleDTO> resultList = IntStream.rangeClosed(0, 9)
                .boxed()
                .map(i -> new ExampleDTO(i, "name" + i))
                .collect(toList());
        log.info("example: {}", resultList);
        throw new UnsupportedOperationException();
//        return Flux.fromIterable(resultList);
    }

    /**
     * .retrieve()
     * .bodyToFlux(ExampleDTO.class);
     *
     * 等同于
     * .exchange()
     * .flatMapMany(clientResponse -> clientResponse.bodyToFlux(ExampleDTO.class));
     *
     *
     * @return
     */
    @GetMapping("/webclient")
    public Flux<ExampleDTO> invokeByWebClient() {
        // 发起请求
        final Flux<ExampleDTO> result = webClient.get()
                .uri("/flux/example")
                .exchange()
                .flatMapMany(clientResponse -> clientResponse.bodyToFlux(ExampleDTO.class));
//                .retrieve()
//                .bodyToFlux(ExampleDTO.class);

        // 切记不要订阅 否则会出现触发两次的问题
        //result.subscribe();

        return result;
    }

}
