package cn.octopusyan.dmt.common.config;

import cn.octopusyan.dmt.Application;
import cn.octopusyan.dmt.common.base.BaseController;
import cn.octopusyan.dmt.common.util.FxmlUtil;
import cn.octopusyan.dmt.common.util.ProcessesUtil;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.util.Callback;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * 上下文
 *
 * @author octopus_yan
 */
public class Context {
    @Getter
    private static Application application;
    private static final Logger log = LoggerFactory.getLogger(Context.class);
    public static final ObjectProperty<Scene> sceneProperty = new SimpleObjectProperty<>();

    /**
     * 控制器集合
     */
    @Getter
    private static final Map<String, BaseController<?>> controllers = new HashMap<>();

    private Context() {
        throw new IllegalStateException("Utility class");
    }

    public static boolean isDebugMode() {
        RuntimeMXBean runtimeMXBean = ManagementFactory.getRuntimeMXBean();
        return runtimeMXBean.getInputArguments().toString().contains("-agentlib:jdwp");
    }

    // 获取控制工厂
    public static Callback<Class<?>, Object> getControlFactory() {
        return type -> {
            try {
                return type.getDeclaredConstructor().newInstance();
            } catch (Exception e) {
                log.error("", e);
                return null;
            }
        };
    }

    public static void setApplication(Application application) {
        Context.application = application;
    }

    /**
     * 初始化场景
     *
     * @return Scene
     */
    public static Scene initScene() {
        try {
            FXMLLoader loader = FxmlUtil.load("main-view");
            //底层面板
            Pane root = loader.load();
            Optional.ofNullable(sceneProperty.get()).ifPresentOrElse(
                    s -> s.setRoot(root),
                    () -> {
                        Scene scene = new Scene(root, root.getPrefWidth() + 20, root.getPrefHeight() + 20, Color.TRANSPARENT);
                        URL resource = Objects.requireNonNull(Context.class.getResource("/css/main-view.css"));
                        scene.getStylesheets().addAll(resource.toExternalForm());
                        scene.setFill(Color.TRANSPARENT);
                        sceneProperty.set(scene);
                    }
            );
        } catch (Throwable e) {
            log.error("loadScene error", e);
        }
        return sceneProperty.get();
    }

    public static void openUrl(String url) {
        getApplication().getHostServices().showDocument(url);
    }

    public static void openFolder(File file) {
        openFile(file);
    }

    public static void openFile(File file) {
        if (!file.exists()) return;

        if (file.isDirectory()) {
            ProcessesUtil.init(file.getAbsolutePath()).exec("explorer.exe .");
        } else {
            ProcessesUtil.init(file.getParentFile().getAbsolutePath()).exec(STR."explorer.exe /select,\{file.getName()}");
        }
    }
}