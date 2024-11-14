package cn.octopusyan.dmt.translate.processor;

import cn.octopusyan.dmt.common.util.JsonUtil;
import cn.octopusyan.dmt.translate.ApiKey;
import cn.octopusyan.dmt.translate.TranslateApi;
import com.fasterxml.jackson.databind.JsonNode;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 谷歌 免费翻译接口
 *
 * @author octopus_yan@foxmail.com
 */
public class BaiduTranslateProcessor extends AbstractTranslateProcessor {

    private ApiKey apiKey;

    public BaiduTranslateProcessor() {
        super(TranslateApi.BAIDU);
    }

    @Override
    public String url() {
        return "https://fanyi-api.baidu.com/api/trans/vip/translate";
    }

    /**
     * 翻译处理
     *
     * @param source 待翻译单词
     * @return 翻译结果
     */
    @Override
    public String customTranslate(String source) throws IOException, InterruptedException {
        apiKey = getApiKey();
        String appid = apiKey.appid();
        String salt = UUID.randomUUID().toString().replace("-", "");

        Map<String, Object> param = new HashMap<>();
        param.put("q", source);
        param.put("from", "auto");
        param.put("to", "zh");
        param.put("appid", appid);
        param.put("salt", salt);
        param.put("sign", getSign(appid, source, salt));

        String resp = httpUtil.get(url(), null, JsonUtil.parseJsonObject(param));
        JsonNode json = JsonUtil.parseJsonObject(resp);

        if (!json.has("trans_result")) {
            Object errorMsg = json.get("error_msg");
            logger.error("翻译失败: {}", errorMsg);
            throw new RuntimeException(String.valueOf(errorMsg));
        }

        return json.get("trans_result").get(0).get("dst").asText();
    }

    private String getSign(String appid, String q, String salt) {
        return encrypt2ToMD5(appid + q + salt + apiKey.apiKey());
    }

    /**
     * MD5加密
     *
     * @param str 待加密字符串
     * @return 16进制加密字符串
     */
    public String encrypt2ToMD5(String str) {

        // 加密后的16进制字符串
        String hexStr = "";
        try {

            // 此 MessageDigest 类为应用程序提供信息摘要算法的功能
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            // 转换为MD5码
            byte[] digest = md5.digest(str.getBytes(StandardCharsets.UTF_8));
            hexStr = bytesToHexString(digest);
        } catch (Exception e) {
            logger.error("", e);
        }
        return hexStr.toLowerCase();
    }

    /**
     * 将byte数组转换成string类型表示
     */
    private String bytesToHexString(byte[] src) {
        StringBuilder builder = new StringBuilder();
        if (src == null || src.length == 0) {
            return null;
        }
        String hv;
        for (byte b : src) {
            // 以十六进制（基数 16）无符号整数形式返回一个整数参数的字符串表示形式，并转换为大写
            hv = Integer.toHexString(b & 0xFF).toUpperCase();
            if (hv.length() < 2) {
                builder.append(0);
            }
            builder.append(hv);
        }

        return builder.toString();
    }

}
