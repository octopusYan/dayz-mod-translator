package cn.octopusyan.dayzmodtranslator.manager;

import cn.octopusyan.dayzmodtranslator.config.AppConstant;
import cn.octopusyan.dayzmodtranslator.manager.thread.ThreadPoolManager;
import cn.octopusyan.dayzmodtranslator.manager.word.WordCsvItem;
import cn.octopusyan.dayzmodtranslator.manager.word.WordItem;
import cn.octopusyan.dayzmodtranslator.util.AlertUtil;
import cn.octopusyan.dayzmodtranslator.util.FileUtil;
import cn.octopusyan.dayzmodtranslator.util.ProcessesUtil;
import javafx.application.Platform;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.LineIterator;
import org.apache.commons.io.filefilter.NameFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * PBO 工具类
 *
 * @author octopus_yan@foxmail.com
 * @see <a href="https://github.com/winseros/pboman3">https://github.com/winseros/pboman3</a>
 */
public class PBOUtil {
    private static final Logger logger = LoggerFactory.getLogger(PBOUtil.class);
    private static PBOUtil util;
    private static final String PBOC_DIR_PATH = AppConstant.DATA_DIR_PATH + File.separator + "pboman";
    private static final File PBOC_DIR = new File(PBOC_DIR_PATH);
    private static final String PBOC_FILE_PATH = PBOC_DIR_PATH + File.separator + "pboc.exe";
    private static final File PBOC_FILE = new File(PBOC_FILE_PATH);
    private static final String UNPACK_COMMAND = PBOC_FILE_PATH + " unpack -o " + AppConstant.TMP_DIR_PATH + " %s";
    private static final String PACK_COMMAND = PBOC_FILE_PATH + " pack -o %s %s";
    private OnPackListener onPackListener;
    private OnUnpackListener onUnpackListener;
    private OnFindTransWordListener onFindTransWordListener;
    private String unpackPath;
    private CfgConvertUtil cfgConvertUtil;
    private static final String FILE_NAME_STRING_TABLE = "stringtable.csv";
    private static final String FILE_NAME_CONFIG_BIN = "config.bin";
    private static final String FILE_NAME_CONFIG_CPP = "config.cpp";

    private PBOUtil() {
    }

    public static void init(CfgConvertUtil cfgConvertUtil) {
        if (util == null) {
            util = new PBOUtil();
        }
        // cfg转换工具
        util.cfgConvertUtil = cfgConvertUtil;
        // 检查pbo解析文件
        util.checkPboc();
    }

    public static synchronized PBOUtil getInstance() {
        if (util == null)
            throw new RuntimeException("are you ready ?");
        return util;
    }

    /**
     * 设置打包监听器
     *
     * @param onPackListener 打包监听器
     */
    public void setOnPackListener(OnPackListener onPackListener) {
        this.onPackListener = onPackListener;
    }

    /**
     * 设置解包监听器
     *
     * @param onUnpackListener 监听器
     */
    public void setOnUnpackListener(OnUnpackListener onUnpackListener) {
        this.onUnpackListener = onUnpackListener;
    }

    public void setOnFindTransWordListener(OnFindTransWordListener onFindTransWordListener) {
        this.onFindTransWordListener = onFindTransWordListener;
    }

    private void checkPboc() {
        if (!PBOC_FILE.exists()) initPboc();
    }

    private void initPboc() {
        try {
            FileUtils.forceMkdir(PBOC_DIR);
            String pbocFileName = "pboc.exe";
            String dllFileName = "Qt6Core.dll";
            FileUtil.copyFile(Objects.requireNonNull(PBOUtil.class.getResourceAsStream("/static/pboc/" + pbocFileName)), new File(PBOC_DIR_PATH + File.separator + pbocFileName));
            FileUtil.copyFile(Objects.requireNonNull(PBOUtil.class.getResourceAsStream("/static/pboc/" + dllFileName)), new File(PBOC_DIR_PATH + File.separator + dllFileName));
        } catch (IOException e) {
            logger.error("", e);
        }
    }

