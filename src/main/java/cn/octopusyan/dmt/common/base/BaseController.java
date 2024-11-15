package cn.octopusyan.dmt.common.base;

import cn.octopusyan.dmt.Application;
import cn.octopusyan.dmt.common.config.Context;
import cn.octopusyan.dmt.common.util.FxmlUtil;
import cn.octopusyan.dmt.common.util.ViewUtil;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;

/**
 * 通用视图控制器基类
 *
 * @author octopus_yan@foxmail.com
 */
public abstract class BaseController<VM extends BaseViewModel> implements Initializable {
    protected final Logger logger = LoggerFactory.getLogger(this.getClass());
    protected final VM viewModel;

    public BaseController() {
        //初始化时保存当前Controller实例
        Context.getControllers().put(this.getClass().getSimpleName(), this);

        // view model
        VM vm = null;
        Type superclass = getClass().getGenericSuperclass();
        if (superclass instanceof ParameterizedType type) {
            Class<VM> clazz = (Class<VM>) type.getActualTypeArguments()[0];
            try {
                vm = clazz.getDeclaredConstructor().newInstance();
            } catch (Exception e) {
                logger.error("", e);
            }
        }
        viewModel = vm;
        viewModel.setController(this);
    }

    @FXML
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // 全局窗口拖拽
        if (dragWindow() && getRootPanel() != null) {
            // 窗口拖拽
            ViewUtil.bindDragged(getRootPanel());
        }

        // 初始化数据
        initData();

        // 初始化视图样式
        initViewStyle();

        // 初始化视图事件
        initViewAction();
    }

    /**
     * 窗口拖拽设置
     *
     * @return 是否启用
     */
    public boolean dragWindow() {
        return false;
    }

    /**
     * 获取根布局
     *
     * @return 根布局对象
     */
    public abstract Pane getRootPanel();

    /**
     * 获取根布局
     * <p> 搭配 {@link FxmlUtil#load(String)} 使用
     *
     * @return 根布局对象
     */
    protected String getRootFxml() {
        System.out.println(getClass().getSimpleName());
        return "";
    }

    public Stage getWindow() {
        try {
            return (Stage) getRootPanel().getScene().getWindow();
        } catch (Throwable _) {
            return Application.getPrimaryStage();
        }
    }

    /**
     * 初始化数据
     */
    public abstract void initData();

    /**
     * 视图样式
     */
    public void initViewStyle() {
    }

    /**
     * 视图事件
     */
    public abstract void initViewAction();

    private static List<Field> getAllField(Class<?> class1) {
        List<Field> list = new ArrayList<>();
        while (class1 != Object.class) {
            list.addAll(Arrays.stream(class1.getDeclaredFields()).toList());
            //获取父类
            class1 = class1.getSuperclass();
        }
        return list;
    }

    public void exit() {
        Platform.exit();
    }

    /**
     * 关闭窗口
     */
    public void onDestroy() {
        Stage stage = getWindow();
        stage.hide();
        stage.close();
    }
}
