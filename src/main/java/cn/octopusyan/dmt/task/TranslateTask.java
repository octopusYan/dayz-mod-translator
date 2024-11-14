package cn.octopusyan.dmt.task;

import cn.octopusyan.dmt.common.manager.thread.ThreadPoolManager;
import cn.octopusyan.dmt.model.WordItem;
import cn.octopusyan.dmt.task.base.BaseTask;
import cn.octopusyan.dmt.task.listener.DefaultTaskListener;
import cn.octopusyan.dmt.translate.DelayWord;
import cn.octopusyan.dmt.translate.TranslateUtil;
import cn.octopusyan.dmt.view.ConsoleLog;
import javafx.application.Platform;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.DelayQueue;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 翻译工具
 *
 * @author octopus_yan@foxmail.com
 */
public class TranslateTask extends BaseTask<DefaultTaskListener> {

    public static final ConsoleLog consoleLog = ConsoleLog.getInstance(TranslateTask.class);
    @Getter
    private final ThreadPoolManager threadPoolManager = ThreadPoolManager.getInstance("translate-pool");
    private final DelayQueue<DelayWord> delayQueue;
    private final long total;
    private final AtomicLong quantity = new AtomicLong();
    private final CountDownLatch countDownLatch;

    public TranslateTask(List<WordItem> data) {
        this(TranslateUtil.getDelayQueue(data), data.size());
    }

    public TranslateTask(DelayQueue<DelayWord> queue, int total) {
        super("Translate");
        this.delayQueue = queue;
        this.total = total;
        this.quantity.set(total - delayQueue.size());
        countDownLatch = new CountDownLatch(queue.size());

        updateProgress(quantity.get(), total);
    }

    @Override
    protected void task() throws Exception {

        while (!delayQueue.isEmpty() && !isCancelled()) {
            // 取出文本
            DelayWord word = delayQueue.take();

            // 多线程处理
            threadPoolManager.execute(() -> {
                // 翻译
                try {
                    if (total == 1) {
                        consoleLog.info("正在翻译：{}", word.getWord().getOriginal());
                    }

                    String translate = TranslateUtil.translate(word.getApi(), word.getWord().getOriginal());
                    // 回调监听器
                    if (StringUtils.isEmpty(translate)) return;

                    synchronized (quantity) {
                        long progress = quantity.addAndGet(1);
                        // 设置翻译结果
                        Platform.runLater(() -> word.getWord().getChineseProperty().setValue(translate));
                        // 更新进度
                        updateProgress(progress, total);
                        // 输出信息
                        if (total != 1) {
                            consoleLog.info("正在翻译（{}/{}）", progress, total);
                        }
                    }

                } catch (Exception e) {
                    if (!(e instanceof InterruptedException) || !isCancelled()) {
                        consoleLog.error("翻译失败", e);
                    }
                    delayQueue.add(word);
                }
                countDownLatch.countDown();
            });
        }

        countDownLatch.await();
    }
}
