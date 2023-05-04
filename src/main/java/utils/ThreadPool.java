package utils;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class ThreadPool {
    private static ThreadPool instance;
    private static int threadCount = 0;
    private ExecutorService executorService;

    private ThreadPool() {
        executorService = Executors.newCachedThreadPool();
    }

    public static ThreadPool getInstance() {
        if (instance == null) {
            instance = new ThreadPool();
        }
        return instance;
    }

    public void execute(Runnable runnable) {
        executorService.submit(runnable);
    }

    public <T> Future<T> submit(Callable<T> callable) {
        return executorService.submit(callable);
    }

    public void shutdown() {
        try {
            executorService.shutdown();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (!executorService.isTerminated())
                executorService.shutdownNow();
        }
    }
}
