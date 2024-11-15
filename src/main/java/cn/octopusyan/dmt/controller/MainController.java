package cn.octopusyan.dmt.controller;

import atlantafx.base.controls.ModalPane;
import atlantafx.base.theme.Styles;
import atlantafx.base.theme.Theme;
import cn.octopusyan.dmt.common.base.BaseController;
import cn.octopusyan.dmt.common.config.Context;
import cn.octopusyan.dmt.common.manager.ConfigManager;
import cn.octopusyan.dmt.common.util.ClipUtil;
import cn.octopusyan.dmt.common.util.FxmlUtil;
import cn.octopusyan.dmt.common.util.ViewUtil;
import cn.octopusyan.dmt.controller.component.WordEditController;
import cn.octopusyan.dmt.model.WordItem;
import cn.octopusyan.dmt.task.TranslateTask;
import cn.octopusyan.dmt.view.ConsoleLog;
import cn.octopusyan.dmt.view.EditButtonTableCell;
import cn.octopusyan.dmt.view.alert.AlertUtil;
import cn.octopusyan.dmt.view.filemanager.DirectoryTree;
import cn.octopusyan.dmt.viewModel.MainViewModel;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

/**
 * 主界面
 *
 * @author octopus_yan
 */
public class MainController extends BaseController<MainViewModel> {
    private static final ConsoleLog consoleLog = ConsoleLog.getInstance(MainController.class);

    public Pane root;

    public Menu viewStyle;

    public static final ToggleGroup viewStyleGroup = new ToggleGroup();

    public StackPane mainView;
    //
    public VBox translateView;
    // 打开文件
    public StackPane selectFileBox;
    public VBox openFileView;
    public VBox dragFileView;
    public VBox loadFileView;
    // 工具栏
    public Button fileNameLabel;
    public Button translate;
    public ProgressBar translateProgress;
    // 文件树加载
    public DirectoryTree treeFileBox;
    public VBox loadWordBox;
    public ProgressBar loadWordProgressBar;
    // 翻译界面
    public Pane wordBox;
    public TableView<WordItem> wordTable;
    // 信息
    public TitledPane titledPane;
    public TextArea logArea;

    public final ModalPane modalPane = new ModalPane();
    // 文件选择器
    public static final FileChooser fileChooser = new FileChooser();

    static {
        var extFilter = new FileChooser.ExtensionFilter("PBO files (*.pbo)", "*.pbo");
        fileChooser.getExtensionFilters().add(extFilter);
    }

    @Override
    public Pane getRootPanel() {
        return root;
    }

    @Override
    public void initData() {
        // 信息
        ConsoleLog.init(logArea);
        // 界面样式
        List<MenuItem> list = ConfigManager.THEME_LIST.stream().map(this::createViewStyleItem).toList();
        viewStyle.getItems().addAll(list);

        fileNameLabel.textProperty().bind(viewModel.fileNameProperty());
    }

    @Override
    public void initViewStyle() {
        // 遮罩
        getRootPanel().getChildren().add(modalPane);
        modalPane.displayProperty().addListener((_, _, val) -> {
            if (!val) {
                modalPane.setAlignment(Pos.CENTER);
                modalPane.usePredefinedTransitionFactories(null);
            }
        });
    }

    @Override
    public void initViewAction() {

        // 文件拖拽
        setDragAction(mainView);

        // 复制单元格内容
        Context.sceneProperty.addListener(_ -> Context.sceneProperty.get()
                .getAccelerators()
                .put(new KeyCodeCombination(KeyCode.C, KeyCombination.CONTROL_ANY), () -> {
                    ObservableList<TablePosition> selectedCells = wordTable.getSelectionModel().getSelectedCells();
                    for (TablePosition tablePosition : selectedCells) {
                        Object cellData = tablePosition.getTableColumn().getCellData(tablePosition.getRow());
                        // 设置剪切板
                        ClipUtil.setClip(cellData.toString());
                    }
                })
        );

        // 日志栏清空
        logArea.contextMenuProperty().addListener(_ ->
                logArea.getContextMenu().getItems().addListener((ListChangeListener<MenuItem>) _ -> {
                    MenuItem clearLog = new MenuItem("清空");
                    clearLog.setOnAction(_ -> logArea.clear());
                    logArea.getContextMenu().getItems().add(clearLog);
                })
        );
    }

