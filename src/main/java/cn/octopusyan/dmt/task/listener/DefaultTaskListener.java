package cn.octopusyan.dmt.task.listener;

import cn.octopusyan.dmt.task.base.BaseTask;
import cn.octopusyan.dmt.task.base.Listener;
import cn.octopusyan.dmt.view.ConsoleLog;
import cn.octopusyan.dmt.view.alert.AlertUtil;
import cn.octopusyan.dmt.view.alert.builder.ProgressBuilder;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * 任务监听器默认实现
 *
 * @author octopus_yan
 */
@Slf4j
public abstract class DefaultTaskListener implements Listener {
    private ConsoleLog consoleLog;
    @Getter
    private BaseTask<? extends DefaultTaskListener> task;
    /**
     * 加载弹窗
     */
    @Getter
    final ProgressBuilder progress = AlertUtil.getInstance().progress();

    /**
     * 是否展示加载弹窗
     */
    private final boolean showProgress;

    public DefaultTaskListener() {
        this(false);
    }

    public DefaultTaskListener(boolean showProgress) {
        this.showProgress = showProgress;
    }

    public <L extends DefaultTaskListener> void setTask(BaseTask<L> task) {
        this.task = task;
        consoleLog = ConsoleLog.getInstance(task.getClass().getSimpleName());
        progress.onCancel(task::cancel);
    }

    @Override
    public void onStart() {
        consoleLog.info(STR."\{task.getNameTag()} start ...");
    }

    @Override
    public void onRunning() {
        // 展示加载弹窗
        if (showProgress)
            progress.show();
    }

    @Override
    public void onCancelled() {
        progress.close();
        consoleLog.info(STR."\{task.getNameTag()} cancel ...");
    }

    @Override
    public void onFailed(Throwable throwable) {
        progress.close();
        consoleLog.error(STR."\{task.getNameTag()} fail ...", throwable);
    }

    @Override
    public void onSucceeded() {
        progress.close();
        consoleLog.info(STR."\{task.getNameTag()} success ...");
        onSucceed();
    }

    protected abstract void onSucceed();
}
