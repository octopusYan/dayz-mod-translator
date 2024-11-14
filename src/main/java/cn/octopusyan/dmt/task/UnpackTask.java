package cn.octopusyan.dmt.task;

import cn.octopusyan.dmt.model.WordItem;
import cn.octopusyan.dmt.task.base.BaseTask;
import cn.octopusyan.dmt.task.listener.DefaultTaskListener;
import cn.octopusyan.dmt.utils.PBOUtil;

import java.io.File;
import java.util.List;

/**
 * 解包PBO文件任务
 *
 * @author octopus_yan
 */
public class UnpackTask extends BaseTask<UnpackTask.UnpackListener> {

    private final File pboFile;

    public UnpackTask(File pboFile) {
        super("Unpack " + pboFile.getName());
        this.pboFile = pboFile;
    }

    @Override
    protected void task() throws Exception {
        // 解包
        String path = PBOUtil.unpack(pboFile);
        if (listener != null)
            listener.onUnpackOver(path);

        List<WordItem> wordItems = PBOUtil.findWord(path);
        if (listener != null)
            listener.onFindWordOver(wordItems);
    }

    /**
     * 解包监听
     *
     * @author octopus_yan
     */
    public abstract static class UnpackListener extends DefaultTaskListener {

        @Override
        protected void onSucceed() {

        }

        /**
         * 解包完成
         *
         * @param path 输出路径
         */
        public abstract void onUnpackOver(String path);

        /**
         * 查找可翻译文本
         *
         * @param wordItems 可翻译文本列表
         */
        public abstract void onFindWordOver(List<WordItem> wordItems);
    }
}
