package com.example.maimaibackend.ticketsource.gateway;

import jakarta.annotation.PreDestroy;
import org.springframework.stereotype.Component;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 对适配器调用增加统一硬超时，避免某个外部票源长期占用请求线程。
 */
@Component
public class TicketSourceCallExecutor {
    private final ExecutorService executorService;
    private final int hardTimeoutMs;

    public TicketSourceCallExecutor(TicketSourceGatewayProperties properties) {
        int workerCount = Math.max(1, properties.getWorkerCount());
        int queueCapacity = Math.max(1, properties.getQueueCapacity());
        this.hardTimeoutMs = Math.max(100, properties.getHardTimeoutMs());
        AtomicInteger sequence = new AtomicInteger(1);
        ThreadFactory threadFactory = task -> {
            Thread thread = new Thread(task, "ticket-source-gateway-" + sequence.getAndIncrement());
            thread.setDaemon(true);
            return thread;
        };
        this.executorService = new ThreadPoolExecutor(
                workerCount,
                workerCount,
                0L,
                TimeUnit.MILLISECONDS,
                new ArrayBlockingQueue<>(queueCapacity),
                threadFactory,
                new ThreadPoolExecutor.AbortPolicy()
        );
    }

    public <T> T execute(Callable<T> callable, Integer providerReadTimeoutMs)
            throws TimeoutException, ExecutionException, InterruptedException {
        int configuredTimeout = providerReadTimeoutMs == null || providerReadTimeoutMs <= 0
                ? hardTimeoutMs
                : providerReadTimeoutMs;
        int timeoutMs = Math.min(configuredTimeout, hardTimeoutMs);
        Future<T> future = executorService.submit(callable);
        try {
            return future.get(timeoutMs, TimeUnit.MILLISECONDS);
        } catch (TimeoutException e) {
            future.cancel(true);
            throw e;
        }
    }

    @PreDestroy
    public void shutdown() {
        executorService.shutdownNow();
    }
}
