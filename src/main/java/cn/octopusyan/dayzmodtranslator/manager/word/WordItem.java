package cn.octopusyan.dayzmodtranslator.manager.word;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import java.io.File;

/**
 * 待翻译单词子项
 *
 * @author octopus_yan@foxmail.com
 */
public class WordItem {
    /**
     * 所在文件
     * <p>PS: 文本所在的文件
     */
    private File file;

    /**
     * 原始文本
     */
    private StringProperty original;

    /**
     * 汉化
     */
    private StringProperty chinese;

    /**
     * 行内下标
     */
    private int[] position;

    /**
     * 文件第几行
     */
    private int lines;

    public WordItem() {
    }

    public WordItem(File file, int lines, String original, String chinese, int[] position) {
        this.file = file;
        this.original = new SimpleStringProperty(original);
        this.chinese = new SimpleStringProperty(chinese);
        this.position = position;
        this.lines = lines;
    }

    public StringProperty originalProperty() {
        return original;
    }

    public String getOriginal() {
        return original.get();
    }

    public void setOriginal(String original) {
        this.original.setValue(original);
    }

    public StringProperty chineseProperty() {
        return chinese;
    }

    public String getChinese() {
        return chinese.get();
    }

    public void setChinese(String chinese) {
        this.chinese.setValue(chinese);
    }

    public int[] getPosition() {
        return position;
    }

    public void setPosition(int[] position) {
        this.position = position;
    }

    public int getLines() {
        return lines;
    }

    public void setLines(int lines) {
        this.lines = lines;
    }

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }
}
