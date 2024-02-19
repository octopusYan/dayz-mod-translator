package cn.octopusyan.dayzmodtranslator.manager.translate.processor;

import cn.octopusyan.dayzmodtranslator.config.CustomConfig;
import cn.octopusyan.dayzmodtranslator.manager.translate.ApiKey;
import cn.octopusyan.dayzmodtranslator.manager.translate.TranslateSource;
import cn.octopusyan.dayzmodtranslator.manager.translate.TranslateUtil;
import cn.octopusyan.dayzmodtranslator.manager.http.HttpUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

/**
 * 翻译处理器抽象类
 *
 * @author octopus_yan@foxmail.com
 */
public abstract class AbstractTranslateProcessor implements TranslateProcessor {
    protected final Logger logger = LoggerFactory.getLogger(this.getClass());
    protected static final HttpUtil httpUtil = HttpUtil.getInstance();
    protected final TranslateSource translateSource;
    protected ApiKey apiKey;

    public AbstractTranslateProcessor(TranslateSource translateSource) {
        this.translateSource = translateSource;
    }

    public String getSource() {
        return translateSource.getName();
    }

    public TranslateSource source() {
        return translateSource;
    }

    @Override
    public boolean needApiKey() {
        return source().needApiKey();
    }

    @Override
    public boolean configuredKey() {
        return CustomConfig.hasTranslateApiKey(source());
    }

    @Override
    public int qps() {
        return CustomConfig.translateSourceQps(source());
    }

    /**
     * 获取Api配置信息
     */
    protected ApiKey getApiKey() {
        if (!configuredKey()) {
            String message = String.format("未配置【%s】翻译源认证信息!", source().getLabel());
            logger.error(message);
            throw new RuntimeException(message);
        }

        String appid = CustomConfig.translateSourceAppid(source());
        String apikey = CustomConfig.translateSourceApikey(source());
        return new ApiKey(appid, apikey);
    }

    @Override
    public String translate(String source) throws Exception {

        if (needApiKey() && !configuredKey()) {
            String message = String.format("未配置【%s】翻译源认证信息!", source().getLabel());
            logger.error(message);
            throw new RuntimeException(message);
        }

        return customTranslate(source);
    }

    /**
     * 翻译处理
     *
     * @param source 原始文本
     * @return 翻译结果
     */
    public abstract String customTranslate(String source) throws Exception;

    /**
     * 设置延迟对象
     *
     * @param word 带翻译单词
     */
    public void setDelayTime(TranslateUtil.DelayWord word) {
        // 设置翻译源
        word.setSource(source());

        // 设置翻译延迟
        int time = word.getIndex() / qps();
        word.setTime(time, TimeUnit.SECONDS);
    }
}
