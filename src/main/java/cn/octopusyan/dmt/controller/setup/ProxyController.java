package cn.octopusyan.dmt.controller.setup;

import cn.octopusyan.dmt.common.base.BaseController;
import cn.octopusyan.dmt.common.enums.ProxySetup;
import cn.octopusyan.dmt.common.manager.ConfigManager;
import cn.octopusyan.dmt.viewModel.ProxyViewModel;
import javafx.beans.binding.Bindings;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;

/**
 * 设置
 *
 * @author octopus_yan
 */
public class ProxyController extends BaseController<ProxyViewModel> {
    public VBox root;

    public RadioButton noneProxy;
    public RadioButton systemProxy;
    public RadioButton manualProxy;
    public ToggleGroup proxyGroup = new ToggleGroup();

    public GridPane manualProxyView;
    public TextField proxyHost;
    public TextField proxyPort;

    @Override
    public Pane getRootPanel() {
        return root;
    }

    @Override
    public void initData() {
        noneProxy.setUserData(ProxySetup.NO_PROXY);
        systemProxy.setUserData(ProxySetup.SYSTEM);
        manualProxy.setUserData(ProxySetup.MANUAL);

        noneProxy.setToggleGroup(proxyGroup);
        systemProxy.setToggleGroup(proxyGroup);
        manualProxy.setToggleGroup(proxyGroup);

        manualProxyView.disableProperty().bind(
                Bindings.createBooleanBinding(() -> !manualProxy.selectedProperty().get(), manualProxy.selectedProperty())
        );
    }

    @Override
    public void initViewAction() {
        proxyGroup.selectedToggleProperty().addListener((_, _, value) -> {
            viewModel.proxySetupProperty().set((ProxySetup) value.getUserData());
        });
        proxyGroup.selectToggle(switch (ConfigManager.proxySetup()) {
            case ProxySetup.SYSTEM -> systemProxy;
            case ProxySetup.MANUAL -> manualProxy;
            default -> noneProxy;
        });

        proxyHost.textProperty().bindBidirectional(viewModel.proxyHostProperty());
        proxyPort.textProperty().bindBidirectional(viewModel.proxyPortProperty());
    }

    public void proxyTest() {
        viewModel.proxyTest();
    }
}
