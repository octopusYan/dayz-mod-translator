package cn.octopusyan.dayzmodtranslator.manager;

import cn.octopusyan.dayzmodtranslator.config.AppConstant;
import cn.octopusyan.dayzmodtranslator.util.FileUtil;
import cn.octopusyan.dayzmodtranslator.util.ProcessesUtil;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.Objects;

/**
 * Cfg文件转换工具类
 *
 * @author octopus_yan@foxmail.com
 */
public class CfgConvertUtil {
    private static final Logger logger = LoggerFactory.getLogger(CfgConvertUtil.class);
    private static CfgConvertUtil util;
    private static final String CfgConvert_DIR_PATH = AppConstant.DATA_DIR_PATH + File.separator + "CfgConvert";
    private static final File CfgConvert_DIR = new File(CfgConvert_DIR_PATH);
    private static final String CfgConvert_FILE_PATH = CfgConvert_DIR_PATH + File.separator + "CfgConvert.exe";
    private static final File CfgConvert_FILE = new File(CfgConvert_FILE_PATH);
    private static final String COMMAND = CfgConvert_FILE_PATH + " %s -dst %s %s";

    private CfgConvertUtil() {
    }

    public static void init() {
        if (util == null) {
            util = new CfgConvertUtil();
        }
        // 检查pbo解析文件
        util.checkCfgConvert();
    }

    public static synchronized CfgConvertUtil getInstance() {
        if (util == null)
            throw new RuntimeException("are you ready ?");
        return util;
    }

    /**
     * 检查Cfg转换文件
     */
    private void checkCfgConvert() {
        if (!CfgConvert_FILE.exists()) initCfgConvert();
    }

    private void initCfgConvert() {
        try {
            FileUtils.forceMkdir(CfgConvert_DIR);
            String cfgConvertFileName = "CfgConvert.exe";
            FileUtil.copyFile(Objects.requireNonNull(CfgConvertUtil.class.getResourceAsStream("/static/CfgConvert/" + cfgConvertFileName)), new File(CfgConvert_DIR_PATH + File.separator + cfgConvertFileName));
        } catch (IOException e) {
            logger.error("", e);
        }
    }

    public void toTxt(File binFile, String outPath, OnToTxtListener onToTxtListener) {
        String fileName = binFile.getAbsolutePath();
        ProcessesUtil.exec(toTxtCommand(binFile, outPath), new ProcessesUtil.OnExecuteListener() {
            @Override
            public void onExecute(String msg) {
                logger.info(fileName + " : " + msg);
            }

            @Override
            public void onExecuteSuccess(int exitValue) {
                logger.info(fileName + " : to txt success");
                String outFilePath = outFilePath(binFile, outPath, ".cpp");
                if (onToTxtListener != null) {
                    onToTxtListener.onToTxtSuccess(outFilePath);
                }
            }

            @Override
            public void onExecuteError(Exception e) {
                logger.error(fileName + " : to txt error", e);
                if (onToTxtListener != null) {
                    onToTxtListener.onToTxtError(e);
                }
            }

            @Override
            public void onExecuteOver() {
                logger.info(fileName + " : to txt end...");
            }
        });
    }

    public void toBin(File cppFile) {
        ProcessesUtil.exec(toBinCommand(cppFile, cppFile.getParentFile().getAbsolutePath()), new ProcessesUtil.OnExecuteListener() {
            @Override
            public void onExecute(String msg) {

            }

            @Override
            public void onExecuteSuccess(int exitValue) {

            }

            @Override
            public void onExecuteError(Exception e) {

            }

            @Override
            public void onExecuteOver() {

            }
        });
    }

    private String toBinCommand(File cppFile, String outPath) {
        String outFilePath = outFilePath(cppFile, outPath, ".bin");
        return String.format(COMMAND, "-bin", outFilePath, cppFile.getAbsolutePath());
    }

    private String toTxtCommand(File binFile, String outPath) {
        String outFilePath = outFilePath(binFile, outPath, ".cpp");
        return String.format(COMMAND, "-txt", outFilePath, binFile.getAbsolutePath());
    }

    private String outFilePath(File file, String outPath, String suffix) {
        return outPath + File.separator + FileUtil.mainName(file) + suffix;
    }

    public interface OnToTxtListener {
        void onToTxtSuccess(String txtFilePath);

        void onToTxtError(Exception e);
    }
}
