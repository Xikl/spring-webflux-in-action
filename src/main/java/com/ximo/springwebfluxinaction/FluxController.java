package com.ximo.springwebfluxinaction;

import com.ximo.springwebfluxinaction.domain.ExampleDTO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.stream.IntStream;

import static java.util.stream.Collectors.toList;

/**
 * @author xikl
 * @date 2019/11/21
 */
@RestController
public class FluxController {

    @GetMapping("/recent")
    public Flux<ExampleDTO> recentTacos() {
        final List<ExampleDTO> resultList = IntStream.rangeClosed(0, 9)
                .boxed()
                .map(i -> new ExampleDTO(i, "name" + i))
                .collect(toList());

        return Flux.fromIterable(resultList);
    }

}
