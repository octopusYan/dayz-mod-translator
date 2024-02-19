package cn.octopusyan.dayzmodtranslator.manager.translate.processor;

/**
 * 翻译处理器
 *
 * @author octopus_yan@foxmail.com
 */
public interface TranslateProcessor {
    /**
     * 翻译源 api接口地址
     */
    String url();

    /**
     * 是否需要配置API认证
     */
    boolean needApiKey();

    /**
     * 已配置API认证
     */
    boolean configuredKey();

    /**
     * qps 每秒访问的数量限制
     */
    int qps();

    /**
     * 翻译
     *
     * @param source 原始文本
     * @return 翻译结果
     * @throws Exception 翻译出错
     */
    String translate(String source) throws Exception;
}
