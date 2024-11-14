package cn.octopusyan.dmt.view.alert.builder;

import cn.octopusyan.dmt.common.config.LabelConstants;
import javafx.beans.binding.Bindings;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.stage.Window;

/**
 * 加载弹窗
 *
 * @author octopus_yan
 */
public class ProgressBuilder extends DefaultBuilder {

    private HBox hBox;

    public ProgressBuilder(Window mOwner) {
        super(mOwner);
        content(getContent());
    }

    public void setWidth(double width) {
        hBox.setPrefWidth(width);
    }

    private Pane getContent() {
        hBox = new HBox();
        hBox.setPrefWidth(350);
        hBox.setAlignment(Pos.CENTER);
        hBox.setSpacing(10);
        hBox.setPadding(new Insets(10, 0, 10, 0));

        // 取消按钮
        Button cancel = new Button(LabelConstants.CANCEL);
        cancel.setCancelButton(true);
        cancel.setOnAction(_ -> dialog.close());

        // 进度条
        ProgressBar progressBar = new ProgressBar(-1);
        progressBar.prefWidthProperty().bind(Bindings.createDoubleBinding(
                () -> hBox.widthProperty().get() - cancel.widthProperty().get() - 40,
                hBox.widthProperty(), cancel.widthProperty()
        ));

        hBox.getChildren().add(progressBar);
        hBox.getChildren().add(cancel);
        return hBox;
    }

    public ProgressBuilder onCancel(Runnable run) {
        dialog.setOnCloseRequest(_ -> run.run());
        return this;
    }
}
