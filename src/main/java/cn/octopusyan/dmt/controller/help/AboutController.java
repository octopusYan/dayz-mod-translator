package cn.octopusyan.dmt.controller.help;

import cn.octopusyan.dmt.common.base.BaseController;
import cn.octopusyan.dmt.common.config.Constants;
import cn.octopusyan.dmt.common.config.Context;
import cn.octopusyan.dmt.viewModel.AboutViewModel;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;

/**
 * 关于
 *
 * @author octopus_yan
 */
public class AboutController extends BaseController<AboutViewModel> {
    public VBox root;
    public Label title;
    public Label infoTitle;
    public Label version;

    @Override
    public Pane getRootPanel() {
        return root;
    }

    @Override
    public void initData() {
        title.setText(Constants.APP_TITLE);
        infoTitle.setText(Constants.APP_TITLE);
        version.setText(STR."版本：\{Constants.APP_VERSION}(x64)");
    }

    @Override
    public void initViewAction() {

    }

    public void openGitee() {
        Context.openUrl("https://gitee.com/octopus_yan/dayz-mod-translator/releases");
    }

    public void openGithub() {
        Context.openUrl("https://github.com/octopusYan/dayz-mod-translator/releases");
    }

    public void openForum() {
        Context.openUrl("https://www.52pojie.cn/thread-1891962-1-1.html");
    }
}