    /**
     * 设置文件拖拽效果
     */
    private void setDragAction(Pane fileBox) {

        // 进入
        fileBox.setOnDragEntered(dragEvent -> {
            var dragboard = dragEvent.getDragboard();
            if (dragboard.hasFiles() && isPboFile(dragboard.getFiles().getFirst())) {
                selectFileBox.setVisible(true);
                dragFileView.setVisible(true);
            }
        });

        //离开
        fileBox.setOnDragExited(_ -> {
            selectFileBox.setVisible(false);
            dragFileView.setVisible(false);
        });

        //
        fileBox.setOnDragOver(dragEvent -> {
            var dragboard = dragEvent.getDragboard();
            if (dragEvent.getGestureSource() != fileBox && dragboard.hasFiles()) {
                /* allow for both copying and moving, whatever user chooses */
                dragEvent.acceptTransferModes(TransferMode.COPY_OR_MOVE);
            }
            dragEvent.consume();
        });

        // 松手
        fileBox.setOnDragDropped(dragEvent -> {
            dragFileView.setVisible(false);

            var db = dragEvent.getDragboard();
            boolean success = false;
            var file = db.getFiles().getFirst();
            if (db.hasFiles() && isPboFile(file)) {
                selectFile(file);
                success = true;
            }
            /* 让源知道字符串是否已成功传输和使用 */
            dragEvent.setDropCompleted(success);

            dragEvent.consume();
        });
    }

    /**
     * 打开文件选择器
     */
    public void selectFile() {
        selectFile(fileChooser.showOpenDialog(getWindow()));
    }

    /**
     * 打开代理设置
     */
    public void openSetupProxy() {
        ViewUtil.openDecorated("网络代理设置", "setup/proxy-view");
    }

    /**
     * 打开翻译设置
     */
    public void openSetupTranslate() {
        ViewUtil.openDecorated("翻译设置", "setup/translate-view");
    }

    public void setFileName(String name) {
        fileNameLabel.setText("PBO文件：" + name);
    }

    /**
     * 显示加载PBO文件
     */
    public void onLoad() {
        // 展示加载
        selectFileBox.setVisible(true);
        loadFileView.setVisible(true);
        wordBox.getChildren().remove(wordTable);
    }

    /**
     * 显示解包完成
     *
     * @param path 解包路径
     */
    public void onUnpack(File path) {
        // 加载解包目录
        treeFileBox.loadRoot(path);
        // 隐藏文件选择
        loadFileView.setVisible(false);
        selectFileBox.setVisible(false);
        // 展示翻译界面
        translateView.setVisible(true);
        loadWordBox.setVisible(true);
    }

    /**
     * 加载可翻译文本数据
     *
     * @param wordItems 文本列表
     */
    public void onLoadWord(List<WordItem> wordItems) {
        loadWordBox.setVisible(false);
        wordBox.setVisible(true);
        bindWordTable(wordItems);
        translate.setDisable(false);
    }

    /**
     * 打包完成
     *
     * @param packFile 打包临时文件
     */
    public void onPackOver(File packFile) {
        // 选择文件保存地址
        fileChooser.setInitialFileName(packFile.getName());
        File file = fileChooser.showSaveDialog(getWindow());

        if (file == null)
            return;

        if (file.exists()) {
            //文件已存在，则删除覆盖文件
            FileUtils.deleteQuietly(file);
        }

        String exportFilePath = file.getAbsolutePath();
        consoleLog.info(STR."导出文件路径 => \{exportFilePath}");

        try {
            FileUtils.copyFile(packFile, file);
        } catch (IOException e) {
            consoleLog.error("保存文件失败！", e);
            Platform.runLater(() -> AlertUtil.getInstance(getWindow()).exception(e).content("保存文件失败！").show());
        }
    }

    public void startTranslate() {
        viewModel.startTranslate();
    }

    public void startPack() {
        viewModel.pack();
    }

    public void selectAllLog() {
        logArea.selectAll();
    }

    public void copyLog() {
        logArea.copy();
    }

    public void clearLog() {
        logArea.clear();
    }

    // ======================================{  }========================================

