package cn.octopusyan.dmt.model;

import cn.octopusyan.dmt.translate.TranslateApi;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

/**
 * 翻译配置
 *
 * @author octopus_yan
 */
@Data
public class Translate {
    /**
     * 当前使用接口
     */
    private String use = TranslateApi.FREE_BAIDU.getName();
    /**
     * 接口配置
     */
    private Map<String, Config> config = new HashMap<>() {
        {
            // 初始化
            for (TranslateApi api : TranslateApi.values()) {
                put(api.getName(), new Config("", "", api.getDefaultQps()));
            }
        }
    };

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Config {
        /**
         * api key
         */
        private String appId;
        /**
         * api 密钥
         */
        private String secretKey;
        /**
         * 请求速率
         */
        private Integer qps;

    }
}
