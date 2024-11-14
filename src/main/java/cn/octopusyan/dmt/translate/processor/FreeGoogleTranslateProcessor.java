package cn.octopusyan.dmt.translate.processor;


import cn.octopusyan.dmt.common.util.JsonUtil;
import cn.octopusyan.dmt.translate.TranslateApi;
import com.fasterxml.jackson.databind.JsonNode;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * 谷歌 免费翻译接口
 *
 * @author octopus_yan@foxmail.com
 */
public class FreeGoogleTranslateProcessor extends AbstractTranslateProcessor {

    public FreeGoogleTranslateProcessor() {
        super(TranslateApi.FREE_GOOGLE);
    }

    @Override
    public String url() {
        return "https://translate.googleapis.com/translate_a/single";
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

        Map<String, Object> form = new HashMap<>();
        form.put("client", "gtx");
        form.put("dt", "t");
        form.put("sl", "auto");
        form.put("tl", "zh-CN");
        form.put("q", source);

        Map<String, Object> header = new HashMap<>();
        StringBuilder retStr = new StringBuilder();
        // TODO 短时大量请求会被ban，需要浏览器验证添加cookie

        String resp = httpUtil.get(url(), JsonUtil.parseJsonObject(header), JsonUtil.parseJsonObject(form));
        JsonNode json = JsonUtil.parseJsonObject(resp);

        for (JsonNode o : json.get(0)) {
            retStr.append(o.get(0).asText());
        }

        return retStr.toString();
    }
}
