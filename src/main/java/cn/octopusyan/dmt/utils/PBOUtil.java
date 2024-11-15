package cn.octopusyan.dmt.utils;

import cn.octopusyan.dmt.common.config.Constants;
import cn.octopusyan.dmt.common.config.Context;
import cn.octopusyan.dmt.common.util.ProcessesUtil;
import cn.octopusyan.dmt.model.WordItem;
import cn.octopusyan.dmt.view.ConsoleLog;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * PBO 文件工具
 *
 * @author octopus_yan
 */
public class PBOUtil {
    private static final Logger log = LoggerFactory.getLogger(PBOUtil.class);
    public static final ConsoleLog consoleLog = ConsoleLog.getInstance(PBOUtil.class);

    private static final ProcessesUtil processesUtil = ProcessesUtil.init(Constants.BIN_DIR_PATH);

    private static final String UNPACK_COMMAND = STR."\{Constants.PBOC_FILE} unpack -o \{Constants.TMP_DIR_PATH} {}";
    private static final String PACK_COMMAND = STR."\{Constants.PBOC_FILE} pack -o {} {}";
    private static final String CFG_COMMAND = STR."\{Constants.CFG_CONVERT_FILE} {} -dst {} {}";

    private static final String FILE_NAME_STRING_TABLE = "stringtable.csv";
    private static final String FILE_NAME_CONFIG_BIN = "config.bin";
    private static final String FILE_NAME_CONFIG_CPP = "config.cpp";
    private static final String[] FILE_NAME_LIST = new String[]{"csv", "bin", "cpp", "layout"};

    private static final Pattern CPP_PATTERN = Pattern.compile(".*(displayName|descriptionShort) ?= ?\"(.*)\";.*");
    private static final Pattern LAYOUT_PATTERN = Pattern.compile(".*text \"(.*)\".*");


    public static void init() {
        try {
            File destDir = new File(Constants.BIN_DIR_PATH);

            if (destDir.exists()) return;

            if (!Context.isDebugMode())
                throw new RuntimeException("Util 初始化失败");

            String srcFilePath = Resources.getResource("bin").getPath();
            FileUtils.forceMkdir(destDir);
            FileUtils.copyDirectory(new File(srcFilePath), destDir);

        } catch (IOException e) {
            log.error("Util 初始化失败", e);
        }
    }

    /**
     * 解包pbo文件
     *
     * @param path pbo文件地址
     * @return 解包输出路径
     */
    public static String unpack(String path) {
        return unpack(new File(path));
    }

    /**
     * 解包pbo文件
     *
     * @param pboFile pbo文件
     * @return 解包输出路径
     */
    public static String unpack(File pboFile) {
        if (!pboFile.exists())
            throw new RuntimeException("文件不存在！");

        File directory = new File(Constants.TMP_DIR_PATH);
        String outputPath = Constants.TMP_DIR_PATH + File.separator + FileUtil.mainName(pboFile);
        try {
            FileUtils.deleteQuietly(new File(outputPath));
            FileUtils.forceMkdir(directory);
        } catch (IOException e) {
            throw new RuntimeException("文件夹创建失败", e);
        }

        String command = ProcessesUtil.format(UNPACK_COMMAND, pboFile.getAbsolutePath());
        consoleLog.debug(STR."unpack command ==> [\{command}]");
        boolean exec = processesUtil.exec(command);
        if (!exec)
            throw new RuntimeException("解包失败！");

        return outputPath;
    }

    /**
     * 打包pbo文件
     *
     * @param unpackPath pbo解包文件路径
     * @return 打包文件
     */
    public static File pack(String unpackPath) {
        String outputPath = STR."\{unpackPath}.pbo";

        // 打包文件临时保存路径
        File packFile = new File(outputPath);
        if (packFile.exists()) {
            // 如果存在则删除
            FileUtils.deleteQuietly(packFile);
        }

        String command = ProcessesUtil.format(PACK_COMMAND, Constants.TMP_DIR_PATH, unpackPath);
        consoleLog.debug(STR."pack command ==> [\{command}]");

        boolean exec = processesUtil.exec(command);
        if (!exec) throw new RuntimeException("打包失败！");

        return packFile;
    }

    /**
     * 查找可翻译文本
     *
     * @param path 根目录
     */
    public static List<WordItem> findWord(String path) {
        return findWord(new File(path));
    }

