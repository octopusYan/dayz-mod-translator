package cn.octopusyan.dmt.view;

import atlantafx.base.theme.Styles;
import cn.octopusyan.dmt.model.WordItem;
import cn.octopusyan.dmt.utils.Resources;
import javafx.scene.control.Button;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.util.Callback;
import org.kordamp.ikonli.feather.Feather;
import org.kordamp.ikonli.javafx.FontIcon;

import java.util.function.Consumer;

/**
 * 按钮
 *
 * @author octopus_yan
 */
public class EditButtonTableCell extends TableCell<WordItem, WordItem> {

    public static Callback<TableColumn<WordItem, WordItem>, TableCell<WordItem, WordItem>> forTableColumn(Consumer<WordItem> edit, Consumer<WordItem> translate) {
        return _ -> new EditButtonTableCell("", edit, translate);
    }

    private final Button edit;
    private final Button translate;

    private static final ImageView translateIcon = new ImageView(new Image(Resources.getResourceAsStream("images/icon/translate.png")));

    static {
        translateIcon.setFitHeight(20);
        translateIcon.setFitWidth(20);
    }

    public EditButtonTableCell(String text, Consumer<WordItem> edit, Consumer<WordItem> translate) {
        // 编辑
        this.edit = new Button(text);
        this.edit.getStyleClass().addAll(Styles.BUTTON_ICON, Styles.FLAT);
        this.edit.setOnMouseClicked(_ -> {
            WordItem data = getTableView().getItems().get(getIndex());
            edit.accept(data);
        });
        this.edit.setGraphic(new FontIcon(Feather.EDIT));

        // 翻译
        ImageView translateIcon = new ImageView(new Image(Resources.getResourceAsStream("images/icon/translate.png")));
        translateIcon.setFitHeight(20);
        translateIcon.setFitWidth(20);
        this.translate = new Button("", translateIcon);
        this.translate.getStyleClass().addAll(Styles.BUTTON_ICON, Styles.FLAT);
        this.translate.setOnMouseClicked(_ -> {
            WordItem data = getTableView().getItems().get(getIndex());
            translate.accept(data);
        });
    }

    @Override
    protected void updateItem(WordItem item, boolean empty) {
        super.updateItem(item, empty);
        if (empty) {
            setGraphic(null);
        } else {
            // 添加多个操作按钮
            setGraphic(new HBox(edit, translate));
        }
    }
}
