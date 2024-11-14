package cn.octopusyan.dmt.viewModel;

import cn.octopusyan.dmt.common.base.BaseViewModel;
import cn.octopusyan.dmt.common.enums.ProxySetup;
import cn.octopusyan.dmt.common.manager.ConfigManager;
import cn.octopusyan.dmt.common.manager.http.HttpUtil;
import cn.octopusyan.dmt.controller.ProxyController;
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

/**
 * 设置
 *
 * @author octopus_yan
 */
public class ProxyViewModel extends BaseViewModel<ProxyViewModel, ProxyController> {
    private final StringProperty proxyHost = new SimpleStringProperty(ConfigManager.proxyHost());
    private final StringProperty proxyPort = new SimpleStringProperty(ConfigManager.proxyPort());
    private final ObjectProperty<ProxySetup> proxySetup = new SimpleObjectProperty<>(ConfigManager.proxySetup());
    private final StringProperty proxyTestUrl = new SimpleStringProperty(ConfigManager.proxyTestUrl());

    public ProxyViewModel() {
        proxySetup.addListener((_, _, newValue) -> ConfigManager.proxySetup(newValue));
        proxyTestUrl.addListener((_, _, newValue) -> ConfigManager.proxyTestUrl(newValue));
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
        var checkUrl = AlertUtil.input("URL :", proxyTestUrl.getValue())
                .title("检查代理设置")
                .header("请输入您要检查的任何URL：")
                .getInput();

        if (StringUtils.isEmpty(checkUrl)) return;

        proxyTestUrl.setValue(checkUrl);

        ProgressBuilder progress = AlertUtil.progress();
        progress.show();
        ConfigManager.checkProxy((success, msg) -> {
            Platform.runLater(progress::close);
            if (!success) {
                final var tmp = "连接问题: ";
                AlertUtil.error(STR."\{tmp}\{msg}").show();
                return;
            }

            HttpUtil.getInstance().proxy(ConfigManager.proxySetup(), ConfigManager.getProxyInfo());
            getProxyCheckTask(checkUrl).execute();
        });
    }

    private void setProxy() {
        ConfigManager.checkProxy((success, msg) -> {
            if (!success) {
                return;
            }

            HttpUtil.getInstance().proxy(ConfigManager.proxySetup(), ConfigManager.getProxyInfo());
        });
    }

    private static ProxyCheckTask getProxyCheckTask(String checkUrl) {
        var task = new ProxyCheckTask(checkUrl);
        task.onListen(new DefaultTaskListener(true) {

            @Override
            public void onSucceed() {
                AlertUtil.info("连接成功").show();
            }

            @Override
            public void onFailed(Throwable throwable) {
                super.onFailed(throwable);
                AlertUtil.exception(new Exception(throwable)).show();
            }
        });
        return task;
    }
}