    /**
     * 解压PBO文件
     *
     * @param file PBO文件
     */
    public void unpack(File file) {
        // 检查pbo解包程序
        checkPboc();
        // 清理缓存
        clear();

        if (onUnpackListener != null) {
            onUnpackListener.onStart();
        }

        String filePath = file.getAbsolutePath();
        if (filePath.contains(" ")) filePath = "\"" + filePath + "\"";

        String command = String.format(UNPACK_COMMAND, filePath);
        logger.info(command);
        try {
            FileUtils.forceMkdir(new File(AppConstant.TMP_DIR_PATH));

            // 执行命令
            ProcessesUtil.exec(command, new ProcessesUtil.OnExecuteListener() {
                @Override
                public void onExecute(String msg) {
                    logger.info(msg);
                }

                @Override
                public void onExecuteSuccess(int exitValue) {
                    if (exitValue != 0) {
                        String msg = "打开PBO文件失败！";
                        logger.error(msg);
                        if (onUnpackListener != null) {
                            Platform.runLater(() -> {
                                onUnpackListener.onUnpackError(msg);
                                onUnpackListener.onUnpackOver();
                            });
                        }
                        return;
                    }
                    logger.info("打开PBO文件成功！");
                    unpackPath = AppConstant.TMP_DIR_PATH + File.separator + FileUtil.mainName(file);
                    if (onUnpackListener != null) {
                        Platform.runLater(() -> {
                            onUnpackListener.onUnpackSuccess(unpackPath);
                            onUnpackListener.onUnpackOver();
                        });
                    }
                }

                @Override
                public void onExecuteError(Exception e) {
                    logger.error("", e);
                    if (onUnpackListener != null) {
                        Platform.runLater(() -> {
                            onUnpackListener.onUnpackError(e.getMessage());
                            onUnpackListener.onUnpackOver();
                        });
                    }
                }

                @Override
                public void onExecuteOver() {
                    if (onUnpackListener != null) {
                        Platform.runLater(() -> onUnpackListener.onUnpackOver());
                    }
                }
            });
        } catch (Exception e) {
            logger.error("", e);
            if (onUnpackListener != null) {
                Platform.runLater(() -> {
                    onUnpackListener.onUnpackError(e.getMessage());
                    onUnpackListener.onUnpackOver();
                });
            }
        }
    }

    /**
     * 获取待翻译单词列表
     */
    public void startFindWord() {
        // 检查pbo解包文件
        if (unpackPath == null || StringUtils.isBlank(unpackPath))
            throw new RuntimeException("No PBO file was obtained !");

        ThreadPoolManager.getInstance().execute(() -> {
            if (hasStringTable()) {
                List<WordItem> worlds = new ArrayList<>(readCsvFile());
                if (onFindTransWordListener != null) {
                    Platform.runLater(() -> onFindTransWordListener.onFoundWords(worlds, true));
                }
            } else {
                findConfigWord();
            }
        });
    }

