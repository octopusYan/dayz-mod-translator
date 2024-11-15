package cn.octopusyan.dmt.controller.component;

import cn.octopusyan.dmt.common.base.BaseController;
import cn.octopusyan.dmt.common.manager.ConfigManager;
import cn.octopusyan.dmt.common.manager.thread.ThreadPoolManager;
import cn.octopusyan.dmt.model.WordItem;
import cn.octopusyan.dmt.translate.factory.TranslateFactoryImpl;
import cn.octopusyan.dmt.view.alert.AlertUtil;
import cn.octopusyan.dmt.viewModel.WordEditViewModel;
import javafx.application.Platform;
import javafx.scene.control.Button;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TextArea;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;

/**
 * 文本编辑
 *
 * @author octopus_yan
 */
public class WordEditController extends BaseController<WordEditViewModel> {

    public VBox root;
    public TextArea original;
    public Button translate;
    public TextArea chinese;
    public ProgressIndicator progress;

    @Override
    public Pane getRootPanel() {
        return root;
    }

    @Override
    public void initData() {

    }

    @Override
    public void initViewAction() {

    }

    public void bindData(WordItem data) {
        viewModel.setData(data);
        original.textProperty().bind(viewModel.getOriginalProperty());
        chinese.textProperty().bindBidirectional(viewModel.getChineseProperty());
    }

    public void startTranslate() {
        progress.setVisible(true);
        ThreadPoolManager.getInstance().execute(() -> {
            try {
                String result = TranslateFactoryImpl.getInstance().translate(ConfigManager.translateApi(), original.getText());
                Platform.runLater(() -> chinese.setText(result));
            } catch (Exception e) {
                Platform.runLater(() -> AlertUtil.getInstance(getWindow()).exception(e).show());
            }
            Platform.runLater(() -> progress.setVisible(false));
        });
    }
}
