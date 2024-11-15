package cn.octopusyan.dmt.viewModel;

import cn.octopusyan.dmt.common.base.BaseViewModel;
import cn.octopusyan.dmt.controller.component.WordEditController;
import cn.octopusyan.dmt.model.WordItem;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 文本编辑
 *
 * @author octopus_yan
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class WordEditViewModel extends BaseViewModel<WordEditController> {
    private WordItem data;

    /**
     * 原文
     */
    private StringProperty originalProperty = new SimpleStringProperty();
    /**
     * 中文
     */
    private StringProperty chineseProperty = new SimpleStringProperty();

    public void setData(WordItem data) {
        if(data == null) return;
        this.data = data;

        originalProperty.bind(data.getOriginalProperty());
        chineseProperty.bindBidirectional(data.getChineseProperty());
    }
}
