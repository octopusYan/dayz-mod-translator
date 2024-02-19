package cn.octopusyan.dayzmodtranslator.manager.translate;

/**
 * 翻译引擎类型
 *
 * @author octopus_yan@foxmail.com
 */
public enum TranslateSource {
    FREE_GOOGLE("free_google", "谷歌(免费)", false, 50),
    BAIDU("baidu", "百度(需认证)", true),

    ;
    private final String name;
    private final String label;
    private final boolean needApiKey;
    private Integer defaultQps;

    TranslateSource(String name, String label, boolean needApiKey) {
        // 设置接口默认qps=10
        this(name, label, needApiKey, 10);
    }

    TranslateSource(String name, String label, boolean needApiKey, int defaultQps) {
        this.name = name;
        this.label = label;
        this.needApiKey = needApiKey;
        this.defaultQps = defaultQps;
    }

    public String getName() {
        return name;
    }

    public String getLabel() {
        return label;
    }

    public boolean needApiKey() {
        return needApiKey;
    }

    public Integer getDefaultQps() {
        return defaultQps;
    }

    public String getDefaultQpsStr() {
        return String.valueOf(defaultQps);
    }

    public static TranslateSource get(String type) {
        for (TranslateSource value : values()) {
            if (value.getName().equals(type))
                return value;
        }
        throw new RuntimeException("类型不存在");
    }

    public static TranslateSource getByLabel(String label) {
        for (TranslateSource value : values()) {
            if (value.getLabel().equals(label))
                return value;
        }
        throw new RuntimeException("类型不存在");
    }
}
