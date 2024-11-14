package cn.octopusyan.dmt.common.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 代理类型
 *
 * @author octopus_yan
 */
@Getter
@RequiredArgsConstructor
public enum ProxySetup {
    /**
     * 不使用代理
     */
    NO_PROXY("no_proxy", "不使用代理"),
    /**
     * 系统代理
     */
    SYSTEM("system", "系统代理"),
    /**
     * 自定义代理
     */
    MANUAL("manual", "自定义代理");

    private final String code;
    private final String name;
}
