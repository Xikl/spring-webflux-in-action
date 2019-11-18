package com.ximo.springwebfluxinaction;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.concurrent.BasicThreadFactory;
import org.junit.jupiter.api.Test;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.*;

/**
 * @author xikl
 * @date 2019/11/18
 */
@Slf4j
public class TestQueue {

    private static final ExecutorService EXECUTOR_SERVICE = Executors.newFixedThreadPool(5,
            new BasicThreadFactory.Builder().daemon(true).namingPattern("queue-work-%d").build());

    @Test
    void testMultiThreadReadFromQueue() throws InterruptedException {
        BlockingQueue<String> blockingQueue = new LinkedBlockingQueue<>();
        for (int i = 1; i <= 10000; i++) {
            String data = String.format("data: %s", i);
            blockingQueue.add(data);
        }

        Set<String> resultSet = new CopyOnWriteArraySet<>();
        CountDownLatch countDownLatch = new CountDownLatch(5);
        for (int i = 0; i < 5; i++) {
            EXECUTOR_SERVICE.submit(() -> {
                List<String> resultList = new LinkedList<>();
                for (int j = 1; j <= 2000; j++) {
                    try {
                        final String result = blockingQueue.take();
                        resultList.add(result);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                log.info("result size: {}", resultList.size());
                resultSet.addAll(resultList);
                // must be in finally countDown
                countDownLatch.countDown();
            });
        }
        countDownLatch.await();
        log.info("final result size： {}", resultSet.size());
    }
}
