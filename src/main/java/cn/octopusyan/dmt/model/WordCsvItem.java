package cn.octopusyan.dmt.model;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.File;

/**
 * csv
 *
 * @author octopus_yan
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class WordCsvItem extends WordItem {

    /**
     * 是否规整（有些翻译列数不完整，无法正常分割）
     */
    private boolean regular;

    /**
     * csv中Language列文本
     * <p>
     * 当{@code regular}为{@code false}时，用于获取于csv原文内容，用于拼接格式化文本
     */
    private String header;

    /**
     * 原文(获取于csv繁体位置，用于替换翻译文本
     */
    private String originalTrad;

    /**
     * csv(规整)文本对象
     *
     * @param file         文件
     * @param lines        行数
     * @param original     原文
     * @param chinese      中文位置对应的文本
     * @param originalTrad 繁体中文位置对应的文本
     */
    public WordCsvItem(File file, Integer lines, String original, String chinese, String originalTrad) {
        super(file, lines, 0, original, chinese);
        this.regular = true;
        this.originalTrad = originalTrad;
    }

    /**
     * csv(不规整)文本对象
     * <p>
     *
     * @param file     文件
     * @param lines    行数
     * @param header   Language列对应名称，用于拼接格式化文本
     * @param original 原文
     */
    public WordCsvItem(File file, Integer lines, String header, String original) {
        super(file, lines, null, original, "");
        this.regular = false;
        this.header = header;
    }
}
