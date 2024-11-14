package cn.octopusyan.dmt.task.base;

import cn.octopusyan.dmt.common.manager.thread.ThreadPoolManager;
import cn.octopusyan.dmt.task.listener.DefaultTaskListener;
import javafx.concurrent.Task;
import lombok.Getter;

/**
 * @author octopus_yan
 */
public abstract class BaseTask<T extends Listener> extends Task<Void> {
    private final ThreadPoolManager Executor = ThreadPoolManager.getInstance("task-pool");
    protected T listener;
    @Getter
    private final String name;

    protected BaseTask(String name) {
        this.name = name;
    }

    public String getNameTag() {
        return "Task " + name;
    }

    @Override
    protected Void call() throws Exception {
        if (listener != null) listener.onStart();
        task();
        return null;
    }

    protected abstract void task() throws Exception;

    public void onListen(T listener) {
        this.listener = listener;
        if (this.listener == null)
            return;
        if (listener instanceof DefaultTaskListener lis)
            lis.setTask((BaseTask) this);

        setOnRunning(_ -> listener.onRunning());
        setOnCancelled(_ -> listener.onCancelled());
        setOnFailed(_ -> listener.onFailed(getException()));
        setOnSucceeded(_ -> listener.onSucceeded());
    }

    public void execute() {
        Executor.execute(this);
    }
}
