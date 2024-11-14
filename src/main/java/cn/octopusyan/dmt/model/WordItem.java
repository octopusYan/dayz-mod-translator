package cn.octopusyan.dmt.model;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import lombok.Data;

import java.io.File;

/**
 * 翻译文本
 *
 * @author octopus_yan
 */
@Data
public class WordItem {
    /**
     * 所在文件
     */
    private File file;
    /**
     * 行数
     */
    private Integer lines;
    /**
     * 开始下标
     */
    private Integer index;
    /**
     * 原文
     */
    private StringProperty originalProperty = new SimpleStringProperty();
    /**
     * 中文
     */
    private StringProperty chineseProperty = new SimpleStringProperty();

    public WordItem(File file, Integer lines, Integer index, String original, String chinese) {
        this.file = file;
        this.lines = lines;
        this.index = index;
        this.originalProperty.set(original);
        this.chineseProperty.set(chinese);
    }

    public String getChinese() {
        return chineseProperty.get();
    }

    public String getOriginal() {
        return originalProperty.get();
    }
}