    /**
     * 获取csv中 原文及 简中单词
     *
     * @return 待翻译语句列表
     */
    private List<WordCsvItem> readCsvFile() {
        List<WordCsvItem> list = new ArrayList<>();
        AtomicInteger position = new AtomicInteger(0);
        File stringTable = new File(unpackPath + File.separator + FILE_NAME_STRING_TABLE);
        try (LineIterator it = FileUtils.lineIterator(stringTable, StandardCharsets.UTF_8.name())) {
            while (it.hasNext()) {
                String line = it.nextLine();

                if (line.isEmpty() || line.startsWith("//") || line.startsWith("\"Language\"")) {
                    position.addAndGet(1);
                    continue;
                }

                // 原句
                int startIndex = line.indexOf(",\"") + 2;
                int endIndex = line.indexOf("\"", startIndex);
                String original = line.substring(startIndex, endIndex);

                // 中文
                startIndex = StringUtils.ordinalIndexOf(line, ",\"", 11) + 2;
                endIndex = line.indexOf("\"", startIndex);
                int[] chinesePosition = new int[]{startIndex, endIndex};
                String chinese = line.substring(startIndex, endIndex);

                // 简中
                startIndex = StringUtils.ordinalIndexOf(line, ",\"", 14) + 2;
                endIndex = line.indexOf("\"", startIndex);
                int[] chineseSimpPosition = new int[]{startIndex, endIndex};
                String chineseSimp = line.substring(startIndex, endIndex);

                // 添加单词
                list.add(new WordCsvItem(stringTable, position.get(), original, chinese, chinesePosition, chineseSimp, chineseSimpPosition));

                position.addAndGet(1);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        list.sort(Comparator.comparingInt(WordItem::getLines));
        return list;
    }

    /**
     * 获取所有 config.bin 文件内 可翻译内容
     */
    private void findConfigWord() {

        // 搜索所有的 config.bin 文件，并按路径解包到bak文件夹
        List<File> files = new ArrayList<>(FileUtils.listFiles(new File(unpackPath), new NameFileFilter(FILE_NAME_CONFIG_BIN, FILE_NAME_CONFIG_CPP), TrueFileFilter.INSTANCE));

        files.forEach(file -> {

            // 转换bin文件为cpp可读取文件
            if (file.getName().endsWith("bin")) {
                cfgConvertUtil.toTxt(file, file.getParentFile().getAbsolutePath(), new CfgConvertUtil.OnToTxtListener() {
                    @Override
                    public void onToTxtSuccess(String txtFilePath) {
                        // 读取 cpp 文件
                        readCppFile(new File(txtFilePath), file.equals(files.get(files.size() - 1)));
                    }

                    @Override
                    public void onToTxtError(Exception e) {
                        Platform.runLater(() -> {
                            AlertUtil.exception(e).content(FILE_NAME_CONFIG_BIN + "文件转换失败").show();
                        });
                    }
                });
            } else {
                // 读取 cpp 文件
                readCppFile(file, file.equals(files.get(files.size() - 1)));
            }
        });
    }


    private static final Pattern pattern = Pattern.compile(".*((displayName|descriptionShort).?=.?\").*");

    /**
     * 读取cpp文件，查询可翻译文本
     *
     * @param file cpp文件
     */
    private void readCppFile(File file, boolean isEnd) {
        List<WordItem> list = new ArrayList<>();
        AtomicInteger lines = new AtomicInteger(0);
        try (LineIterator it = FileUtils.lineIterator(file, StandardCharsets.UTF_8.name())) {
            while (it.hasNext()) {
                String line = it.nextLine();

                Matcher matcher = pattern.matcher(line);
                if (!line.contains("$") && matcher.find()) {

                    String name = matcher.group(1);

                    // 原始文本
                    int startIndex = line.indexOf(name) + name.length();

                    int endIndex = line.indexOf("\"", startIndex);
                    String original;
                    try {
                        original = line.substring(startIndex, endIndex);
                    } catch (Exception e) {
                        lines.addAndGet(1);
                        continue;
                    }

                    // 添加单词
                    if (!"".endsWith(original) && !containsChinese(original)) {
                        list.add(new WordItem(file, lines.get(), original, "", new int[]{startIndex, endIndex}));
                    }
                }

                lines.addAndGet(1);
            }
        } catch (IOException e) {
            logger.error("", e);
            throw new RuntimeException(e);
        }

        list.sort(Comparator.comparingInt(WordItem::getLines));

        if (onFindTransWordListener != null) {
            Platform.runLater(() -> onFindTransWordListener.onFoundWords(list, isEnd));
        }
    }

    /**
     * 给定字符串是否存在中文
     *
     * @param str 字符串
     * @return 是否存在中文
     */
    private boolean containsChinese(String str) {
        // [\u4e00-\u9fa5]
        return Pattern.compile("[\u4e00-\u9fa5]").matcher(str).find();
    }

    /**
     * 打包PBO文件
     */
    public void pack(List<WordItem> words) {
        if (onPackListener != null) {
            Platform.runLater(() -> onPackListener.onStart());
        }

        ThreadPoolManager.getInstance().execute(() -> {
            File unpackDir;
            if (StringUtils.isBlank(unpackPath)
                    || !(unpackDir = new File(unpackPath)).exists()
                    || !unpackDir.isDirectory()
            ) {
                AlertUtil.error("未获取到打开的pbo文件！").show();
                return;
            }

            // 写入翻译后文本
            try {
                writeWords(words);
            } catch (Exception e) {
                logger.error("writeWords error", e);
                if (onPackListener != null) {
                    Platform.runLater(() -> {
                        onPackListener.onPackOver();
                        onPackListener.onPackError("writeWords error ==> " + e.getMessage());
                    });
                }
                throw new RuntimeException(e);
            }

            // 打包文件临时保存路径
            String packFilePath = unpackPath + ".pbo";
            File packFile = new File(packFilePath);
            if (packFile.exists()) {
                // 如果存在则删除
                FileUtils.deleteQuietly(packFile);
            }

            // 执行打包指令
            String command = String.format(PACK_COMMAND, AppConstant.TMP_DIR_PATH, unpackPath);
            logger.info(command);
            ProcessesUtil.exec(command, new ProcessesUtil.OnExecuteListener() {
                @Override
                public void onExecute(String msg) {
                    logger.info(msg);
                }

                @Override
                public void onExecuteSuccess(int exitValue) {
                    Platform.runLater(() -> {
                        if (exitValue != 0) {
                            logger.error("保存PBO文件失败！");
                            if (onPackListener != null) {
                                onPackListener.onPackOver();
                                onPackListener.onPackError("保存PBO文件失败！");
                            }
                        } else {
                            if (onPackListener != null) {
                                onPackListener.onPackOver();
                                onPackListener.onPackSuccess(packFile);
                            }
                        }
                    });
                }

                @Override
                public void onExecuteError(Exception e) {
                    logger.error("保存PBO文件失败！");
                    if (onPackListener != null) {
                        Platform.runLater(() -> {
                            onPackListener.onPackOver();
                            onPackListener.onPackError(e.getMessage());
                        });
                    }
                }

                @Override
                public void onExecuteOver() {
                    if (onPackListener != null) {
                        Platform.runLater(() -> onPackListener.onPackOver());
                    }
                }
            });
        });
    }

    /**
     * 写入翻译文本
     *
     * @param words 已经翻译好的文本对象
     */
    private void writeWords(List<WordItem> words) throws Exception {
        Map<File, List<WordItem>> wordMap = words.stream()
                .collect(Collectors.groupingBy(WordItem::getFile, Collectors.toList()));

        AtomicInteger progress = new AtomicInteger(0);

        // 0 执行成功 大于0 执行失败
        List<Future<Integer>> result = new ArrayList<>();

        for (Map.Entry<File, List<WordItem>> entry : wordMap.entrySet()) {
            Future<Integer> submit = ThreadPoolManager.getInstance().submit(() -> {
                try {
                    entry.getValue().sort(Comparator.comparingInt(WordItem::getLines));

                    File file = entry.getKey();
                    // 创建备份文件
                    File bakFile = getWordBakFile(file);
                    // 判断重复打包时 备份文件处理
                    if (!bakFile.exists()) {
                        FileUtils.copyFile(file, bakFile);
                    }

                    // 清空原始文件
                    FileWriter fileWriter = new FileWriter(file);
                    fileWriter.write("");
                    fileWriter.flush();
                    fileWriter.close();

                    // 遍历拼接翻译文本并写入
                    long lines = 0;
                    String line;
                    LineIterator it = FileUtils.lineIterator(bakFile, StandardCharsets.UTF_8.name());
                    while (it.hasNext() && !entry.getValue().isEmpty()) {
                        line = it.nextLine();

                        WordItem item = entry.getValue().get(0);
                        int[] ip = item.getPosition();

                        // 拼接翻译后的文本
                        if (lines == item.getLines()) {
                            if (item instanceof WordCsvItem csv) {
                                int[] simp = csv.getPositionSimp();
                                // 判断 ip 是否比 simp 靠前
                                boolean tag = ip[0] < simp[0];
                                line = line.substring(0, (tag ? ip : simp)[0])
                                        + (tag ? csv.getChinese() : csv.getChineseSimp())
                                        + line.substring((tag ? ip : simp)[1], (tag ? simp : ip)[0])
                                        + (tag ? csv.getChineseSimp() : csv.getChinese())
                                        + line.substring((tag ? simp : ip)[1]);
                            } else {
                                try {
                                    line = line.substring(0, ip[0])
                                            + item.getChinese()
                                            + line.substring(ip[1]);
                                } catch (Exception e) {
                                    System.out.println(line);
                                }
                            }

                            entry.getValue().remove(item);
                            if (onPackListener != null) {
                                Platform.runLater(() -> onPackListener.onProgress(progress.addAndGet(1), words.size()));
                            }
                        }

                        // 写入原始文件
                        FileUtils.writeStringToFile(file, line + System.lineSeparator(), StandardCharsets.UTF_8, true);

                        lines++;
                    }

                    // 关闭流
                    IOUtils.closeQuietly(it);

                    // cpp 文件需要 转换为 bin
                    if (file.getName().endsWith("cpp")) {
                        File binFile = new File(file.getParent() + File.separator + FileUtil.mainName(file) + ".bin");
                        if (binFile.exists()) {
                            // 转为bin文件
                            cfgConvertUtil.toBin(file);
                            // 删除cpp文件
                            FileUtils.deleteQuietly(file);
                        }
                        // 同目录下不存在bin文件，说明原始文件为cpp 无需转换
                    }
                } catch (Exception e) {
                    logger.error("写入翻译文本失败", e);
                    // 执行失败
                    return 1;
                }
                // 执行成功
                return 0;
            });

            // 添加执行结果
            result.add(submit);
        }

        for (Future<Integer> future : result) {
            if (future.get() > 0)
                throw new IOException();
        }
    }

    private File getWordBakFile(File wordFile) {
        return new File(wordFile.getAbsolutePath().replace(unpackPath, AppConstant.BAK_FILE_PATH));
    }

    /**
     * 是否有国际化翻译文件
     *
     * @return 是否有 <code>stringtable.csv</code> 文件
     */
    private boolean hasStringTable() {
        List<String> fileNames = FileUtil.listFileNames(unpackPath);
        return fileNames.stream().anyMatch(FILE_NAME_STRING_TABLE::equals);
    }

    /**
     * 清理缓存文件
     */
    public static void clear() {
        File tmpDest = new File(AppConstant.TMP_DIR_PATH);

        if (tmpDest.exists())
            FileUtils.deleteQuietly(tmpDest);
    }

    public interface OnUnpackListener {
        /**
         * 开始解包
         */
        void onStart();

        /**
         * 解包完成
         *
         * @param unpackDirPath 解包文件夹绝对路径
         */
        void onUnpackSuccess(String unpackDirPath);

        /**
         * 解包失败
         *
         * @param msg 失败信息
         */
        void onUnpackError(String msg);

        void onUnpackOver();
    }

    public interface OnPackListener {
        /**
         * 开始解包
         */
        void onStart();

        void onProgress(long current, long all);

        /**
         * 打包完成
         */
        void onPackSuccess(File packFile);

        /**
         * 打包失败
         *
         * @param msg 失败信息
         */
        void onPackError(String msg);

        void onPackOver();
    }

    public interface OnFindTransWordListener {
        void onFoundWords(List<WordItem> worlds, boolean isOver);
    }
}
