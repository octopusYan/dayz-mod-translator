package cn.octopusyan.dmt.task;

import cn.octopusyan.dmt.common.manager.ConfigManager;
import cn.octopusyan.dmt.common.manager.http.HttpUtil;
import cn.octopusyan.dmt.common.util.JsonUtil;
import cn.octopusyan.dmt.model.UpgradeConfig;
import cn.octopusyan.dmt.task.base.BaseTask;
import cn.octopusyan.dmt.task.listener.DefaultTaskListener;
import com.fasterxml.jackson.databind.JsonNode;
import org.apache.commons.lang3.StringUtils;

/**
 * 检查更新任务
 *
 * @author octopus_yan
 */
public class UpgradeTask extends BaseTask<UpgradeTask.UpgradeListener> {

    private final UpgradeConfig upgradeConfig = ConfigManager.upgradeConfig();

    protected UpgradeTask() {
        super("Check Update");
    }

    @Override
    protected void task() throws Exception {
        String responseStr = HttpUtil.getInstance().get(upgradeConfig.getReleaseApi(), null, null);
        JsonNode response = JsonUtil.parseJsonObject(responseStr);

        // TODO 校验返回内容
        String newVersion = response.get("tag_name").asText();

        if (listener != null)
            listener.onChecked(!StringUtils.equals(upgradeConfig.getVersion(), newVersion), newVersion);
    }

    /**
     * 检查更新监听默认实现
     *
     * @author octopus_yan
     */
    public abstract static class UpgradeListener extends DefaultTaskListener {

        public UpgradeListener() {
            super(true);
        }

        public abstract void onChecked(boolean hasUpgrade, String version);

        @Override
        protected void onSucceed() {
            // do nothing ...
        }
    }
}
