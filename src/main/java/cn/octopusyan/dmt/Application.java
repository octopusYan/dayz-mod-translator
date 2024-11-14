package cn.octopusyan.dmt;

import cn.octopusyan.dmt.common.config.Constants;
import cn.octopusyan.dmt.common.config.Context;
import cn.octopusyan.dmt.common.manager.ConfigManager;
import cn.octopusyan.dmt.common.manager.http.CookieManager;
import cn.octopusyan.dmt.common.manager.http.HttpConfig;
import cn.octopusyan.dmt.common.manager.http.HttpUtil;
import cn.octopusyan.dmt.common.manager.thread.ThreadPoolManager;
import cn.octopusyan.dmt.common.util.ProcessesUtil;
import cn.octopusyan.dmt.utils.PBOUtil;
import cn.octopusyan.dmt.view.alert.AlertUtil;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.stage.Stage;
import lombok.Getter;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ProxySelector;
import java.net.http.HttpClient;
import java.util.Objects;

public class Application extends javafx.application.Application {
    private static final Logger logger = LoggerFactory.getLogger(Application.class);
    @Getter
    private static Stage primaryStage;

    @Override
    public void init() {
        logger.info("application init ...");

        // 初始化客户端配置
        ConfigManager.load();

        // 初始化 PBO工具
        PBOUtil.init();

        // http请求工具初始化
        HttpConfig httpConfig = new HttpConfig();
        httpConfig.setCookieHandler(CookieManager.get());
        httpConfig.setExecutor(ThreadPoolManager.getInstance("http-pool"));
        // 加载代理设置
        switch (ConfigManager.proxySetup()) {
            case NO_PROXY -> httpConfig.setProxySelector(HttpClient.Builder.NO_PROXY);
            case SYSTEM -> httpConfig.setProxySelector(ProxySelector.getDefault());
            case MANUAL -> {
                if (ConfigManager.hasProxy()) {
                    InetSocketAddress unresolved = InetSocketAddress.createUnresolved(
                            Objects.requireNonNull(ConfigManager.proxyHost()),
                            ConfigManager.getProxyPort()
                    );
                    httpConfig.setProxySelector(ProxySelector.of(unresolved));
                }
            }
        }
        httpConfig.setConnectTimeout(3000);
        HttpUtil.init(httpConfig);
    }

    @Override
    public void start(Stage primaryStage) throws IOException {

        logger.info("application start ...");

        Application.primaryStage = primaryStage;

        Context.setApplication(this);

        // 初始化弹窗工具
        AlertUtil.initOwner(primaryStage);

        // 全局异常处理
        Thread.setDefaultUncaughtExceptionHandler(this::showErrorDialog);
        Thread.currentThread().setUncaughtExceptionHandler(this::showErrorDialog);

        // 主题样式
        Application.setUserAgentStylesheet(ConfigManager.theme().getUserAgentStylesheet());

        // 启动主界面
        primaryStage.setTitle(String.format("%s %s", Constants.APP_TITLE, Constants.APP_VERSION));
        Scene scene = Context.initScene();
        primaryStage.setScene(scene);
        primaryStage.show();


    }

    private void showErrorDialog(Thread t, Throwable e) {
        logger.error("未知异常", e);
        Platform.runLater(() -> AlertUtil.exception(new Exception(e)).show());
    }

    @Override
    public void stop() {
        logger.info("application stop ...");
        // 关闭所有命令
        ProcessesUtil.destroyAll();
        // 保存应用数据
        ConfigManager.save();
        // 停止所有线程
        ThreadPoolManager.shutdownAll();
        // 删除缓存
        FileUtils.deleteQuietly(new File(Constants.TMP_DIR_PATH));
        FileUtils.deleteQuietly(new File(Constants.BAK_DIR_PATH));
        // 关闭主界面
        Platform.exit();
        System.exit(0);
    }
}