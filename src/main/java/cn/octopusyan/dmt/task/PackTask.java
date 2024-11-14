package cn.octopusyan.dmt.task;

import cn.octopusyan.dmt.model.WordItem;
import cn.octopusyan.dmt.task.base.BaseTask;
import cn.octopusyan.dmt.task.listener.DefaultTaskListener;
import cn.octopusyan.dmt.utils.PBOUtil;

import java.io.File;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collector;
import java.util.stream.Collectors;

/**
 * 打包任务
 *
 * @author octopus_yan
 */
public class PackTask extends BaseTask<PackTask.PackListener> {

    private static final Function<List<WordItem>, List<WordItem>> sortFunc = items -> items.stream().sorted(Comparator.comparing(WordItem::getLines)).toList();
    private static final Collector<WordItem, Object, List<WordItem>> downstream = Collectors.collectingAndThen(Collectors.toList(), sortFunc);

    private final Map<File, List<WordItem>> wordFileMap;
    private final String unpackPath;

    public PackTask(List<WordItem> words, String unpackPath) {
        super("Pack");

        if (words == null)
            throw new RuntimeException("参数为null!");

        this.unpackPath = unpackPath;
        wordFileMap = words.stream().collect(Collectors.groupingBy(WordItem::getFile, downstream));
    }

    @Override
    protected void task() throws Exception {
        if (wordFileMap.isEmpty()) return;

        // 写入文件
        PBOUtil.writeWords(wordFileMap);
        if (listener != null) listener.onWriteOver();
        // 打包
        File packFile = PBOUtil.pack(unpackPath);
        if (listener != null) listener.onPackOver(packFile);
    }

    /**
     * 解包监听
     *
     * @author octopus_yan
     */
    public abstract static class PackListener extends DefaultTaskListener {

        public PackListener() {
            super(true);
            getProgress().setWidth(550);
        }

        @Override
        protected void onSucceed() {

        }

        /**
         * 写入完成
         */
        public abstract void onWriteOver();

        /**
         * 打包完成
         *
         * @param file 文件地址
         */
        public abstract void onPackOver(File file);
    }
}
