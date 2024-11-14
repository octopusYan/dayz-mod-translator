package cn.octopusyan.dmt.controller;

import cn.octopusyan.dmt.common.base.BaseController;
import cn.octopusyan.dmt.common.manager.ConfigManager;
import cn.octopusyan.dmt.translate.TranslateApi;
import cn.octopusyan.dmt.view.alert.AlertUtil;
import cn.octopusyan.dmt.viewModel.TranslateViewModel;
import javafx.collections.ObservableList;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;
import org.apache.commons.lang3.StringUtils;

/**
 * 翻译
 *
 * @author octopus_yan
 */
public class TranslateController extends BaseController<TranslateViewModel> {
    public VBox root;
    public ComboBox<TranslateApi> translateSourceCombo;
    public TextField qps;
    public VBox appidBox;
    public TextField appid;
    public VBox apikeyBox;
    public TextField apikey;

    @Override
    public Pane getRootPanel() {
        return root;
    }

    @Override
    public void initData() {

        // 翻译源
        for (TranslateApi value : TranslateApi.values()) {
            ObservableList<TranslateApi> items = translateSourceCombo.getItems();
            items.addAll(value);
        }

        translateSourceCombo.setConverter(new StringConverter<>() {
            @Override
            public String toString(TranslateApi object) {
                if (object == null) return null;

                return object.getLabel();
            }

            @Override
            public TranslateApi fromString(String string) {
                return TranslateApi.getByLabel(string);
            }
        });

        // 当前翻译源
        translateSourceCombo.getSelectionModel().select(ConfigManager.translateApi());
        viewModel.getSource().bind(translateSourceCombo.getSelectionModel().selectedItemProperty());

        qps.textProperty().bindBidirectional(viewModel.getQps());
        appid.textProperty().bindBidirectional(viewModel.getAppId());
        apikey.textProperty().bindBidirectional(viewModel.getApiKey());

        appidBox.visibleProperty().bind(viewModel.getNeedApiKey());
        apikeyBox.visibleProperty().bind(viewModel.getNeedApiKey());
    }

    @Override
    public void initViewAction() {
    }

    public void save() {
        TranslateApi source = translateSourceCombo.getValue();
        String apikey = this.apikey.getText();
        String appid = this.appid.getText();
        int qps = Integer.parseInt(this.qps.getText());

        ConfigManager.translateApi(source);
        ConfigManager.translateQps(source, qps);
        if (source.needApiKey()) {
            if (StringUtils.isBlank(apikey) || StringUtils.isBlank(appid)) {
                AlertUtil.error("认证信息不能为空");
            }

            ConfigManager.translateApikey(source, apikey);
            ConfigManager.translateAppid(source, appid);
        }

        onDestroy();
    }
}
