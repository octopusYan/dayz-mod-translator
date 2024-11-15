package cn.octopusyan.dmt.view.alert;

import cn.octopusyan.dmt.Application;
import cn.octopusyan.dmt.view.alert.builder.*;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.stage.Stage;
import javafx.stage.Window;

/**
 * 弹窗工具
 *
 * @author octopus_yan@foxmail.com
 */
public class AlertUtil {
    private final Window mOwner;
    private static volatile AlertUtil alertUtil;

    private AlertUtil(Window mOwner) {
        this.mOwner = mOwner;
    }

    public static synchronized AlertUtil getInstance() {
        if (alertUtil == null) {
            alertUtil = new AlertUtil(Application.getPrimaryStage());
        }
        return alertUtil;
    }

    public static AlertUtil getInstance(Stage stage) {
        return new AlertUtil(stage);
    }

    public DefaultBuilder builder() {
        return new DefaultBuilder(mOwner, true);
    }

    public DefaultBuilder builder(boolean transparent) {
        return new DefaultBuilder(mOwner, transparent);
    }

    public AlertBuilder info(String content) {
        return info().content(content).header(null);
    }

    public AlertBuilder info() {
        return alert(Alert.AlertType.INFORMATION);
    }

    public AlertBuilder error(String message) {
        return alert(Alert.AlertType.ERROR).header(null).content(message);
    }

    public AlertBuilder warning() {
        return alert(Alert.AlertType.WARNING);
    }

    public AlertBuilder exception(Exception ex) {
        return alert(Alert.AlertType.ERROR).exception(ex);
    }

    /**
     * 确认对话框
     */
    public AlertBuilder confirm() {
        return alert(Alert.AlertType.CONFIRMATION);
    }

    /**
     * 自定义确认对话框 <p>
     *
     * @param buttons <code>"Cancel"</code> OR <code>"取消"</code> 为取消按钮
     */
    public AlertBuilder confirm(String... buttons) {
        return confirm().buttons(buttons);
    }

    public AlertBuilder confirm(ButtonType... buttons) {
        return confirm().buttons(buttons);
    }

    public AlertBuilder alert(Alert.AlertType type) {
        return new AlertBuilder(mOwner, type);
    }

    public TextInputBuilder input(String content) {
        return new TextInputBuilder(mOwner);
    }

    public TextInputBuilder input(String content, String defaultResult) {
        return new TextInputBuilder(mOwner, defaultResult).content(content);
    }

    @SafeVarargs
    public final <T> ChoiceBuilder<T> choices(String hintText, T... choices) {
        return new ChoiceBuilder<>(mOwner, choices).content(hintText);
    }

    public ProgressBuilder progress() {
        return new ProgressBuilder(mOwner);
    }

    public interface OnChoseListener {
        void confirm();

        default void cancelOrClose(ButtonType buttonType) {
        }
    }

    public interface OnClickListener {
        void onClicked(String result);
    }
}
