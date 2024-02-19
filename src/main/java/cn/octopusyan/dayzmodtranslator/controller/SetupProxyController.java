package cn.octopusyan.dayzmodtranslator.controller;

import cn.octopusyan.dayzmodtranslator.base.BaseController;
import cn.octopusyan.dayzmodtranslator.config.CustomConfig;
import cn.octopusyan.dayzmodtranslator.util.AlertUtil;
import cn.octopusyan.dayzmodtranslator.util.FxmlUtil;
import cn.octopusyan.dayzmodtranslator.manager.http.HttpUtil;
import javafx.scene.control.TextField;
import javafx.scene.layout.StackPane;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

import java.io.IOException;
import java.net.URI;

/**
 * 应用配置
 *
 * @author octopus_yan@foxmail.com
 */
public class SetupProxyController extends BaseController<StackPane> {
    public StackPane root;
    public TextField hostField;
    public TextField portField;
    public TextField testPath;
    public static final String PROXY_ERROR = "ProxyError";

    /**
     * 窗口拖拽设置
     *
     * @return 是否启用
     */
    @Override
    public boolean dragWindow() {
        return false;
    }

    /**
     * 获取根布局
     *
     * @return 根布局对象
     */
    @Override
    public StackPane getRootPanel() {
        return root;
    }

    /**
     * 获取根布局
     * <p> 搭配 <code>FxmlUtil.load</code> 使用
     *
     * @return 根布局对象
     * @see FxmlUtil#load(String)
     */
    @Override
    public String getRootFxml() {
        return "proxy-view";
    }

    /**
     * 初始化数据
     */
    @Override
    public void initData() {
        // 是否已有代理配置
        if (CustomConfig.hasProxy()) {
            hostField.textProperty().setValue(CustomConfig.proxyHost());
            portField.textProperty().setValue(String.valueOf(CustomConfig.proxyPort()));
        }

        // 默认测试地址
        testPath.textProperty().setValue("https://translate.googleapis.com");
    }

    /**
     * 视图样式
     */
    @Override
    public void initViewStyle() {

    }

    /**
     * 视图事件
     */
    @Override
    public void initViewAction() {

    }

    private String getHost() {
        String text = hostField.getText();
        if (StringUtils.isBlank(text)) {
            throw new RuntimeException(PROXY_ERROR);
        }
        try {
            URI.create(text);
        } catch (Exception e) {
            throw new RuntimeException(PROXY_ERROR);
        }

        return text;
    }

    private int getPort() {
        String text = portField.getText();
        if (StringUtils.isBlank(text)) {
            throw new RuntimeException();
        }

        boolean creatable = NumberUtils.isCreatable(text);
        if (!creatable) {
            throw new RuntimeException(PROXY_ERROR);
        }

        return Integer.parseInt(text);
    }

    private String getTestPath() {
        String text = testPath.getText();
        if (StringUtils.isBlank(text)) {
            throw new RuntimeException(PROXY_ERROR);
        }
        return (text.startsWith("http") ? "" : "http://") + text;
    }

    /**
     * 测试代理有效性
     */
    public void test() {
        HttpUtil.getInstance().clearProxy();
        try {
            String resp = HttpUtil.getInstance().proxy(getHost(), getPort())
                    .get(getTestPath(), null, null);
            AlertUtil.info("成功").show();
        } catch (IOException | InterruptedException e) {
            logger.error("代理访问失败", e);
            AlertUtil.error("失败!").show();
        }
    }

    /**
     * 保存代理配置
     */
    public void save() {
        CustomConfig.proxyHost(getHost());
        CustomConfig.proxyPort(getPort());

        CustomConfig.store();

        onDestroy();
    }

    /**
     * 取消
     */
    public void close() {
        HttpUtil.getInstance().clearProxy();

        onDestroy();
    }
}
