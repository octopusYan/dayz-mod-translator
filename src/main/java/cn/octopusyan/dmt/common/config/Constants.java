package cn.octopusyan.dmt.common.config;


import cn.octopusyan.dmt.common.util.PropertiesUtils;

import java.io.File;
import java.nio.file.Paths;

/**
 * 应用信息
 *
 * @author octopus_yan@foxmail.com
 */
public class Constants {
    public static final String APP_TITLE = PropertiesUtils.getInstance().getProperty("app.title");
    public static final String APP_NAME = PropertiesUtils.getInstance().getProperty("app.name");
    public static final String APP_VERSION = PropertiesUtils.getInstance().getProperty("app.version");

    public static final String DATA_DIR_PATH = Paths.get("").toFile().getAbsolutePath();
    public static final String BIN_DIR_PATH = STR."\{DATA_DIR_PATH}\{File.separator}bin";
    public static final String TMP_DIR_PATH = STR."\{DATA_DIR_PATH}\{File.separator}tmp";
    public static final String BAK_DIR_PATH = STR."\{DATA_DIR_PATH}\{File.separator}bak";

    public static final String CONFIG_FILE_PATH = STR."\{DATA_DIR_PATH}\{File.separator}config.yaml";
    public static final String PBOC_FILE = STR."\{BIN_DIR_PATH}\{File.separator}pboc.exe";
    public static final String CFG_CONVERT_FILE = STR."\{BIN_DIR_PATH}\{File.separator}CfgConvert.exe";
}
