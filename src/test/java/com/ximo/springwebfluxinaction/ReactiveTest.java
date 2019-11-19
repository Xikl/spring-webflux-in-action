package com.ximo.springwebfluxinaction;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import reactor.util.function.Tuple2;

import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * @author xikl
 * @date 2019/11/19
 */
@Slf4j
class ReactiveTest {

    @Test
    void testMonoJust() {
        // original
        String name = "Craig";
        String capitalName = name.toUpperCase();
        String greeting = "Hello, " + capitalName + "!";
        System.out.println(greeting);

        // Mono
        Mono.just("Craig")
                .map(String::toUpperCase)
                .map(data -> String.format("Hello %s!", data))
                .log()
                .subscribe(System.out::println);

        // Optional
        Optional.of("Craig")
                .map(String::toUpperCase)
                .map(data -> String.format("Hello %s!", data))
                .ifPresent(System.out::println);

    }

    @Test
    void testFLuxJust() {
        final Flux<String> data = Flux.just("a", "b", "c")
                .map(String::toUpperCase);
        data.log().subscribe();

        StepVerifier.create(data)
                .expectNext("A")
                .expectNext("B")
                .expectNext("C")
                .verifyComplete();
    }

    /**
     * @see Flux#fromArray(Object[])
     * @see Flux#fromIterable(Iterable)
     * @see Flux#fromStream(Stream)
     * @see Flux#fromStream(Supplier)
     */
    @Test
    void testFluxFromArray() {
        String[] fruits =
                new String[]{"Apple", "Orange", "Grape", "Banana", "Strawberry"};

        final Flux<String> fruitsFlux = Flux.fromArray(fruits);

        StepVerifier.create(fruitsFlux)
                .expectNext("Apple", "Orange", "Grape", "Banana", "Strawberry")
                .verifyComplete();

        final Flux<Integer> integerFlux = Flux.fromStream(() -> Stream.generate(() -> 2));
        integerFlux
                .take(4)
                .log()
                .subscribe();
    }

    @Test
    void testFluxRange() {
        // rangeClosed VS Stream#RangeCloesd
        Flux.range(1, 6)
                .log()
                .subscribe();

        // Stream
        IntStream.rangeClosed(1, 6)
                .forEach(data -> log.info("data: {}", data));

    }

    @Test
    void testFluxInterval() throws InterruptedException {
        Flux.interval(Duration.ofSeconds(1))
                .take(5)
                .log()
//                .subscribeOn(Schedulers.single())
                .subscribe();
        // 等一会
        TimeUnit.SECONDS.sleep(5);
        System.out.println("结束");
    }

    /**
     * 22:39:59.225 [main] INFO reactor.Flux.Merge.1 - onSubscribe(FluxFlatMap.FlatMapMain)
     * 22:39:59.226 [main] INFO reactor.Flux.Merge.1 - request(unbounded)
     * 22:39:59.734 [parallel-1] INFO reactor.Flux.Merge.1 - onNext(a)
     * 22:39:59.989 [parallel-3] INFO reactor.Flux.Merge.1 - onNext(b)
     * 22:40:00.235 [parallel-4] INFO reactor.Flux.Merge.1 - onNext(c)
     * 22:40:00.490 [parallel-5] INFO reactor.Flux.Merge.1 - onNext(d)
     * 22:40:00.736 [parallel-6] INFO reactor.Flux.Merge.1 - onNext(e)
     * 22:40:00.736 [parallel-6] INFO reactor.Flux.Merge.1 - onComplete()
     *
     * @throws InterruptedException
     */
    @Test
    void testFluxMerge() throws InterruptedException {
        final Flux<String> firstFlux = Flux.just("a", "c", "e")
                .delayElements(Duration.ofMillis(500));

        final Flux<String> secondFlux = Flux.just("b", "d")
                .delaySubscription(Duration.ofMillis(250))
                .delayElements(Duration.ofMillis(500));

        // 合并
        final Flux<String> finalFlux = firstFlux.mergeWith(secondFlux);

        finalFlux.log()
                .subscribe();

        TimeUnit.SECONDS.sleep(6);


    }

