package cn.octopusyan.dmt.viewModel;

import cn.octopusyan.dmt.common.base.BaseViewModel;
import cn.octopusyan.dmt.common.manager.ConfigManager;
import cn.octopusyan.dmt.controller.setup.TranslateController;
import cn.octopusyan.dmt.translate.TranslateApi;
import javafx.beans.property.*;
import lombok.Getter;

/**
 * 翻译VM
 *
 * @author octopus_yan
 */
@Getter
public class TranslateViewModel extends BaseViewModel<TranslateController> {

    private final ObjectProperty<TranslateApi> source = new SimpleObjectProperty<>(ConfigManager.translateApi()) {
        {
            addListener((_, _, newValue) -> {
                appId.setValue(ConfigManager.translateAppid(newValue));
                apiKey.setValue(ConfigManager.translateApikey(newValue));
                qps.setValue(String.valueOf(ConfigManager.translateQps(newValue)));
                needApiKey.setValue(newValue.needApiKey());
            });
        }
    };
    private final StringProperty appId = new SimpleStringProperty(ConfigManager.translateAppid(ConfigManager.translateApi()));
    private final StringProperty apiKey = new SimpleStringProperty(ConfigManager.translateApikey(ConfigManager.translateApi()));
    private final StringProperty qps = new SimpleStringProperty(String.valueOf(ConfigManager.translateQps(ConfigManager.translateApi())));
    private final BooleanProperty needApiKey = new SimpleBooleanProperty(ConfigManager.translateApi().needApiKey());
}
