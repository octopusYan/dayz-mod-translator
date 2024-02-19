package cn.octopusyan.dayzmodtranslator.manager.thread;


import java.util.concurrent.*;

/**
 * 线程池管理类
 */
public final class ThreadPoolManager extends ThreadPoolExecutor {

    private static volatile ThreadPoolManager sInstance;

    private static ScheduledExecutorService scheduledExecutorService;

    private ThreadPoolManager() {
        super(32,
                200,
                10,
                TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(200),
                new ThreadFactory(ThreadFactory.DEFAULT_THREAD_PREFIX),
                new ThreadPoolExecutor.DiscardPolicy());
    }

    public static ThreadPoolManager getInstance() {
        if (sInstance == null) sInstance = new ThreadPoolManager();
        return sInstance;
    }
}