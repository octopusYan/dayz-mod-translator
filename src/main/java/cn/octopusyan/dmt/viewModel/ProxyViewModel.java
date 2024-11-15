package cn.octopusyan.dmt.viewModel;

import cn.octopusyan.dmt.common.base.BaseViewModel;
import cn.octopusyan.dmt.common.enums.ProxySetup;
import cn.octopusyan.dmt.common.manager.ConfigManager;
import cn.octopusyan.dmt.common.manager.http.HttpUtil;
import cn.octopusyan.dmt.controller.setup.ProxyController;
import cn.octopusyan.dmt.task.ProxyCheckTask;
import cn.octopusyan.dmt.task.listener.DefaultTaskListener;
import cn.octopusyan.dmt.view.alert.AlertUtil;
import cn.octopusyan.dmt.view.alert.builder.ProgressBuilder;
import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import org.apache.commons.lang3.StringUtils;

import java.net.URI;

/**
 * 设置
 *
 * @author octopus_yan
 */
public class ProxyViewModel extends BaseViewModel<ProxyController> {
    private final StringProperty proxyHost = new SimpleStringProperty(ConfigManager.proxyHost());
    private final StringProperty proxyPort = new SimpleStringProperty(ConfigManager.proxyPort());
    private final ObjectProperty<ProxySetup> proxySetup = new SimpleObjectProperty<>(ConfigManager.proxySetup());
    private final AlertUtil alertUtil = AlertUtil.getInstance();

    public ProxyViewModel() {

        proxySetup.addListener((_, _, newValue) -> ConfigManager.proxySetup(newValue));
        proxyHost.addListener((_, _, newValue) -> {
            ConfigManager.proxyHost(newValue);
            setProxy();
        });
        proxyPort.addListener((_, _, newValue) -> {
            ConfigManager.proxyPort(newValue);
            setProxy();
        });
    }

    public ObjectProperty<ProxySetup> proxySetupProperty() {
        return proxySetup;
    }

    public StringProperty proxyHostProperty() {
        return proxyHost;
    }

    public StringProperty proxyPortProperty() {
        return proxyPort;
    }

    public void proxyTest() {
        var checkUrl = alertUtil.input("URL :", ConfigManager.proxyTestUrl())
                .title("检查代理设置")
                .header("请输入您要检查的任何URL：")
                .getInput();

        if (StringUtils.isEmpty(checkUrl)) return;

        // 检查URL格式
        if (!checkUrl(checkUrl)) return;

        ConfigManager.proxyTestUrl(checkUrl);

        ProgressBuilder progress = alertUtil.progress();
        progress.show();
        ConfigManager.checkProxy((success, msg) -> {
            Platform.runLater(progress::close);
            if (!success) {
                final var tmp = "连接问题: ";
                alertUtil.error(STR."\{tmp}\{msg}").show();
                return;
            }
            HttpUtil.getInstance().proxy(ConfigManager.proxySetup(), ConfigManager.getProxyInfo());
            // 代理检查任务
            getProxyCheckTask(checkUrl).execute();
        });
    }

    private boolean checkUrl(String checkUrl) {
        try {
            //noinspection ResultOfMethodCallIgnored
            URI.create(checkUrl);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private void setProxy() {
        ConfigManager.checkProxy((success, msg) -> {
            if (!success) {
                return;
            }

            HttpUtil.getInstance().proxy(ConfigManager.proxySetup(), ConfigManager.getProxyInfo());
        });
    }

    private ProxyCheckTask getProxyCheckTask(String checkUrl) {
        var task = new ProxyCheckTask(checkUrl);
        task.onListen(new DefaultTaskListener(true) {

            @Override
            public void onSucceed() {
                alertUtil.info("连接成功").show();
            }

            @Override
            public void onFailed(Throwable throwable) {
                super.onFailed(throwable);
                alertUtil.exception(new Exception(throwable)).show();
            }
        });
        return task;
    }
}
