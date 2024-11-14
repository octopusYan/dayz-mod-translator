package cn.octopusyan.dmt.translate;

import lombok.Getter;

/**
 * API 密钥配置
 *
 * @author octopus_yan@foxmail.com
 */
@Getter
public record ApiKey(String appid, String apiKey) {

}
