package cn.octopusyan.dmt.model;

import cn.octopusyan.dmt.common.enums.ProxySetup;
import lombok.Data;

/**
 * 代理信息
 *
 * @author octopus_yan
 */
@Data
public class ProxyInfo {
    /**
     * 主机地址
     */
    private String host = "";
    /**
     * 端口
     */
    private String port = "";
    /**
     * 登录名
     */
    private String username = "";
    /**
     * 密码
     */
    private String password = "";
    /**
     * 测试Url
     */
    private String testUrl = "http://";
    /**
     * 代理类型
     *
     * @see ProxySetup
     */
    private String setup = ProxySetup.NO_PROXY.getCode();
}
