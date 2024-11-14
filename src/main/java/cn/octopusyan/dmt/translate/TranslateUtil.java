package cn.octopusyan.dmt.translate;

import cn.octopusyan.dmt.common.manager.ConfigManager;
import cn.octopusyan.dmt.model.WordItem;
import cn.octopusyan.dmt.translate.factory.TranslateFactory;
import cn.octopusyan.dmt.translate.factory.TranslateFactoryImpl;

import java.util.List;
import java.util.concurrent.DelayQueue;

/**
 * 翻译
 *
 * @author octopus_yan
 */
public class TranslateUtil {
    private static final TranslateFactory factory = TranslateFactoryImpl.getInstance();

    /**
     * 翻译（英->中）
     * <p> TODO 切换语种
     *
     * @param api          翻译源
     * @param sourceString 原始文本
     * @return 翻译结果
     * @throws Exception 翻译出错
     */
    public static String translate(TranslateApi api, String sourceString) throws Exception {
        return factory.translate(api, sourceString);
    }

    public static String translate(String sourceString) throws Exception {
        return factory.translate(ConfigManager.translateApi(), sourceString);
    }

    /**
     * 获取延迟翻译对象
     *
     * @param words 待翻译文本列表
     * @return 延迟对象
     */
    public static DelayQueue<DelayWord> getDelayQueue(List<WordItem> words) {
        return getDelayQueue(ConfigManager.translateApi(), words);
    }

    /**
     * 获取延迟翻译对象
     *
     * @param source 翻译接口
     * @param words  待翻译文本列表
     * @return 延迟对象
     */
    public static DelayQueue<DelayWord> getDelayQueue(TranslateApi source, List<WordItem> words) {
        return factory.getDelayQueue(source, words);
    }

    /**
     * 重设延迟时间
     *
     * @param index     序列号
     * @param delayWord 延迟对象
     */
    public static void resetDelayTime(int index, DelayWord delayWord) {
        factory.resetDelayTime(ConfigManager.translateApi(), index, delayWord);
    }
}
