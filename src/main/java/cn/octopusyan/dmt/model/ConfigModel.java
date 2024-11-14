package cn.octopusyan.dmt.model;

import cn.octopusyan.dmt.common.manager.ConfigManager;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * GUI配置信息
 *
 * @author octopus_yan
 */
@Data
public class ConfigModel {
    private static final Logger log = LoggerFactory.getLogger(ConfigModel.class);

    /**
     * 主题
     */
    private String theme = ConfigManager.DEFAULT_THEME;

    /**
     * 代理设置
     */
    private ProxyInfo proxy = new ProxyInfo();

    /**
     * 代理设置
     */
    private Translate translate = new Translate();


}