    /**
     * 打开文件
     */
    private void selectFile(File file) {
        viewModel.selectFile(file);
        viewModel.unpack();
    }

    /**
     * 绑定表格数据
     *
     * @param words 单词列表
     */
    private void bindWordTable(List<WordItem> words) {

        if (wordTable == null) {
            wordTable = new TableView<>();
            // 填满
            VBox.setVgrow(wordTable, Priority.ALWAYS);
            // 可编辑
            wordTable.setEditable(true);
            // 自动调整列宽
            wordTable.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);
            // 边框
            Styles.toggleStyleClass(wordTable, Styles.BORDERED);
//            // 行分隔
//            Styles.toggleStyleClass(wordTable, Styles.STRIPED);
            // 单元格选择模式而不是行选择
            wordTable.getSelectionModel().setCellSelectionEnabled(true);
            // 不允许选择多个单元格
            wordTable.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);

            // 创建列
            TableColumn<WordItem, String> colFile = createColumn("文件");
            colFile.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getFile().getName()));
            TableColumn<WordItem, String> colOriginal = createColumn("原文");
            colOriginal.setCellValueFactory(param -> param.getValue().getOriginalProperty());
            TableColumn<WordItem, String> colChinese = createColumn("中文翻译");
            colChinese.setCellValueFactory(param -> param.getValue().getChineseProperty());
            colChinese.setEditable(true);
            TableColumn<WordItem, WordItem> colIcon = new TableColumn<>("");
            colIcon.setSortable(false);
            colIcon.setCellFactory(EditButtonTableCell.forTableColumn(item -> {
                // 展示编辑弹窗
                try {
                    showModal(getEditWordPane(item), false);
                } catch (IOException e) {
                    consoleLog.error("加载布局失败", e);
                }
            }, item -> {
                // 翻译当前文本
                new TranslateTask(Collections.singletonList(item)).execute();
            }));

            wordTable.getColumns().add(colFile);
            wordTable.getColumns().add(colOriginal);
            wordTable.getColumns().add(colChinese);
            wordTable.getColumns().add(colIcon);
        }

        // 添加表数据
        wordTable.getItems().clear();
        wordBox.getChildren().addFirst(wordTable);
        wordTable.getItems().addAll(words);
    }

    /**
     * 表字段创建
     *
     * @param colName 列名
     * @return 列定义
     */
    private TableColumn<WordItem, String> createColumn(String colName) {
        TableColumn<WordItem, String> tableColumn = new TableColumn<>(colName);
        tableColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        tableColumn.setPrefWidth(150);
        tableColumn.setSortable(false);
        tableColumn.setEditable("中文翻译".equals(colName));
        return tableColumn;
    }

    private MenuItem createViewStyleItem(Theme theme) {
        var item = new RadioMenuItem(theme.getName());
        item.setSelected(theme.getName().equals(ConfigManager.themeName()));
        item.setToggleGroup(viewStyleGroup);
        item.setUserData(theme);
        item.selectedProperty().subscribe(selected -> {
            if (!selected) return;
            ConfigManager.theme(theme);
        });
        return item;
    }

    /**
     * 是否PBO文件
     */
    private boolean isPboFile(File file) {
        if (file == null) return false;
        return Pattern.compile(".*(.pbo)$").matcher(file.getName()).matches();
    }

    /**
     * 展示遮罩弹窗
     * <p>
     * 当{@code persistent}为{@code true}时，需要调用{@link #hideModal}才能关闭
     *
     * @param node       展示内容
     * @param persistent 是否持久性内容
     */
    private void showModal(Node node, boolean persistent) {
        modalPane.setAlignment(Pos.CENTER);
        modalPane.usePredefinedTransitionFactories(null);
        modalPane.show(node);
        modalPane.setPersistent(persistent);
    }

    /**
     * 关闭/隐藏遮罩弹窗
     */
    public void hideModal() {
        modalPane.hide(false);
        modalPane.setPersistent(false);
    }

    /**
     * 编辑
     */
    private Node getEditWordPane(WordItem data) throws IOException {
        FXMLLoader load = FxmlUtil.load("component/edit-view");
        Pane pane = load.load();
        WordEditController ctrl = load.getController();
        ctrl.bindData(data);
        return pane;
    }
}
