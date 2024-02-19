package cn.octopusyan.dayzmodtranslator.manager.translate.factory;

import cn.octopusyan.dayzmodtranslator.manager.translate.TranslateSource;
import cn.octopusyan.dayzmodtranslator.manager.translate.TranslateUtil;
import cn.octopusyan.dayzmodtranslator.manager.translate.processor.AbstractTranslateProcessor;
import cn.octopusyan.dayzmodtranslator.manager.translate.processor.BaiduTranslateProcessor;
import cn.octopusyan.dayzmodtranslator.manager.translate.processor.FreeGoogleTranslateProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * 翻译处理器
 *
 * @author octopus_yan@foxmail.com
 */
public class TranslateFactoryImpl implements TranslateFactory {
    private static final Logger logger = LoggerFactory.getLogger(TranslateFactoryImpl.class);
    private static TranslateFactoryImpl impl;
    private final Map<String, AbstractTranslateProcessor> processorMap = new HashMap<>();
    private final List<AbstractTranslateProcessor> processorList = new ArrayList<>();

    private TranslateFactoryImpl() {
    }

    public static synchronized TranslateFactoryImpl getInstance() {
        if (impl == null) {
            impl = new TranslateFactoryImpl();
            impl.initProcessor();
        }
        return impl;
    }

    private void initProcessor() {
        processorList.addAll(Arrays.asList(
                new FreeGoogleTranslateProcessor(TranslateSource.FREE_GOOGLE),
                new BaiduTranslateProcessor(TranslateSource.BAIDU)
        ));
        for (AbstractTranslateProcessor processor : processorList) {
            processorMap.put(processor.getSource(), processor);
        }
    }

    private AbstractTranslateProcessor getProcessor(TranslateSource source) {
        return processorMap.get(source.getName());
    }

    /**
     * 获取延迟翻译对象
     *
     * @param source   翻译源
     * @param index    序号
     * @param original 原始文本
     * @param listener 监听器
     * @return 延迟翻译对象
     */
    @Override
    public TranslateUtil.DelayWord getDelayWord(TranslateSource source, int index, String original, TranslateUtil.OnTranslateListener listener) {
        // 生产翻译对象
        TranslateUtil.DelayWord word = new TranslateUtil.DelayWord(index, original, listener);
        // 设置延迟
        getProcessor(source).setDelayTime(word);

        return word;
    }

    /**
     * 翻译（英->中）
     * <p> TODO 切换语种
     *
     * @param source       翻译源
     * @param sourceString 原始文本
     * @return 翻译结果
     * @throws Exception 翻译出错
     */
    @Override
    public String translate(TranslateSource source, String sourceString) throws Exception {
        return getProcessor(source).translate(sourceString);
    }
}
