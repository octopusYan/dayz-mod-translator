package cn.octopusyan.dmt.translate.processor;

import cn.octopusyan.dmt.common.manager.ConfigManager;
import cn.octopusyan.dmt.common.manager.http.HttpUtil;
import cn.octopusyan.dmt.translate.ApiKey;
import cn.octopusyan.dmt.translate.TranslateApi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 翻译处理器抽象类
 *
 * @author octopus_yan@foxmail.com
 */
public abstract class AbstractTranslateProcessor implements TranslateProcessor {
    protected final Logger logger = LoggerFactory.getLogger(this.getClass());
    protected static final HttpUtil httpUtil = HttpUtil.getInstance();
    protected final TranslateApi translateApi;

    public AbstractTranslateProcessor(TranslateApi translateApi) {
        this.translateApi = translateApi;
    }

    public String getSource() {
        return translateApi.getName();
    }

    public TranslateApi source() {
        return translateApi;
    }

    @Override
    public boolean needApiKey() {
        return source().needApiKey();
    }

    @Override
    public boolean configuredKey() {
        return ConfigManager.hasTranslateApiKey(source());
    }

    @Override
    public int qps() {
        return ConfigManager.translateQps(source());
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

        String appid = ConfigManager.translateAppid(source());
        String apikey = ConfigManager.translateApikey(source());
        return new ApiKey(appid, apikey);
    }

    @Override
    public String translate(String original) throws Exception {

        if (needApiKey() && !configuredKey()) {
            String message = String.format("未配置【%s】翻译源认证信息!", source().getLabel());
            logger.error(message);
            throw new RuntimeException(message);
        }

        return customTranslate(original);
    }

    /**
     * 翻译处理
     *
     * @param source 原始文本
     * @return 翻译结果
     */
    public abstract String customTranslate(String source) throws Exception;
}
