package cn.octopusyan.dmt.translate.factory;

import cn.octopusyan.dmt.model.WordItem;
import cn.octopusyan.dmt.translate.DelayWord;
import cn.octopusyan.dmt.translate.TranslateApi;
import cn.octopusyan.dmt.translate.processor.AbstractTranslateProcessor;
import cn.octopusyan.dmt.translate.processor.BaiduTranslateProcessor;
import cn.octopusyan.dmt.translate.processor.FreeBaiduTranslateProcessor;
import cn.octopusyan.dmt.translate.processor.FreeGoogleTranslateProcessor;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.concurrent.DelayQueue;
import java.util.concurrent.TimeUnit;

/**
 * 翻译处理器
 *
 * @author octopus_yan@foxmail.com
 */
@Slf4j
public class TranslateFactoryImpl implements TranslateFactory {
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
                new FreeGoogleTranslateProcessor(),
                new FreeBaiduTranslateProcessor(),
                new BaiduTranslateProcessor()
        ));
        for (AbstractTranslateProcessor processor : processorList) {
            processorMap.put(processor.getSource(), processor);
        }
    }

    private AbstractTranslateProcessor getProcessor(TranslateApi api) {
        return processorMap.get(api.getName());
    }

    @Override
    public DelayQueue<DelayWord> getDelayQueue(TranslateApi api, List<WordItem> words) {
        var queue = new DelayQueue<DelayWord>();

        // 设置翻译延迟
        AbstractTranslateProcessor processor = getProcessor(api);
        for (int i = 0; i < words.size(); i++) {
            // 翻译对象
            DelayWord delayWord = new DelayWord(words.get(i));

            // 设置翻译源
            delayWord.setApi(api);
            long time = 1000L / processor.qps();
            delayWord.setDelayTime(time * (i + 1), TimeUnit.MILLISECONDS);

            queue.add(delayWord);
        }
        return queue;
    }

    @Override
    public void resetDelayTime(TranslateApi api, int index, DelayWord delayWord) {
        AbstractTranslateProcessor processor = getProcessor(api);
        // 设置翻译源
        delayWord.setApi(api);
        long time = 1000L / processor.qps();
        delayWord.setDelayTime(time * (index + 1), TimeUnit.MILLISECONDS);
    }

    /**
     * 翻译（英->中）
     * <p> TODO 切换语种
     *
     * @param api          翻译源
     * @param sourceString 原始文本
     * @return 翻译结果
     * @throws Exception 翻译出错
     */
    @Override
    public String translate(TranslateApi api, String sourceString) throws Exception {
        return getProcessor(api).translate(sourceString);
    }
}
