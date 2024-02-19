package cn.octopusyan.dayzmodtranslator.util;

import javafx.beans.value.ChangeListener;
import javafx.scene.control.Tooltip;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.stage.Window;

/**
 * 提示工具
 *
 * @author octopus_yan@foxmail.com
 */
public class TooltipUtil {
    private static TooltipUtil util;
    private final Tooltip tooltip = new Tooltip();
    private Window owner;
    private ChangeListener<Number> xListener;
    private ChangeListener<Number> yListener;
    private boolean paneMove = false;

    private TooltipUtil(Window window) {
        this.owner = window;
        this.tooltip.styleProperty().set(
                "-fx-background-color: white;" +
                        "-fx-text-fill: grey;" +
                        "-fx-font-size: 12px;"
        );
    }

    public static TooltipUtil getInstance(Pane pane) {
        if (pane == null) return null;
        Window window = pane.getScene().getWindow();
        if (window == null) return null;

        if (util == null) {
            util = new TooltipUtil(window);
            // 窗口位置监听
            util.xListener = (observable, oldValue, newValue) -> {
                util.tooltip.setAnchorX(util.tooltip.getAnchorX() + (newValue.doubleValue() - oldValue.doubleValue()));
                util.paneMove = true;
            };
            util.yListener = (observable, oldValue, newValue) -> {
                util.tooltip.setAnchorY(util.tooltip.getAnchorY() + (newValue.doubleValue() - oldValue.doubleValue()));
                util.paneMove = true;
            };
            util.tooltip.focusedProperty().addListener((observable, oldValue, newValue) -> {
                if (!newValue) util.paneMove = false;
            });
            // 随窗口移动
            util.owner.xProperty().addListener(util.xListener);
            util.owner.yProperty().addListener(util.yListener);
        }

        if (!window.equals(util.owner)) {
            // 删除旧监听
            util.owner.xProperty().removeListener(util.xListener);
            util.owner.yProperty().removeListener(util.yListener);
            // 新窗口
            util.owner = window;
            // 随窗口移动
            util.owner.xProperty().addListener(util.xListener);
            util.owner.yProperty().addListener(util.yListener);
        }

        // 点击关闭
        pane.setOnMouseClicked(event -> {
            if (!util.paneMove) util.tooltip.hide();
            util.paneMove = false;
        });

        util.tooltip.hide();

        return util;
    }

    public void showProxyTypeTip(MouseEvent event) {
        tooltip.setText(
                "提示：XTCP 映射成功率并不高，具体取决于 NAT 设备的复杂度。\n" +
                        "TCP ：基础的 TCP 映射，适用于大多数服务，例如远程桌面、SSH、Minecraft、泰拉瑞亚等\n" +
                        "UDP ：基础的 UDP 映射，适用于域名解析、部分基于 UDP 协议的游戏等\n" +
                        "HTTP ：搭建网站专用映射，并通过 80 端口访问\n" +
                        "HTTPS ：带有 SSL 加密的网站映射，通过 443 端口访问，服务器需要支持 SSL\n" +
                        "XTCP ：客户端之间点对点 (P2P) 连接协议，流量不经过服务器，适合大流量传输的场景，需要两台设备之间都运行一个客户端\n" +
                        "STCP ：安全交换 TCP 连接协议，基于 TCP，访问此服务的用户也需要运行一个客户端，才能建立连接，流量由服务器转发"
        );
        show(event);
    }

    private void show(MouseEvent event) {

        if (tooltip.isShowing()) {
            tooltip.hide();
        } else {
            tooltip.show(owner);
            double mx = event.getScreenX();
            double my = event.getScreenY();
            double tw = tooltip.widthProperty().doubleValue();
            double th = tooltip.heightProperty().doubleValue();

            tooltip.setX(mx - tw / 2);
            tooltip.setY(my - th - 10);
        }
    }

    public void hide() {
        tooltip.hide();
    }

    public boolean isShowing() {
        return tooltip.isShowing();
    }
}
