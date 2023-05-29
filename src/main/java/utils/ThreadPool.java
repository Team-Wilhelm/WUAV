package utils;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Thread pool to reuse an executor service throughout the application.
 */
public class ThreadPool {
    private static ThreadPool instance;
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

    /**
     * Shuts down the thread pool, waiting for all tasks to finish.
     * If tasks do not finish, they are interrupted and the thread pool is shut down forcefully.
     */
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

    public ExecutorService getExecutorService() {
        return executorService;
    }
}
