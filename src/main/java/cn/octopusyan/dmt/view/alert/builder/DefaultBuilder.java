package cn.octopusyan.dmt.view.alert.builder;

import cn.octopusyan.dmt.common.base.BaseBuilder;
import cn.octopusyan.dmt.common.util.ViewUtil;
import javafx.scene.Node;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.scene.paint.Color;
import javafx.stage.StageStyle;
import javafx.stage.Window;

/**
 * 默认弹窗
 *
 * @author octopus_yan
 */
public class DefaultBuilder extends BaseBuilder<DefaultBuilder, Dialog<?>> {

    public DefaultBuilder(Window mOwner) {
        this(mOwner, true);
    }

    public DefaultBuilder(Window mOwner, boolean transparent) {
        super(new Dialog<>(), mOwner);

        header(null);

        DialogPane dialogPane = dialog.getDialogPane();
        if (transparent) {
            dialogPane.getScene().setFill(Color.TRANSPARENT);
            ViewUtil.bindDragged(dialogPane);
            ViewUtil.bindShadow(dialogPane);
            ViewUtil.getStage(dialogPane).initStyle(StageStyle.TRANSPARENT);
        }

        dialogPane.getButtonTypes().add(new ButtonType("取消", ButtonType.CANCEL.getButtonData()));

        for (Node child : dialogPane.getChildren()) {
            if (child instanceof ButtonBar) {
                dialogPane.getChildren().remove(child);
                break;
            }
        }
    }

    public DefaultBuilder content(Node content) {
        dialog.getDialogPane().setContent(content);
        return this;
    }
}
