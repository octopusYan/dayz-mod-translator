package cn.octopusyan.dayzmodtranslator.util;

import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

/**
 * 加载等待弹窗
 *
 * @author octopus_yan@foxmail.com
 */
public class Loading {
    protected Stage stage;
    protected StackPane root;
    protected Label messageLb;
    protected ImageView loadingView = new ImageView(new Image("https://blog-static.cnblogs.com/files/miaoqx/loading.gif"));

    public Loading(Stage owner) {

        messageLb = new Label("请耐心等待...");
        messageLb.setFont(Font.font(20));

        root = new StackPane();
        root.setMouseTransparent(true);
        root.setPrefSize(owner.getWidth(), owner.getHeight());
        root.setBackground(new Background(new BackgroundFill(Color.rgb(0, 0, 0, 0.3), null, null)));
        root.getChildren().addAll(loadingView, messageLb);

        Scene scene = new Scene(root);
        scene.setFill(Color.TRANSPARENT);

        stage = new Stage();
        stage.setX(owner.getX());
        stage.setY(owner.getY());
        stage.setScene(scene);
        stage.setResizable(false);
        stage.initOwner(owner);
        stage.initStyle(StageStyle.TRANSPARENT);
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.getIcons().addAll(owner.getIcons());
        stage.setX(owner.getX());
        stage.setY(owner.getY());
        stage.setHeight(owner.getHeight());
        stage.setWidth(owner.getWidth());
    }

    // 更改信息
    public Loading showMessage(String message) {
        Platform.runLater(() -> messageLb.setText(message));
        return this;
    }

    // 更改信息
    public Loading image(Image image) {
        Platform.runLater(() -> loadingView.imageProperty().set(image));
        return this;
    }

    // 显示
    public void show() {
        Platform.runLater(() -> stage.show());
    }

    // 关闭
    public void closeStage() {
        Platform.runLater(() -> stage.close());
    }

    // 是否正在展示
    public boolean showing() {
        return stage.isShowing();
    }
}
