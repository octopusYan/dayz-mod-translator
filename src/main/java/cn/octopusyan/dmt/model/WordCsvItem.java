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
     * 开始下标(csv繁体
     */
    private Integer indexTrad;

    /**
     * 原文(获取于csv繁体位置，用于替换翻译文本
     */
    private String originalTrad;

    public WordCsvItem(File file, Integer lines, Integer index, String original, String chinese, Integer indexTrad, String originalTrad) {
        super(file, lines, index, original, chinese);
        this.indexTrad = indexTrad;
        this.originalTrad = originalTrad;
    }
}
