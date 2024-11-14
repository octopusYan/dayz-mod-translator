package cn.octopusyan.dmt.translate.processor;

import cn.octopusyan.dmt.common.manager.http.CookieManager;
import cn.octopusyan.dmt.common.util.JsonUtil;
import cn.octopusyan.dmt.translate.TranslateApi;
import com.fasterxml.jackson.databind.JsonNode;

import java.io.IOException;
import java.net.HttpCookie;
import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 谷歌 免费翻译接口
 *
 * @author octopus_yan@foxmail.com
 */
public class FreeBaiduTranslateProcessor extends AbstractTranslateProcessor {
    private static final Map<String, Object> header = new HashMap<>();

    static {
        header.put("Origin", "https://fanyi.baidu.com");
        header.put("Referer", "https://fanyi.baidu.com");
        header.put("User-Agent", "Apifox/1.0.0 (https://apifox.com)");
        header.put("Accept","*/*");
    }

    public FreeBaiduTranslateProcessor() {
        super(TranslateApi.FREE_BAIDU);
    }

    @Override
    public String url() {
        return "https://fanyi.baidu.com/transapi";
    }

    @Override
    public int qps() {
        return source().getDefaultQps();
    }

    /**
     * 翻译处理
     *
     * @param source 待翻译单词
     * @return 翻译结果
     */
    @Override
    public String customTranslate(String source) throws IOException, InterruptedException {

        Map<String, Object> param = new HashMap<>();
        param.put("query", source);
        param.put("from", "auto");
        param.put("to", "zh");
        param.put("source", "txt");

        String resp = httpUtil.post(url(), JsonUtil.parseJsonObject(header), JsonUtil.parseJsonObject(param));
        JsonNode json = JsonUtil.parseJsonObject(resp);

        if (!json.has("data")) {
            String errorMsg = json.get("errmsg").asText();
            if("访问出现异常，请刷新后重试！".equals(errorMsg) && !header.containsKey("Cookie")) {
                checkCookie();
                return customTranslate(source);
            }
            throw new RuntimeException(errorMsg);
        }

        return json.get("data").get(0).get("dst").asText();
    }

    private void checkCookie() throws IOException, InterruptedException {
        // 短时大量请求会被ban，需要添加验证cookie

        if (header.containsKey("Cookie")) return;

        List<HttpCookie> cookieList = CookieManager.getStore().get(URI.create("https://baidu.com"));
        boolean noneMatch = cookieList.stream()
                .filter(cookie -> "ab_sr".equals(cookie.getName()) || "BAIDUID".equals(cookie.getName()))
                .count() < 2;

        if (noneMatch) {
            String url = "https://miao.baidu.com/abdr?_o=https%3A%2F%2Ffanyi.baidu.com";
            Map<String, Object> param = new HashMap<>();
            param.put("data", miao_data);
            param.put("key_id", "6e75c85adea0454a");
            param.put("enc", 2);
            httpUtil.post(url, JsonUtil.parseJsonObject(header), JsonUtil.parseJsonObject(param));
        }

        cookieList = CookieManager.getStore().get(URI.create("https://baidu.com"));
        List<HttpCookie> cookies = cookieList.stream()
                .filter(cookie -> "ab_sr".equals(cookie.getName()) || "BAIDUID".equals(cookie.getName()))
                .toList();

        String collect = cookies.stream()
                .map(cookie -> cookie.getName() + "=" + cookie.getValue())
                .collect(Collectors.joining(";"));

        header.put("Cookie", collect);
    }

    private static final String miao_data = "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";
}
