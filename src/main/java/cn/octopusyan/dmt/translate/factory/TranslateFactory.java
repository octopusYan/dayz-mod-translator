package cn.octopusyan.dmt.translate.factory;


import cn.octopusyan.dmt.model.WordItem;
import cn.octopusyan.dmt.translate.DelayWord;
import cn.octopusyan.dmt.translate.TranslateApi;

import java.util.List;
import java.util.concurrent.DelayQueue;

/**
 * 翻译器接口
 *
 * @author octopus_yan@foxmail.com
 */
public interface TranslateFactory {
    /**
     * 翻译处理
     *
     * @param api          翻译源
     * @param sourceString 原始文本
     * @return 翻译结果
     * @throws Exception 翻译出错
     */
    String translate(TranslateApi api, String sourceString) throws Exception;

    /**
     * 获取延迟翻译对象
     *
     * @param api  翻译源
     * @param word 待翻译文本对象
     * @return 延迟翻译对象
     */
    DelayQueue<DelayWord> getDelayQueue(TranslateApi api, List<WordItem> word);

    /**
     * 重设延迟时间
     *
     * @param api       翻译接口
     * @param index     序列号
     * @param delayWord 延迟对象
     */
    void resetDelayTime(TranslateApi api, int index, DelayWord delayWord);
}