    public static List<WordItem> findWord(File file) {
        ArrayList<WordItem> wordItems = new ArrayList<>();
        if (!file.exists())
            return wordItems;

        List<File> files = new ArrayList<>(FileUtils.listFiles(file, FILE_NAME_LIST, true));
        for (File item : files) {
            wordItems.addAll(findWordByFile(item));
        }

        return wordItems;
    }

    /**
     * 写入文件
     *
     * @param wordFileMap 文件对应文本map
     */
    public static void writeWords(Map<File, List<WordItem>> wordFileMap) {

        for (Map.Entry<File, List<WordItem>> entry : wordFileMap.entrySet()) {

            Map<Integer, WordItem> wordMap = entry.getValue().stream()
                    .collect(Collectors.toMap(WordItem::getLines, Function.identity()));

            File file = entry.getKey();

            // 需要转bin文件时，写入bak目录下cpp文件
            boolean hasBin = new File(outFilePath(file, ".bin")).exists();
            String writePath = file.getAbsolutePath().replace(Constants.BAK_DIR_PATH, Constants.TMP_DIR_PATH);
            File writeFile = hasBin ? file : new File(writePath);

            AtomicInteger lineIndex = new AtomicInteger(0);
            List<String> lines = new ArrayList<>();

            consoleLog.info("正在写入文件[{}]", writeFile.getAbsolutePath());

            try (LineIterator it = FileUtils.lineIterator(file, StandardCharsets.UTF_8.name())) {
                while (it.hasNext()) {
                    String line = it.next();
                    WordItem word = wordMap.get(lineIndex.get());

                    // 当前行是否有需要替换的文本
                    // TODO 是否替换空文本
                    if (word != null && line.contains(word.getOriginal())) {
                        line = line.substring(0, word.getIndex()) +
                                line.substring(word.getIndex()).replace(word.getOriginal(), word.getChinese());
                    }

                    // 缓存行内容
                    lines.add(line);

                    lineIndex.addAndGet(1);
                }
            } catch (IOException e) {
                consoleLog.error(STR."文件[\{file.getAbsoluteFile()}]读取出错", e);
            }

            try {
                // 写入文件
                String charsets = writeFile.getName().endsWith(".layout") ? FileUtil.getCharsets(writeFile) : StandardCharsets.UTF_8.name();
                FileUtils.writeLines(writeFile, charsets, lines);
            } catch (IOException e) {
                consoleLog.error(STR."文件(\{writeFile.getAbsoluteFile()})写入失败", e);
            }

            // CPP转BIN (覆盖TMP下BIN文件)
            if (hasBin) cpp2bin(writeFile);
        }
    }

    /**
     * 查找文件内可翻译文本
     *
     * @param file 文件
     * @return 可翻译文本信息列表
     */
    private static List<WordItem> findWordByFile(File file) {
        if (!FILE_NAME_CONFIG_CPP.equals(file.getName())
                && !FILE_NAME_CONFIG_BIN.equals(file.getName())
                && !FILE_NAME_STRING_TABLE.equals(file.getName())
                && !file.getName().endsWith(".layout")
        ) {
            return Collections.emptyList();
        }

        // 创建备份(在bak文件夹下的同级目录
        file = createBak(file);

        // bin转cpp
        if (FILE_NAME_CONFIG_BIN.equals(file.getName())) {
            file = bin2cpp(file);
        }

        String charset = file.getName().endsWith(".layout") ? FileUtil.getCharsets(file) : StandardCharsets.UTF_8.name();

        try (LineIterator it = FileUtils.lineIterator(file, charset)) {
            // CPP
            if (FILE_NAME_CONFIG_CPP.equals(file.getName())) {
                return findWordByCPP(file, it);
            }
            // CSV
            if (FILE_NAME_STRING_TABLE.equals(file.getName())) {
                return findWordByCSV(file, it);
            }
            // layout
            if (file.getName().endsWith(".layout")) {
                return findWordByLayout(file, it);
            }

            // TODO 待添加更多文件格式
            return Collections.emptyList();

        } catch (IOException e) {
            consoleLog.error(STR."文件[\{file.getAbsoluteFile()}]读取出错", e);
        }

        return Collections.emptyList();
    }

