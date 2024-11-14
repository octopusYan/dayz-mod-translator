package cn.octopusyan.dmt.common.manager.thread;


import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 线程池管理类
 */
public final class ThreadPoolManager extends ThreadPoolExecutor {

    private static volatile ThreadPoolManager sInstance;
    private static final List<ThreadPoolManager> poolManagerList = new ArrayList<>();

    private ThreadPoolManager() {
        this("");
    }

    private ThreadPoolManager(String threadPoolName) {
        super(32,
                200,
                10,
                TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(200),
                new ThreadFactory(StringUtils.isEmpty(threadPoolName) ? ThreadFactory.DEFAULT_THREAD_PREFIX : threadPoolName),
                new DiscardPolicy());
    }

    public static ThreadPoolManager getInstance(String threadPoolName) {
        ThreadPoolManager threadPoolManager = new ThreadPoolManager(threadPoolName);
        poolManagerList.add(threadPoolManager);
        return threadPoolManager;
    }

    public static ThreadPoolManager getInstance() {
        if (sInstance == null) {
            synchronized (ThreadPoolManager.class) {
                if (sInstance == null) {
                    sInstance = new ThreadPoolManager();
                }
            }
        }
        return sInstance;
    }

    public static void shutdownAll() {
        getInstance().shutdown();
        poolManagerList.forEach(ThreadPoolExecutor::shutdown);
    }
}