package dev.heliosclient.system;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

public class HeliosExecutor {
    private static final AtomicInteger threadNumber = new AtomicInteger(1);
    private static final ExecutorService executorService = Executors.newCachedThreadPool((task) -> {
        Thread thread = new Thread(task);
        thread.setDaemon(true);
        thread.setName("Helios-Executor-" + threadNumber.getAndIncrement());
        return thread;
    });

    public static void execute(Runnable task) {
        executorService.execute(task);
    }
    public static void shutdown() {
        executorService.shutdown();
    }
    public static boolean isShutdown() {
        return executorService.isShutdown();
    }

    public static Future<?> submit(Runnable task) {
        return executorService.submit(task);
    }

    public static <T> Future<T> submit(Callable<T> task) {
        return executorService.submit(task);
    }

}