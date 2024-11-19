package cn.octopusyan.dmt.viewModel;

import atlantafx.base.theme.Styles;
import cn.octopusyan.dmt.common.base.BaseViewModel;
import cn.octopusyan.dmt.controller.MainController;
import cn.octopusyan.dmt.model.WordItem;
import cn.octopusyan.dmt.task.PackTask;
import cn.octopusyan.dmt.task.TranslateTask;
import cn.octopusyan.dmt.task.UnpackTask;
import cn.octopusyan.dmt.task.listener.DefaultTaskListener;
import cn.octopusyan.dmt.translate.DelayWord;
import cn.octopusyan.dmt.translate.TranslateUtil;
import cn.octopusyan.dmt.view.ConsoleLog;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.ObservableList;
import javafx.concurrent.Worker;
import javafx.scene.control.ProgressIndicator;
import org.apache.commons.lang3.StringUtils;
import org.kordamp.ikonli.feather.Feather;
import org.kordamp.ikonli.javafx.FontIcon;

import java.io.File;
import java.util.List;
import java.util.concurrent.DelayQueue;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 主界面
 *
 * @author octopus_yan
 */
public class MainViewModel extends BaseViewModel<MainController> {
    private static final ConsoleLog consoleLog = ConsoleLog.getInstance(MainViewModel.class);
    /**
     * 解包任务
     */
    private UnpackTask unpackTask;
    /**
     * 翻译任务
     */
    private TranslateTask translateTask;
    private DelayQueue<DelayWord> delayQueue;
    private String unpackPath;
    private int total;

    FontIcon startIcon = new FontIcon(Feather.PLAY);
    FontIcon pauseIcon = new FontIcon(Feather.PAUSE);
    private List<WordItem> wordItems;

    private final StringProperty fileName = new SimpleStringProperty();

    public StringProperty fileNameProperty() {
        return fileName;
    }

    /**
     * 加载PBO文件
     */
    public void selectFile(File pboFile) {
        if (pboFile == null) return;

        fileName.setValue(pboFile.getAbsolutePath());

        unpackTask = new UnpackTask(pboFile);
    }

    /**
     * 解包
     */
    public void unpack() {
        if (unpackTask == null) return;

        unpackTask.onListen(new UnpackTask.UnpackListener() {

            @Override
            public void onRunning() {
                // 展示加载
                controller.onLoad();
                // 重置进度
                resetProgress();
            }

            @Override
            public void onUnpackOver(String path) {
                MainViewModel.this.unpackPath = path;
                Platform.runLater(() -> controller.onUnpack(new File(path)));
            }

            @Override
            public void onFindWordOver(List<WordItem> wordItems) {
                total = wordItems.size();
                MainViewModel.this.wordItems = wordItems;
                Platform.runLater(() -> controller.onLoadWord(wordItems));
            }
        });

        unpackTask.execute();
    }

    /**
     * 开始翻译
     */
    public void startTranslate() {
        if (wordItems.isEmpty()) return;

        if (translateTask == null) {
            List<WordItem> words = wordItems.stream().filter(item -> StringUtils.isEmpty(item.getChinese())).toList();
            delayQueue = TranslateUtil.getDelayQueue(words);
            translateTask = createTask();
        }

        if (!translateTask.isRunning()) {
            // 检查进度
            if (!delayQueue.isEmpty()) {
                AtomicInteger index = new AtomicInteger(0);
                delayQueue.forEach(item -> TranslateUtil.resetDelayTime(index.getAndIncrement(), item));
                translateTask = createTask();
            }

            if (translateTask.getState() != Worker.State.SUCCEEDED) {
                translateTask.execute();
                // 展示进度
                controller.translateProgress.setVisible(true);
                controller.translateProgress.progressProperty().bind(translateTask.progressProperty());
            }
        } else {
            translateTask.cancel();
        }

    }

    /**
     * 打包
     */
    public void pack() {
        if (wordItems.isEmpty()) return;

        PackTask packTask = new PackTask(wordItems, unpackPath);
        packTask.onListen(new PackTask.PackListener() {
            @Override
            public void onWriteOver() {
                consoleLog.info("写入完成");
            }

            @Override
            public void onPackOver(File file) {
                Platform.runLater(() -> controller.onPackOver(file));
            }

        });
        packTask.execute();
    }

    private TranslateTask createTask() {
        TranslateTask task = new TranslateTask(delayQueue, total);
        task.onListen(new DefaultTaskListener() {

            @Override
            public void onRunning() {
                ProgressIndicator graphic = new ProgressIndicator();
                graphic.setPrefWidth(15);
                graphic.setPrefHeight(15);
                graphic.setOnMouseClicked(_ -> controller.startTranslate());
                controller.translate.setGraphic(graphic);
                controller.translateProgress.setVisible(true);
            }

            @Override
            public void onCancelled() {
                task.getThreadPoolManager().shutdownNow();
                TranslateTask.consoleLog.info("翻译暂停");
                Platform.runLater(() -> controller.translate.setGraphic(pauseIcon));
            }

            @Override
            protected void onSucceed() {
                if (delayQueue.isEmpty()) {
                    Platform.runLater(() -> controller.translate.setGraphic(startIcon));
                } else {
                    Platform.runLater(() -> controller.translate.setGraphic(pauseIcon));
                }
            }
        });
        return task;
    }

    /**
     * 加载PBO文件后，重置进度
     */
    private void resetProgress() {
        translateTask = null;
        controller.translate.setGraphic(startIcon);
        ObservableList<String> styleClass = controller.translateProgress.getStyleClass();
        if (!styleClass.contains(Styles.SMALL)) {
            Styles.toggleStyleClass(controller.translateProgress, Styles.SMALL);
        }
        controller.translateProgress.progressProperty().unbind();
        controller.translateProgress.setProgress(0);
        controller.translateProgress.setVisible(false);
    }
}
