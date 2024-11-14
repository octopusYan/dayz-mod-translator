package cn.octopusyan.dmt.task;

import cn.octopusyan.dmt.common.manager.http.HttpUtil;
import cn.octopusyan.dmt.task.base.BaseTask;
import cn.octopusyan.dmt.task.listener.DefaultTaskListener;
import lombok.extern.slf4j.Slf4j;

/**
 * 代理检测任务
 *
 * @author octopus_yan
 */
@Slf4j
public class ProxyCheckTask extends BaseTask<DefaultTaskListener> {
    private final String checkUrl;

    public ProxyCheckTask(String checkUrl) {
        super(STR."ProxyCheck[\{checkUrl}]");
        this.checkUrl = checkUrl;
        this.updateProgress(0d, 1d);
    }

    @Override
    protected void task() throws Exception {
        String response = HttpUtil.getInstance().get(checkUrl, null, null);
        log.debug(STR."Proxy check response result => \n\{response}");
    }
}
