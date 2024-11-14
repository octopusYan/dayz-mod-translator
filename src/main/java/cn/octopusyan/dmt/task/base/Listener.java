package cn.octopusyan.dmt.task.base;

/**
 * 任务监听
 *
 * @author octopus_yan
 */
public interface Listener {

    default void onStart() {
    }

    default void onRunning() {
    }

    default void onCancelled() {
    }

    default void onFailed(Throwable throwable) {
    }

    void onSucceeded();
}
