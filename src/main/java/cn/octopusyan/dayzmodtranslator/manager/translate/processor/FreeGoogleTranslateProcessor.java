package cn.octopusyan.dayzmodtranslator.manager.translate.processor;

import cn.octopusyan.dayzmodtranslator.manager.translate.TranslateSource;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;

import java.io.IOException;

/**
 * 谷歌 免费翻译接口
 *
 * @author octopus_yan@foxmail.com
 */
public class FreeGoogleTranslateProcessor extends AbstractTranslateProcessor {

    public FreeGoogleTranslateProcessor(TranslateSource translateSource) {
        super(translateSource);
    }

    @Override
    public String url() {
        return "https://translate.googleapis.com/translate_a/single";
    }

    /**
     * 翻译处理
     *
     * @param source 待翻译单词
     * @return 翻译结果
     */
    @Override
    public String customTranslate(String source) throws IOException, InterruptedException {

        JSONObject form = new JSONObject();
        form.put("client", "gtx");
        form.put("dt", "t");
        form.put("sl", "auto");
        form.put("tl", "zh-CN");
        form.put("q", source);

        JSONObject header = new JSONObject();
        StringBuilder retStr = new StringBuilder();
        // TODO 短时大量请求会被ban，需要浏览器验证添加cookie
        String resp = httpUtil.get(url(), header, form);
        JSONArray jsonObject = JSONArray.parseArray(resp);
        for (Object o : jsonObject.getJSONArray(0)) {
            JSONArray a = (JSONArray) o;
            retStr.append(a.getString(0));
        }

        return retStr.toString();
    }
}
