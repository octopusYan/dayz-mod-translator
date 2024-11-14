package cn.octopusyan.dmt.translate;

import cn.octopusyan.dmt.model.Translate;
import lombok.Getter;

/**
 * 翻译引擎类型
 *
 * @author octopus_yan@foxmail.com
 */
@Getter
public enum TranslateApi {
    FREE_BAIDU("free_baidu", "百度", false),
    FREE_GOOGLE("free_google", "谷歌", false),
    BAIDU("baidu", "百度(需认证)", true),
    ;

    @Getter
    private final String name;
    @Getter
    private final String label;
    private final boolean needApiKey;
    private final Integer defaultQps;

    TranslateApi(String name, String label, boolean needApiKey) {
        // 设置接口默认qps=10
        this(name, label, needApiKey, 10);
    }

    TranslateApi(String name, String label, boolean needApiKey, int defaultQps) {
        this.name = name;
        this.label = label;
        this.needApiKey = needApiKey;
        this.defaultQps = defaultQps;
    }

    public boolean needApiKey() {
        return needApiKey;
    }

    public Translate.Config translate() {
        return new Translate.Config("", "", defaultQps);
    }

    public static TranslateApi get(String name) {
        for (TranslateApi value : values()) {
            if (value.getName().equals(name))
                return value;
        }
        throw new RuntimeException("类型不存在");
    }

    public static TranslateApi getByLabel(String label) {
        for (TranslateApi value : values()) {
            if (value.getLabel().equals(label))
                return value;
        }
        throw new RuntimeException("类型不存在");
    }
}
