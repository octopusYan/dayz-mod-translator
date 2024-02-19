package cn.octopusyan.dayzmodtranslator.manager.word;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import java.io.File;

/**
 * csv单词对象
 *
 * @author octopus_yan@foxmail.com
 */
public class WordCsvItem extends WordItem {
    /**
     * 简体中文
     */
    private StringProperty chineseSimp;

    /**
     * 文件中坐标(简体中文)
     */
    private int[] positionSimp;

    public WordCsvItem() {
    }

    public WordCsvItem(File stringTable, int lines, String original, String chineses, int[] position, String chineseSimp, int[] positionSimp) {
        super(stringTable, lines, original, chineses, position);
        this.chineseSimp = new SimpleStringProperty(chineseSimp);
        this.positionSimp = positionSimp;
    }

    public StringProperty chineseSimpProperty() {
        return chineseSimp;
    }

    public String getChineseSimp() {
        return chineseSimp.get();
    }

    public void setChineseSimp(String chineseSimp) {
        this.chineseSimp.setValue(chineseSimp);
    }

    public int[] getPositionSimp() {
        return positionSimp;
    }

    public void setPositionSimp(int[] positionSimp) {
        this.positionSimp = positionSimp;
    }
}
