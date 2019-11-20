package com.ximo.springwebfluxinaction;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.test.StepVerifier;
import reactor.util.function.Tuple2;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
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

        // CompletableFuture
        CompletableFuture.supplyAsync(() -> 2)
                .whenComplete((data, error) -> log.info("data: {}", data, error));
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

    /**
     * [apple, orange, banana][kiwi, strawberry]
     */
    @Test
    void testBuffer() {
        Flux<String> fruitFlux = Flux.just(
                "apple", "orange", "banana", "kiwi", "strawberry");
        Flux<List<String>> bufferedFlux = fruitFlux.buffer(3);

        StepVerifier
                .create(bufferedFlux)
                .expectNext(Arrays.asList("apple", "orange", "banana"))
                .expectNext(Arrays.asList("kiwi", "strawberry"))
                .verifyComplete();

        bufferedFlux.log().subscribe();
        bufferedFlux.subscribe(System.out::print);
    }

    /**
     * 18:59:18.753 [main] DEBUG reactor.util.Loggers$LoggerFactory - Using Slf4j logging framework
     * 18:59:18.769 [main] INFO reactor.Flux.Buffer.1 - onSubscribe(FluxBuffer.BufferExactSubscriber)
     * 18:59:18.772 [main] INFO reactor.Flux.Buffer.1 - request(256)
     * 18:59:18.773 [main] INFO reactor.Flux.Buffer.1 - onNext([apple, orange, banana])
     * 18:59:18.784 [main] INFO reactor.Flux.SubscribeOn.2 - onSubscribe(FluxSubscribeOn.SubscribeOnSubscriber)
     * 18:59:18.784 [main] INFO reactor.Flux.SubscribeOn.2 - request(32)
     * 18:59:18.787 [main] INFO reactor.Flux.Buffer.1 - onNext([kiwi, strawberry])
     * 18:59:18.787 [main] INFO reactor.Flux.SubscribeOn.3 - onSubscribe(FluxSubscribeOn.SubscribeOnSubscriber)
     * 18:59:18.787 [main] INFO reactor.Flux.SubscribeOn.3 - request(32)
     * 18:59:18.787 [main] INFO reactor.Flux.Buffer.1 - onComplete()
     * 18:59:18.792 [parallel-2] INFO reactor.Flux.SubscribeOn.3 - onNext(KIWI)
     * 18:59:18.793 [parallel-2] INFO reactor.Flux.SubscribeOn.3 - onNext(STRAWBERRY)
     * 18:59:18.793 [parallel-2] INFO reactor.Flux.SubscribeOn.3 - onComplete()
     * 18:59:18.792 [parallel-1] INFO reactor.Flux.SubscribeOn.2 - onNext(APPLE)
     * 18:59:18.794 [parallel-1] INFO reactor.Flux.SubscribeOn.2 - onNext(ORANGE)
     * 18:59:18.795 [parallel-1] INFO reactor.Flux.SubscribeOn.2 - onNext(BANANA)
     * 18:59:18.795 [parallel-1] INFO reactor.Flux.SubscribeOn.2 - onComplete()
     * 前三个 和 后两个 都在同时执行
     *
     */
    @Test
    void tetFluxBufferAndFlatMap() {
        Flux.just("apple", "orange", "banana", "kiwi", "strawberry")
                .buffer(3)
                .log()
                .flatMap(list -> Flux.fromIterable(list)
                        .map(String::toUpperCase)
                        .subscribeOn(Schedulers.parallel())
                        .log()
                ).subscribe();


    }

    @Test
    void testFluxFilter() {
        Flux.range(0, 10)
                .filter(data -> data % 2 == 0)
                .log()
                .subscribe();
    }

    @Test
    void testFluxDistinct() {
        Flux.just("22", "333", "22", "44")
                .distinct()
                .log()
                .subscribe();
    }

    @Test
    void testFluxMap() {
        Flux.range(0, 10)
                .map(data -> data * data)
                .log()
                .subscribe();
    }

    @Test
    void testFluxFlatMap() {
        Flux.just("aaaa,aaa", "bbbb,bbb", "cccc,ccc")
                .flatMap(data -> {
                    String[] inputArray = data.split(",");
                    return Flux.fromArray(inputArray);
                }).log()
//                .subscribeOn(Schedulers.elastic())
                .subscribe();
    }

    @Test
    void testFluxCollectList() {
        Mono<List<Integer>> listMono = Flux.range(0, 9)
                .collectList();

        // emitted
        List<Integer> block = listMono.log().block();
    }

    @Test
    void testFluxCollectMap() {

        Flux<String> animalFlux = Flux.just(
                "aardvark", "elephant", "koala", "eagle", "kangaroo");

        // 就等于identity 不写就是它自己
        animalFlux.collectMap(data -> data.charAt(0));

    }
}
