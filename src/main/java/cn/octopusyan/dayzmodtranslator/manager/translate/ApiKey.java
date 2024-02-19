package cn.octopusyan.dayzmodtranslator.manager.translate;

/**
 * API 密钥配置
 *
 * @author octopus_yan@foxmail.com
 */
public class ApiKey {
    private String appid;
    private String apiKey;

    public ApiKey() {
    }

    public ApiKey(String appid, String apiKey) {
        this.appid = appid;
        this.apiKey = apiKey;
    }

    public String getAppid() {
        return appid;
    }

    public String getApiKey() {
        return apiKey;
    }
}
