package cn.octopusyan.dayzmodtranslator.manager.translate.factory;

import cn.octopusyan.dayzmodtranslator.manager.translate.TranslateSource;
import cn.octopusyan.dayzmodtranslator.manager.translate.TranslateUtil;

/**
 * 翻译器接口
 *
 * @author octopus_yan@foxmail.com
 */
public interface TranslateFactory {
    /**
     * 翻译处理
     *
     * @param source       翻译源
     * @param sourceString 原始文本
     * @return 翻译结果
     * @throws Exception 翻译出错
     */
    String translate(TranslateSource source, String sourceString) throws Exception;

    /**
     * 获取延迟翻译对象
     *
     * @param source   翻译源
     * @param index    序号
     * @param original 原始文本
     * @param listener 监听器
     * @return 延迟翻译对象
     */
    TranslateUtil.DelayWord getDelayWord(TranslateSource source, int index, String original, TranslateUtil.OnTranslateListener listener);
}
