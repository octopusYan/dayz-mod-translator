package cn.octopusyan.dayzmodtranslator.manager.translate;

import cn.octopusyan.dayzmodtranslator.config.CustomConfig;
import cn.octopusyan.dayzmodtranslator.manager.thread.ThreadFactory;
import cn.octopusyan.dayzmodtranslator.manager.thread.ThreadPoolManager;
import cn.octopusyan.dayzmodtranslator.manager.translate.factory.TranslateFactory;
import cn.octopusyan.dayzmodtranslator.manager.translate.factory.TranslateFactoryImpl;
import javafx.application.Platform;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

/**
 * 翻译工具
 *
 * @author octopus_yan@foxmail.com
 */
public class TranslateUtil {
    private static final Logger logger = LoggerFactory.getLogger(TranslateUtil.class);
    private static TranslateUtil util;
    private static final DelayQueue<DelayWord> delayQueue = new DelayQueue<>();
    private final TranslateFactory factory;
    private static WordThread wordThread;
    private static ThreadPoolExecutor threadPoolExecutor;

    private TranslateUtil(TranslateFactory factory) {
        this.factory = factory;
    }

    public static TranslateUtil getInstance() {
        if (util == null) {
            util = new TranslateUtil(TranslateFactoryImpl.getInstance());
        }
        return util;
    }

    /**
     * 提交翻译任务
     *
     * @param index    序号
     * @param original 原始文本
     * @param listener 翻译结果回调 (主线程)
     */
    public void translate(int index, String original, OnTranslateListener listener) {

        // 设置延迟时间
        DelayWord word = factory.getDelayWord(CustomConfig.translateSource(), index, original, listener);
        // 添加到延迟队列
        delayQueue.add(word);

        if (wordThread == null) {
            wordThread = new WordThread();
            wordThread.start();
        }
    }

    /**
     * 清除翻译任务
     */
    public void clear() {
        // 尝试停止所有线程
        getThreadPoolExecutor().shutdownNow();
        // 清空队列
        delayQueue.clear();
        // 设置停止标记
        if (wordThread != null)
            wordThread.setStop(true);
        wordThread = null;
    }

    /**
     * 获取翻译任务用线程池
     */
    public static ThreadPoolExecutor getThreadPoolExecutor() {
        if (threadPoolExecutor == null || threadPoolExecutor.isShutdown()) {
            threadPoolExecutor = new ThreadPoolExecutor(32,
                    200,
                    10,
                    TimeUnit.SECONDS,
                    new LinkedBlockingQueue<>(200),
                    new ThreadFactory(ThreadFactory.DEFAULT_THREAD_PREFIX),
                    new ThreadPoolExecutor.DiscardPolicy());
        }
        return threadPoolExecutor;
    }

    public interface OnTranslateListener {
        void onTranslate(String result);
    }

    /**
     * 延迟翻译对象
     */
    public static class DelayWord implements Delayed {
        private TranslateSource source;
        private final int index;
        private final String original;
        private final OnTranslateListener listener;
        private long time;

        public DelayWord(int index, String original, OnTranslateListener listener) {
            this.index = index;
            this.original = original;
            this.listener = listener;

        }

        public void setSource(TranslateSource source) {
            this.source = source;
        }

        public void setTime(long time, TimeUnit timeUnit) {
            this.time = System.currentTimeMillis() + (time > 0 ? timeUnit.toMillis(time) : 0);
        }

        public TranslateSource getSource() {
            return source;
        }

        public int getIndex() {
            return index;
        }

        public String getOriginal() {
            return original;
        }

        public OnTranslateListener getListener() {
            return listener;
        }

        @Override
        public long getDelay(TimeUnit unit) {
            return time - System.currentTimeMillis();
        }

        @Override
        public int compareTo(Delayed o) {
            DelayWord word = (DelayWord) o;
            return Integer.compare(this.index, word.index);
        }
    }

    /**
     * 延迟队列处理线程
     */
    private static class WordThread extends Thread {
        private boolean stop = false;

        public void setStop(boolean stop) {
            this.stop = stop;
        }

        @Override
        public void run() {
            List<DelayWord> tmp = new ArrayList<>();
            while (!delayQueue.isEmpty()) {
                // 停止处理
                if (stop) {
                    this.interrupt();
                    return;
                }

                try {
                    // 取出待翻译文本
                    DelayWord take = delayQueue.take();
                    tmp.add(take);

                    if (tmp.size() < CustomConfig.translateSourceQps(take.source))
                        continue;

                    tmp.forEach(word -> {
                        try {
                            getThreadPoolExecutor().execute(() -> {
                                // 翻译
                                try {
                                    String translate = util.factory.translate(word.getSource(), word.getOriginal());
                                    // 回调监听器
                                    if (word.getListener() != null)
                                        // 主线程处理翻译结果
                                        Platform.runLater(() -> word.getListener().onTranslate(translate));
                                } catch (InterruptedException ignored) {
                                } catch (Exception e) {
                                    logger.error("翻译出错", e);
                                    throw new RuntimeException(e);
                                }
                            });
                        } catch (Exception e) {
                            logger.error("翻译出错", e);
                            throw new RuntimeException(e);
                        }
                    });

                    tmp.clear();
                } catch (InterruptedException ignored) {
                }
            }

            // 处理剩余
            tmp.forEach(word -> {
                try {
                    ThreadPoolManager.getInstance().execute(() -> {
                        // 翻译
                        try {
                            String translate = util.factory.translate(word.getSource(), word.getOriginal());
                            // 回调监听器
                            if (word.getListener() != null)
                                // 主线程处理翻译结果
                                Platform.runLater(() -> word.getListener().onTranslate(translate));
                        } catch (Exception e) {
                            logger.error("翻译出错", e);
                            throw new RuntimeException(e);
                        }
                    });
                } catch (Exception e) {
                    logger.error("翻译出错", e);
                    throw new RuntimeException(e);
                }
            });

            tmp.clear();
        }
    }
}
