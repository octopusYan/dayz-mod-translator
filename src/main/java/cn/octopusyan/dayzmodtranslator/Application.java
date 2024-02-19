package cn.octopusyan.dayzmodtranslator;

import cn.octopusyan.dayzmodtranslator.config.AppConstant;
import cn.octopusyan.dayzmodtranslator.config.CustomConfig;
import cn.octopusyan.dayzmodtranslator.manager.http.HttpConfig;
import cn.octopusyan.dayzmodtranslator.controller.MainController;
import cn.octopusyan.dayzmodtranslator.manager.CfgConvertUtil;
import cn.octopusyan.dayzmodtranslator.manager.PBOUtil;
import cn.octopusyan.dayzmodtranslator.manager.thread.ThreadPoolManager;
import cn.octopusyan.dayzmodtranslator.manager.translate.TranslateUtil;
import cn.octopusyan.dayzmodtranslator.util.AlertUtil;
import cn.octopusyan.dayzmodtranslator.util.FxmlUtil;
import cn.octopusyan.dayzmodtranslator.manager.http.HttpUtil;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ProxySelector;
import java.util.Objects;

public class Application extends javafx.application.Application {
    private static final Logger logger = LoggerFactory.getLogger(Application.class);

    @Override
    public void init() throws Exception {
        logger.info("application init ...");
    }

    @Override
    public void start(Stage stage) throws IOException {

        logger.info("application start ...");

        // bin转换 工具初始化
        CfgConvertUtil.init();

        // PBO 工具初始化
        PBOUtil.init(CfgConvertUtil.getInstance());

        // 客户端配置初始化
        CustomConfig.init();

        // 初始化弹窗工具
        AlertUtil.initOwner(stage);

        // http请求工具初始化
        HttpConfig httpConfig = new HttpConfig();
        if (CustomConfig.hasProxy()) {
            InetSocketAddress unresolved = InetSocketAddress.createUnresolved(CustomConfig.proxyHost(), CustomConfig.proxyPort());
            httpConfig.setProxySelector(ProxySelector.of(unresolved));
        }
        httpConfig.setConnectTimeout(10);
        HttpUtil.init(httpConfig);

        //  TODO 全局异常处理
        Thread.setDefaultUncaughtExceptionHandler(this::showErrorDialog);
        Thread.currentThread().setUncaughtExceptionHandler(this::showErrorDialog);

        // 启动主界面
        try {
            FXMLLoader fxmlLoader = FxmlUtil.load("main-view");
            VBox root = fxmlLoader.load();//底层面板
            Scene scene = new Scene(root);
            scene.getStylesheets().addAll(Objects.requireNonNull(getClass().getResource("/css/root.css")).toExternalForm());
            stage.setScene(scene);
            stage.setMinHeight(330);
            stage.setMinWidth(430);
            stage.setMaxWidth(Double.MAX_VALUE);
            stage.setMaxHeight(Double.MAX_VALUE);
            stage.setTitle(AppConstant.APP_TITLE + " v" + AppConstant.APP_VERSION);
            stage.show();

            MainController controller = fxmlLoader.getController();
            controller.setApplication(this);
        } catch (Throwable t) {
            showErrorDialog(Thread.currentThread(), t);
        }

        logger.info("application start over ...");
    }

    private void showErrorDialog(Thread t, Throwable e) {
        logger.error("", e);
        AlertUtil.exceptionAlert(new Exception(e)).show();
    }

    @Override
    public void stop() throws Exception {
        logger.info("application stop ...");

        // 清除翻译任务
        TranslateUtil.getInstance().clear();
        // 停止所有线程
        ThreadPoolManager.getInstance().shutdown();
        // 保存应用数据
        CustomConfig.store();
        // 清理缓存
        PBOUtil.clear();
    }
}