    /**
     * 22:49:41.939 [main] DEBUG reactor.util.Loggers$LoggerFactory - Using Slf4j logging framework
     * 22:49:41.991 [main] INFO reactor.Flux.Merge.1 - onSubscribe(FluxFlatMap.FlatMapMain)
     * 22:49:41.992 [main] INFO reactor.Flux.Merge.1 - request(unbounded)
     * 22:49:42.499 [parallel-1] INFO reactor.Flux.Merge.1 - onNext(a)
     * 22:49:42.499 [parallel-1] INFO reactor.Flux.Merge.1 - onNext(c)
     * 22:49:42.499 [parallel-1] INFO reactor.Flux.Merge.1 - onNext(e)
     * 22:49:42.753 [parallel-3] INFO reactor.Flux.Merge.1 - onNext(b)
     * 22:49:42.753 [parallel-3] INFO reactor.Flux.Merge.1 - onNext(d)
     * 22:49:42.753 [parallel-3] INFO reactor.Flux.Merge.1 - onComplete()
     *
     * <img class="marble" src="https://projectreactor.io/docs/core/release/api/reactor/core/publisher/doc-files/marbles/delaySequence.svg" alt="">
     * @throws InterruptedException
     */
    @Test
    void testFluxDelaySequence() throws InterruptedException {
        // 会在这个时间段同时发布
        final Flux<String> firstFlux = Flux.just("a", "c", "e")
                .delaySequence(Duration.ofMillis(500));

        final Flux<String> secondFlux = Flux.just("b", "d")
                .delaySubscription(Duration.ofMillis(250))
                .delaySequence(Duration.ofMillis(500));

        // 合并
        final Flux<String> finalFlux = firstFlux.mergeWith(secondFlux);

        finalFlux.log().subscribe();

        TimeUnit.SECONDS.sleep(6);
    }

    @Test
    void testFluxZip() {
        final Flux<String> keyFlux = Flux.just("key1", "key2", "key3");
        final Flux<String> valueFlux = Flux.just("value1", "value2", "value3");
        final Flux<Tuple2<String, String>> mapFlux = keyFlux.zipWith(valueFlux);

//        mapFlux.log()
//                .subscribe();

        StepVerifier.create(mapFlux)
                .expectNextMatches(p -> p.getT1().equals("key1"))
                .expectNextMatches(p -> p.getT2().equals("value2"))
                .thenCancel()
                .log()
                .verify();
//                .expectNextMatches(p -> p.getT2().equals("value3"))
//                .verifyComplete();
    }

    @Test
    void testFluxZipWithCombinator() {
        final Flux<String> keyFlux = Flux.just("key1", "key2", "key3");
        final Flux<String> valueFlux = Flux.just("value1", "value2", "value3");
        final Flux<String> stringFlux = keyFlux.zipWith(valueFlux, (key, value) -> String.format("key: %s -> value: %s", key, value));

        stringFlux.log().subscribe();

    }

    /**
     * 23:36:11.278 [main] DEBUG reactor.util.Loggers$LoggerFactory - Using Slf4j logging framework
     * 23:36:11.325 [main] INFO reactor.Flux.FirstEmitting.1 - onSubscribe(FluxFirstEmitting.RaceCoordinator)
     * 23:36:11.327 [main] INFO reactor.Flux.FirstEmitting.1 - request(unbounded)
     * 23:36:11.836 [parallel-1] INFO reactor.Flux.FirstEmitting.1 - onNext(a)
     * 23:36:12.337 [parallel-4] INFO reactor.Flux.FirstEmitting.1 - onNext(c)
     * 23:36:12.838 [parallel-5] INFO reactor.Flux.FirstEmitting.1 - onNext(e)
     * 23:36:12.838 [parallel-5] INFO reactor.Flux.FirstEmitting.1 - onComplete()
     *
     * 只取第一个source
     *
     * @throws InterruptedException
     */
    @Test
    void testFluxFirst() throws InterruptedException {
        final Flux<String> fistFlux = Flux.just("a", "c", "e")
                .delayElements(Duration.ofMillis(500));

        final Flux<String> secondFlux = Flux.just("b", "d")
                .delaySubscription(Duration.ofMillis(250))
                .delayElements(Duration.ofMillis(500));

        // 只会有第一个的数据
        Flux.first(fistFlux, secondFlux)
                .log()
                .subscribe();

        TimeUnit.SECONDS.sleep(5);
    }

    @Test
    void testFluxSkip() throws InterruptedException {
        Flux.range(0, 10)
                .skip(3)
                .log()
                .subscribe();

        Flux.range(0, 10)
                .delayElements(Duration.ofMillis(500))
                .skip(Duration.ofSeconds(2))
                .log()
                .subscribe();

        TimeUnit.SECONDS.sleep(5);
    }

    @Test
    void testFluxTake() throws InterruptedException {
        Flux.range(0, 10)
                .take(5)
                .log()
                .subscribe();

        Flux.range(0, 10)
                .delayElements(Duration.ofMillis(250))
                .take(Duration.ofSeconds(2))
                .log()
                .subscribe();
        TimeUnit.SECONDS.sleep(5);
    }
}