    /**
     * 从csv文件中读取可翻译文本
     *
     * @param file csv文件
     * @param it   行内容遍历器
     * @return 可翻译文本列表
     */
    private static List<WordItem> findWordByCSV(File file, LineIterator it) {
        ArrayList<WordItem> wordItems = new ArrayList<>();
        AtomicInteger lines = new AtomicInteger(0);
        int index = -1;
        String line;
        while (it.hasNext()) {
            line = it.next();
            List<String> split = Arrays.stream(line.split(",")).toList();

            if (lines.get() == 0) {
                index = split.indexOf("\"chinese\"");
            } else if (index < split.size()) {
                // 原文
                String original = split.get(index).replaceAll("\"", "");
                // 开始下标
                Integer startIndex = line.indexOf(original);
                // 添加单词
                if (original.length() > 1) {
                    wordItems.add(new WordItem(file, lines.get(), startIndex, original, ""));
                }
            }

            lines.addAndGet(1);
        }
        return wordItems;
    }

    /**
     * 从layout文件中读取可翻译文本
     *
     * @param file layout文件
     * @param it   行内容遍历器
     * @return 可翻译文本列表
     */
    private static List<WordItem> findWordByLayout(File file, LineIterator it) {
        ArrayList<WordItem> wordItems = new ArrayList<>();
        AtomicInteger lines = new AtomicInteger(0);
        String line;
        Matcher matcher;
        while (it.hasNext()) {
            line = it.next();
            matcher = LAYOUT_PATTERN.matcher(line);
            if (StringUtils.isNoneEmpty(line) && matcher.matches()) {
                // 原文
                String original = matcher.group(1);
                // 开始下标
                Integer startIndex = line.indexOf(original);
                // 添加单词
                if (original.length() > 1) {
                    wordItems.add(new WordItem(file, lines.get(), startIndex, original, ""));
                }
            }

            lines.addAndGet(1);
        }
        return wordItems;
    }

    /**
     * 读取cpp文件内可翻译文本
     *
     * @param file cpp文件
     * @param it   行内容遍历器
     * @return 可翻译文本列表
     */
    private static List<WordItem> findWordByCPP(File file, LineIterator it) {
        ArrayList<WordItem> wordItems = new ArrayList<>();
        AtomicInteger lines = new AtomicInteger(0);

        while (it.hasNext()) {
            String line = it.next();

            Matcher matcher = CPP_PATTERN.matcher(line);
            if (!line.contains("$") && matcher.matches()) {

                String name = matcher.group(1);

                // 原始文本
                int startIndex = line.indexOf(name) + name.length();
                String original = matcher.group(2);

                // 添加单词
                if (original.length() > 1) {
                    wordItems.add(new WordItem(file, lines.get(), startIndex, original, ""));
                }
            }

            lines.addAndGet(1);
        }
        return wordItems;
    }

    /**
     * 创建备份文件
     */
    private static File createBak(File file) {
        try {
            String absolutePath = file.getAbsolutePath().replace(Constants.TMP_DIR_PATH, Constants.BAK_DIR_PATH);
            File destFile = new File(absolutePath);
            FileUtils.copyFile(file, destFile);
            return destFile;
        } catch (IOException e) {
            consoleLog.error(STR."创建备份文件失败[\{file.getAbsolutePath()}]", e);
        }
        return file;
    }

    /**
     * bin 转 cpp
     *
     * @param file bin文件
     * @return cpp文件
     */
    private static File bin2cpp(File file) {
        boolean exec = processesUtil.exec(toTxtCommand(file));

        if (!exec) throw new RuntimeException("bin2cpp 失败");

        return new File(outFilePath(file, ".cpp"));
    }

    /**
     * cpp 转 bin
     *
     * @param file bin文件
     */
    private static void cpp2bin(File file) {
        boolean exec = processesUtil.exec(toBinCommand(file));

        if (!exec) throw new RuntimeException("cpp2bin 失败");
    }

    /**
     * cpp to bin 命令
     */
    private static String toBinCommand(File cppFile) {
        String outFilePath = outFilePath(cppFile, ".bin");
        outFilePath = outFilePath.replace(Constants.BAK_DIR_PATH, Constants.TMP_DIR_PATH);
        return ProcessesUtil.format(CFG_COMMAND, "-bin", outFilePath, cppFile.getAbsolutePath());
    }

    /**
     * bin to cpp 命令
     */
    private static String toTxtCommand(File binFile) {
        String outFilePath = outFilePath(binFile, ".cpp");
        return ProcessesUtil.format(CFG_COMMAND, "-txt", outFilePath, binFile.getAbsolutePath());
    }

    private static String outFilePath(File file, String suffix) {
        return file.getParentFile().getAbsolutePath() + File.separator + FileUtil.mainName(file) + suffix;
    }
}
