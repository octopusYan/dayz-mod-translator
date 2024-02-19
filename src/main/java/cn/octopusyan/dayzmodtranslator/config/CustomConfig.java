package cn.octopusyan.dayzmodtranslator.config;

import cn.octopusyan.dayzmodtranslator.manager.translate.TranslateSource;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

/**
 * 客户端设置
 *
 * @author octopus_yan@foxmail.com
 */
public class CustomConfig {
    private static final Logger logger = LoggerFactory.getLogger(CustomConfig.class);
    private static final Properties properties = new Properties();
    public static final String PROXY_HOST_KEY = "proxy.host";
    public static final String PROXY_PORT_KEY = "proxy.port";
    public static final String TRANSLATE_SOURCE_KEY = "translate.source";
    public static final String TRANSLATE_SOURCE_APPID_KEY = "translate.{}.appid";
    public static final String TRANSLATE_SOURCE_APIKEY_KEY = "translate.{}.apikey";
    public static final String TRANSLATE_SOURCE_QPS_KEY = "translate.{}.qps";

    public static void init() {
        File customConfigFile = new File(AppConstant.CUSTOM_CONFIG_PATH);
        try {
            if (!customConfigFile.exists()) {
                // 初始配置
                properties.put(TRANSLATE_SOURCE_KEY, TranslateSource.FREE_GOOGLE.getName());
                // 保存配置文件
                store();
            } else {
                properties.load(new FileInputStream(customConfigFile));
            }
        } catch (IOException ignore) {
            logger.error("读取配置文件失败");
        }
    }

    /**
     * 是否配置代理
     */
    public static boolean hasProxy() {
        String host = proxyHost();
        Integer port = proxyPort();

        return StringUtils.isNoneBlank(host) && null != port;
    }

    /**
     * 代理地址
     */
    public static String proxyHost() {
        return properties.getProperty(PROXY_HOST_KEY);
    }

    /**
     * 代理地址
     */
    public static void proxyHost(String host) {
        properties.setProperty(PROXY_HOST_KEY, host);
    }

    /**
     * 代理端口
     */
    public static Integer proxyPort() {
        try {
            return Integer.parseInt(properties.getProperty(PROXY_PORT_KEY));
        } catch (Exception ignored) {
        }
        return null;
    }

    /**
     * 代理端口
     */
    public static void proxyPort(int port) {
        properties.setProperty(PROXY_PORT_KEY, String.valueOf(port));
    }

    /**
     * 翻译源
     */
    public static TranslateSource translateSource() {
        String name = properties.getProperty(TRANSLATE_SOURCE_KEY, TranslateSource.FREE_GOOGLE.getName());
        return TranslateSource.get(name);
    }

    /**
     * 翻译源
     */
    public static void translateSource(TranslateSource source) {
        properties.setProperty(TRANSLATE_SOURCE_KEY, source.getName());
    }

    /**
     * 是否配置接口认证
     *
     * @param source 翻译源
     */
    public static boolean hasTranslateApiKey(TranslateSource source) {
        return StringUtils.isNoneBlank(translateSourceAppid(source))
                && StringUtils.isNoneBlank(translateSourceApikey(source));
    }

    /**
     * 设置翻译源appid
     *
     * @param source 翻译源
     * @param appid  appid
     */
    public static void translateSourceAppid(TranslateSource source, String appid) {
        properties.setProperty(getTranslateSourceAppidKey(source), appid);
    }

    /**
     * 获取翻译源appid
     *
     * @param source 翻译源
     * @return appid
     */
    public static String translateSourceAppid(TranslateSource source) {
        return properties.getProperty(getTranslateSourceAppidKey(source));
    }

    public static void translateSourceApikey(TranslateSource source, String apikey) {
        properties.setProperty(getTranslateSourceApikeyKey(source), apikey);
    }

    public static String translateSourceApikey(TranslateSource source) {
        return properties.getProperty(getTranslateSourceApikeyKey(source));
    }

    public static Integer translateSourceQps(TranslateSource source) {
        String qpsStr = properties.getProperty(getTranslateSourceQpsKey(source));
        return qpsStr == null ? source.getDefaultQps() : Integer.parseInt(qpsStr);
    }

    public static void translateSourceQps(TranslateSource source, int qps) {
        properties.setProperty(getTranslateSourceQpsKey(source), String.valueOf(qps));
    }


    /**
     * 保存配置
     */
    public static void store() {
        // 生成配置文件
        try {
            properties.store(new PrintStream(AppConstant.CUSTOM_CONFIG_PATH), String.valueOf(StandardCharsets.UTF_8));
        } catch (IOException e) {
            logger.error("保存客户端配置失败", e);
        }
    }

    private static String getTranslateSourceAppidKey(TranslateSource source) {
        return StringUtils.replace(TRANSLATE_SOURCE_APPID_KEY, "{}", source.getName());
    }

    private static String getTranslateSourceApikeyKey(TranslateSource source) {
        return StringUtils.replace(TRANSLATE_SOURCE_APIKEY_KEY, "{}", source.getName());
    }

    private static String getTranslateSourceQpsKey(TranslateSource source) {
        return StringUtils.replace(TRANSLATE_SOURCE_QPS_KEY, "{}", source.getName());
    }
}
