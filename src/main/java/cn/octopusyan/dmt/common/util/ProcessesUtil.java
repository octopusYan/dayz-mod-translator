package cn.octopusyan.dmt.common.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.exec.*;

import java.io.File;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * 命令工具类
 *
 * @author octopus_yan@foxmail.com
 */
@Slf4j
public class ProcessesUtil {
    private static final String NEW_LINE = System.lineSeparator();
    public static final int[] EXIT_VALUES = {0, 1};

    private final DefaultExecutor executor;
    private final ShutdownHookProcessDestroyer processDestroyer = new ShutdownHookProcessDestroyer();
    private OnExecuteListener listener;
    private CommandLine commandLine;

    private static final Set<ProcessesUtil> set = new HashSet<>();

    /**
     * Prevent construction.
     */
    private ProcessesUtil(String workingDirectory) {
        this(new File(workingDirectory));
    }

    private ProcessesUtil(File workingDirectory) {
        LogOutputStream logout = new LogOutputStream() {
            @Override
            protected void processLine(String line, int logLevel) {
                if (listener != null)
                    listener.onExecute(line + NEW_LINE);
            }
        };
        PumpStreamHandler streamHandler = new PumpStreamHandler(logout, logout);
        executor = DefaultExecutor.builder()
                .setExecuteStreamHandler(streamHandler)
                .setWorkingDirectory(workingDirectory)
                .get();
        executor.setExitValues(EXIT_VALUES);
        executor.setProcessDestroyer(processDestroyer);
    }

    public static ProcessesUtil init(String workingDirectory) {
        return init(new File(workingDirectory));
    }

    public static ProcessesUtil init(File workingDirectory) {
        ProcessesUtil util = new ProcessesUtil(workingDirectory);
        set.add(util);
        return util;
    }

    /**
     * 转命令
     *
     * @param command 命令模板
     * @param params  参数
     * @return 命令
     */
    public static String format(String command, Object... params) {

        int i = 0;
        while (command.contains("{}") && params != null) {
            String param = String.valueOf(params[i++]);

            if (param.contains(" "))
                param = STR."\"\{param}\"";

            command = command.replaceFirst("\\{}", param.replace("\\", "\\\\"));
        }
        return command;
    }

    public boolean exec(String command) {
        commandLine = CommandLine.parse(command);
        try {
            int execute = executor.execute(commandLine);
            return Arrays.stream(EXIT_VALUES).anyMatch(item -> item == execute);
        } catch (Exception e) {
            log.error("exec error", e);
            return false;
        }
    }

    public void exec(String command, OnExecuteListener listener) {
        this.listener = listener;
        commandLine = CommandLine.parse(command);
        DefaultExecuteResultHandler handler = new DefaultExecuteResultHandler() {
            @Override
            public void onProcessComplete(int exitValue) {
                if (listener != null) {
                    listener.onExecuteSuccess(Arrays.stream(EXIT_VALUES).noneMatch(item -> item == exitValue));
                }
            }

            @Override
            public void onProcessFailed(ExecuteException e) {
                if (listener != null) {
                    listener.onExecuteError(e);
                }
            }
        };
        try {
            executor.execute(commandLine, handler);
        } catch (Exception e) {
            if (listener != null) listener.onExecuteError(e);
        }
    }

    public void destroy() {
        if (processDestroyer.isEmpty()) return;
        processDestroyer.run();
    }

    public boolean isRunning() {
        return !processDestroyer.isEmpty();
    }

    public static void destroyAll() {
        set.forEach(ProcessesUtil::destroy);
    }

    public interface OnExecuteListener {
        void onExecute(String msg);

        default void onExecuteSuccess(boolean success) {
        }

        default void onExecuteError(Exception e) {
        }
    }
}
