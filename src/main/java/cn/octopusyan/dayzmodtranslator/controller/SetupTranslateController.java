package cn.octopusyan.dayzmodtranslator.controller;

import cn.octopusyan.dayzmodtranslator.base.BaseController;
import cn.octopusyan.dayzmodtranslator.config.CustomConfig;
import cn.octopusyan.dayzmodtranslator.manager.translate.TranslateSource;
import cn.octopusyan.dayzmodtranslator.util.AlertUtil;
import javafx.collections.ObservableList;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;
import org.apache.commons.lang3.StringUtils;

/**
 * 翻译设置控制器
 *
 * @author octopus_yan@foxmail.com
 */
public class SetupTranslateController extends BaseController<StackPane> {
    public StackPane root;
    public ComboBox<TranslateSource> translateSourceCombo;
    public TextField qps;
    public VBox appidBox;
    public TextField appid;
    public VBox apikeyBox;
    public TextField apikey;

    @Override
    public boolean dragWindow() {
        return false;
    }

    @Override
    public StackPane getRootPanel() {
        return root;
    }

    @Override
    public String getRootFxml() {
        return "translate-view";
    }

    /**
     * 初始化数据
     */
    @Override
    public void initData() {
        // 翻译源
        for (TranslateSource value : TranslateSource.values()) {
            ObservableList<TranslateSource> items = translateSourceCombo.getItems();
            items.addAll(value);
        }
        translateSourceCombo.setConverter(new StringConverter<>() {
            @Override
            public String toString(TranslateSource object) {
                if (object == null) return null;

                return object.getLabel();
            }

            @Override
            public TranslateSource fromString(String string) {
                return TranslateSource.getByLabel(string);
            }
        });
        translateSourceCombo.getSelectionModel()
                .selectedItemProperty()
                .addListener((observable, oldValue, newValue) -> {
                    boolean needApiKey = newValue.needApiKey();
                    appidBox.setVisible(needApiKey);
                    apikeyBox.setVisible(needApiKey);
                    if (needApiKey) {
                        appid.textProperty().setValue(CustomConfig.translateSourceAppid(newValue));
                        apikey.textProperty().setValue(CustomConfig.translateSourceApikey(newValue));

                    }

                    qps.textProperty().setValue(String.valueOf(CustomConfig.translateSourceQps(newValue)));
                });

        // 当前翻译源
        translateSourceCombo.getSelectionModel().select(CustomConfig.translateSource());
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
        qps.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*")) {
                qps.setText(oldValue);
            }
        });
    }

    public void save() {
        TranslateSource source = translateSourceCombo.getValue();
        String apikey = this.apikey.getText();
        String appid = this.appid.getText();
        int qps = Integer.parseInt(this.qps.getText());

        CustomConfig.translateSource(source);
        if (source.needApiKey()) {
            if (StringUtils.isBlank(apikey) || StringUtils.isBlank(appid)) {
                AlertUtil.error("认证信息不能为空");
            }

            CustomConfig.translateSourceApikey(source, apikey);
            CustomConfig.translateSourceAppid(source, appid);
            CustomConfig.translateSourceQps(source, qps);
        }
        // 保存到文件
        CustomConfig.store();
        // 退出
        onDestroy();
    }
}
