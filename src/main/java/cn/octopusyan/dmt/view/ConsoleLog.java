package cn.octopusyan.dmt.view;

import cn.octopusyan.dmt.common.config.Context;
import javafx.application.Platform;
import javafx.scene.control.TextArea;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 模拟控制台输出
 *
 * @author octopus_yan
 */
public class ConsoleLog {
    public static final String format = "yyyy/MM/dd hh:mm:ss";
    private static final Logger log = LoggerFactory.getLogger(ConsoleLog.class);
    private static Logger markerLog;
    private static TextArea logArea;
    private final String tag;

    public static void init(TextArea logArea) {
        ConsoleLog.logArea = logArea;
    }

    private ConsoleLog(String tag) {
        this.tag = tag;
    }

    public static <T> ConsoleLog getInstance(Class<T> clazz) {
        markerLog = LoggerFactory.getLogger(clazz);
        return getInstance(clazz.getSimpleName());
    }

    public static ConsoleLog getInstance(String tag) {
        return new ConsoleLog(tag);
    }

    public static boolean isInit() {
        return log != null;
    }

    public void info(String message, Object... param) {
        printLog(tag, Level.INFO, message, param);
    }

    public void warning(String message, Object... param) {
        printLog(tag, Level.WARN, message, param);
    }

    public void debug(String message, Object... param) {
        if (!Context.isDebugMode()) return;
        printLog(tag, Level.DEBUG, message, param);
    }

    public void error(String message, Object... param) {
        printLog(tag, Level.ERROR, message, param);
    }

    public void error(String message, Throwable throwable) {
        markerLog.error(message, throwable);
        message = STR."\{message} \{throwable.getMessage()}";
        printLog(tag, Level.ERROR, message);
    }

    public void msg(String message, Object... params) {
        if (StringUtils.isEmpty(message) || !isInit()) return;
        message = format(message, params);
        message = resetConsoleColor(message);

        print(message);
    }

    final static DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public void printLog(String tag, Level level, String message, Object... params) {
        if (!isInit()) return;

        // 时间
        String time = LocalDateTime.now().format(formatter);
        // 级别
        String levelStr = level.code;
        // 消息
        message = format(message, params);

        // 拼接后输出
        String input = STR."\{time} \{levelStr} [\{tag}] - \{message.replace(tag, "")}";

        switch (level) {
            case WARN -> markerLog.warn(message);
            case DEBUG -> markerLog.debug(message);
//            case ERROR -> markerLog.error(message);
            default -> markerLog.info(message);
        }

        print(input);
    }

    private static void print(String message) {
        var msg = message + (message.endsWith("\n") ? "" : "\n");
        Platform.runLater(() -> {
            ConsoleLog.logArea.appendText(msg);
            // 滚动到底部
            ConsoleLog.logArea.setScrollTop(Double.MAX_VALUE);
        });
    }

//==========================================={ 私有方法 }===================================================

    private static String format(String msg, Object... params) {
        int i = 0;
        while (msg.contains("{}") && params != null) {
            msg = msg.replaceFirst("\\{}", String.valueOf(params[i++]).replace("\\", "\\\\"));
        }
        return msg;
    }

    /**
     * 处理控制台输出颜色
     *
     * @param msg 输出消息
     * @return 信息
     */
    private static String resetConsoleColor(String msg) {
        if (!msg.contains("\033[")) return msg;

        return msg.replaceAll("\\033\\[(\\d;)?(\\d+)m", "");
    }

//============================{ 枚举 }================================

    @Getter
    @RequiredArgsConstructor
    public enum Level {
        INFO("INFO", null),
        DEBUG("DEBUG", null),
        WARN("WARN", "-color-danger-emphasis"),
        ERROR("ERROR", "-color-danger-fg"),
        ;

        private final String code;
        private final String color;
    }
}
