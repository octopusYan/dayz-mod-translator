package cn.octopusyan.dmt.model;

import cn.octopusyan.dmt.common.util.PropertiesUtils;
import lombok.Data;

/**
 * 更新配置
 *
 * @author octopus_yan
 */
@Data
public class UpgradeConfig {

    private final String owner = "octopusYan";

    private final String repo = "dayz-mod-translator";

    private String releaseFile = "DMT-windows-nojre.zip";

    private String version = PropertiesUtils.getInstance().getProperty("app.version");

    public String getReleaseApi() {
        return STR."https://api.github.com/repos/\{getOwner()}/\{getRepo()}/releases/latest";
    }

    public String getDownloadUrl(String version) {
        return STR."https://github.com/\{getOwner()}/\{getRepo()}/releases/download/\{version}/\{getReleaseFile()}";
    }
}
