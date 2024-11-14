package cn.octopusyan.dmt.common.manager;

import atlantafx.base.theme.*;
import cn.octopusyan.dmt.Application;
import cn.octopusyan.dmt.common.config.Constants;
import cn.octopusyan.dmt.common.enums.ProxySetup;
import cn.octopusyan.dmt.common.manager.http.HttpUtil;
import cn.octopusyan.dmt.common.manager.thread.ThreadPoolManager;
import cn.octopusyan.dmt.model.ConfigModel;
import cn.octopusyan.dmt.model.ProxyInfo;
import cn.octopusyan.dmt.model.Translate;
import cn.octopusyan.dmt.model.UpgradeConfig;
import cn.octopusyan.dmt.translate.TranslateApi;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import javafx.application.Platform;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 客户端设置
 *
 * @author octopus_yan@foxmail.com
 */
public class ConfigManager {
    private static final Logger logger = LoggerFactory.getLogger(ConfigManager.class);
    public static ObjectMapper objectMapper = new ObjectMapper(new YAMLFactory());

    public static final UpgradeConfig upgradeConfig = new UpgradeConfig();
    public static final String DEFAULT_THEME = new PrimerLight().getName();
    public static List<Theme> THEME_LIST = List.of(
            new PrimerLight(), new PrimerDark(),
            new NordLight(), new NordDark(),
            new CupertinoLight(), new CupertinoDark(),
            new Dracula()
    );
    public static Map<String, Theme> THEME_MAP = THEME_LIST.stream()
            .collect(Collectors.toMap(Theme::getName, Function.identity()));

    private static ConfigModel configModel;

    static {
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
    }

    public static void load() {
        configModel = loadConfig(Constants.CONFIG_FILE_PATH, ConfigModel.class);
        if (configModel == null)
            configModel = new ConfigModel();
    }

    public static <T> T loadConfig(String path, Class<T> clazz) {
        File src = new File(path);
        try {
            if (!src.exists()) {
                checkFile(src, clazz);
            }
            return objectMapper.readValue(src, clazz);
        } catch (Exception e) {
            logger.error(String.format("load %s error", clazz.getSimpleName()), e);
        }
        return null;
    }

    private static <T> void checkFile(File src, Class<T> clazz) throws Exception {
        if (!src.exists()) {
            File parentDir = FileUtils.createParentDirectories(src);
            if (!parentDir.exists())
                logger.error("{} 创建失败", src.getAbsolutePath());
        }
        objectMapper.writeValue(src, clazz.getDeclaredConstructor().newInstance());
    }

    public static void save() {
        try {
            objectMapper.writeValue(new File(Constants.CONFIG_FILE_PATH), configModel);
        } catch (IOException e) {
            logger.error("save config error", e);
        }
    }

// --------------------------------{ 主题 }------------------------------------------

    public static String themeName() {
        return configModel.getTheme();
    }

    public static Theme theme() {
        return THEME_MAP.get(themeName());
    }

    public static void theme(Theme theme) {
        Application.setUserAgentStylesheet(theme.getUserAgentStylesheet());
        configModel.setTheme(theme.getName());
    }

// --------------------------------{ 翻译接口配置 }------------------------------------------

    public static TranslateApi translateApi() {
        return TranslateApi.get(configModel.getTranslate().getUse());
    }

    public static void translateApi(TranslateApi api) {
        configModel.getTranslate().setUse(api.getName());
    }

    public static Translate.Config getTranslateConfig(TranslateApi api) {
        return Optional.of(configModel.getTranslate().getConfig().get(api.getName()))
                .orElse(api.translate());
    }

    public static boolean hasTranslateApiKey(TranslateApi api) {
        return StringUtils.isNoneEmpty(getTranslateConfig(api).getAppId());
    }

    public static void translateAppid(TranslateApi api, String appId) {
        getTranslateConfig(api).setAppId(appId);
    }

    public static String translateAppid(TranslateApi api) {
        return getTranslateConfig(api).getAppId();
    }

    public static void translateApikey(TranslateApi api, String secretKey) {
        getTranslateConfig(api).setSecretKey(secretKey);
    }

    public static String translateApikey(TranslateApi api) {
        return getTranslateConfig(api).getSecretKey();
    }

    public static void translateQps(TranslateApi api, int qps) {
        getTranslateConfig(api).setQps(qps);
    }

    public static int translateQps(TranslateApi api) {
        return getTranslateConfig(api).getQps();
    }

// --------------------------------{ 网络代理 }------------------------------------------

    public static ProxySetup proxySetup() {
        return ProxySetup.valueOf(StringUtils.upperCase(getProxyInfo().getSetup()));
    }

    public static void proxyTestUrl(String url) {
        getProxyInfo().setTestUrl(url);
    }

    public static String proxyTestUrl() {
        return getProxyInfo().getTestUrl();
    }

    public static void proxySetup(ProxySetup setup) {
        getProxyInfo().setSetup(setup.getCode());

        switch (setup) {
            case NO_PROXY -> HttpUtil.getInstance().clearProxy();
            case SYSTEM, MANUAL -> {
                if (ProxySetup.MANUAL.equals(setup) && !hasProxy())
                    return;
                HttpUtil.getInstance().proxy(setup, ConfigManager.getProxyInfo());
            }
        }
    }

    public static boolean hasProxy() {
        if (configModel == null)
            return false;
        ProxyInfo proxyInfo = getProxyInfo();
        return proxyInfo != null
                && StringUtils.isNoneEmpty(proxyInfo.getHost())
                && StringUtils.isNoneEmpty(proxyInfo.getPort())
                && Integer.parseInt(proxyInfo.getPort()) > 0;
    }

    public static ProxyInfo getProxyInfo() {
        ProxyInfo proxyInfo = configModel.getProxy();

        if (proxyInfo == null)
            setProxyInfo(new ProxyInfo());

        return configModel.getProxy();
    }

    private static void setProxyInfo(ProxyInfo info) {
        configModel.setProxy(info);
    }

    public static String proxyHost() {
        return getProxyInfo().getHost();
    }

    public static void proxyHost(String host) {
        getProxyInfo().setHost(host);
    }

    public static String proxyPort() {
        return getProxyInfo().getPort();
    }

    public static int getProxyPort() {
        return Integer.parseInt(proxyPort());
    }

    public static void proxyPort(String port) {
        if (!NumberUtils.isParsable(port)) return;

        getProxyInfo().setPort(port);
    }

    public static void checkProxy(BiConsumer<Boolean, String> consumer) {
        if (ProxySetup.SYSTEM.equals(proxySetup())) {
            consumer.accept(true, "");
            return;
        }
        if (!hasProxy()) return;

        ThreadPoolManager.getInstance().execute(() -> {
            try {
                try (Socket socket = new Socket(proxyHost(), getProxyPort())) {
                    Platform.runLater(() -> consumer.accept(true, "success"));
                } catch (IOException e) {
                    Platform.runLater(() -> consumer.accept(false, "connection timed out"));
                }
            } catch (Exception e) {
                logger.error(STR."host=\{proxyHost()},port=\{proxyPort()}", e);
                Platform.runLater(() -> consumer.accept(false, e.getMessage()));
            }
        });
    }

// --------------------------------{ 版本检查 }------------------------------------------

    public static UpgradeConfig upgradeConfig() {
        return upgradeConfig;
    }
}
