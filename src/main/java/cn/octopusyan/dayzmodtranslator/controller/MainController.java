package cn.octopusyan.dayzmodtranslator.controller;

import cn.octopusyan.dayzmodtranslator.base.BaseController;
import cn.octopusyan.dayzmodtranslator.manager.PBOUtil;
import cn.octopusyan.dayzmodtranslator.manager.file.FileTreeItem;
import cn.octopusyan.dayzmodtranslator.manager.translate.TranslateUtil;
import cn.octopusyan.dayzmodtranslator.manager.word.WordCsvItem;
import cn.octopusyan.dayzmodtranslator.manager.word.WordItem;
import cn.octopusyan.dayzmodtranslator.util.AlertUtil;
import cn.octopusyan.dayzmodtranslator.util.ClipUtil;
import javafx.application.Platform;
import javafx.beans.property.StringProperty;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.*;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.input.*;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;
import java.util.regex.Pattern;

/**
 * 主控制器
 *
 * @author octopus_yan@foxmail.com
 */
public class MainController extends BaseController<VBox> {

    public VBox root;
    public MenuItem openFileSetupBtn;
    public MenuItem translateSetupBtn;
    public MenuItem proxySetupBtn;
    public Label filePath;
    public StackPane fileBox;
    public VBox openFileBox;
    public Button openFile;
    public VBox dragFileBox;
    public Label dragFileLabel;
    public VBox loadFileBox;
    public Label loadFileLabel;
    public ProgressBar loadFileProgressBar;
    public TreeView<String> treeFileBox;
    public StackPane wordBox;
    public TableView<WordItem> wordTableBox;
    public VBox wordMsgBox;
    public Label wordMsgLabel;
    public ProgressBar loadWordProgressBar;
    public Button translateWordBtn;
    public Button packBtn;
    private final PBOUtil pboUtil = PBOUtil.getInstance();
    private final TranslateUtil translateUtil = TranslateUtil.getInstance();

    /**
     * 翻译标志 用于停止翻译
     */
    private AtomicBoolean transTag;
    /**
     * 已翻译文本下标缓存
     */
    private Set<Integer> transNum;

    @Override
    public boolean dragWindow() {
        return false;
    }

    @Override
    public VBox getRootPanel() {
        return root;
    }

    @Override
    public String getRootFxml() {
        return "main-view";
    }

    /**
     * 初始化数据
     */
    @Override
    public void initData() {

        // 解包监听
        pboUtil.setOnUnpackListener(new PBOUtil.OnUnpackListener() {
            @Override
            public void onStart() {
                refreshWordBox();
            }

            @Override
            public void onUnpackSuccess(String unpackDirPath) {
                loadFileLabel.textProperty().setValue("加载完成，正在获取文件目录");
                // 展示解包文件内容
                logger.info("正在获取文件目录。。");
                showDirectory(new File(unpackDirPath));
                // 展示可翻译语句
                logger.info("正在查询待翻译文本目录。。");
                showTranslateWord();
            }

            @Override
            public void onUnpackError(String msg) {
                loadFileLabel.textProperty().setValue("打开文件失败");
                logger.info("打开文件失败: \n" + msg);
            }

            @Override
            public void onUnpackOver() {

            }
        });

        // 打包监听
        pboUtil.setOnPackListener(new PBOUtil.OnPackListener() {
            @Override
            public void onStart() {
                showLoading("正在打包pbo文件");
            }

            @Override
            public void onProgress(long current, long all) {
                showLoading(String.format("正在打包pbo文件(%d / %d)", current, all));
            }

            @Override
            public void onPackSuccess(File packFile) {
                // 选择文件保存地址
                FileChooser fileChooser = new FileChooser();
                FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("PBO files (*.pbo)", "*.pbo");
                fileChooser.getExtensionFilters().add(extFilter);
                File file = fileChooser.showSaveDialog(getWindow());
                if (file == null)
                    return;
                if (file.exists()) {
                    //文件已存在，则删除覆盖文件
                    FileUtils.deleteQuietly(file);
                }
                String exportFilePath = file.getAbsolutePath();
                logger.info("导出文件的路径 =>" + exportFilePath);

                try {
                    FileUtils.copyFile(packFile, file);
                } catch (IOException e) {
                    logger.error("保存文件失败！", e);

                    Platform.runLater(() -> AlertUtil.exception(e).content("保存文件失败！").show());
                }
            }

            @Override
            public void onPackError(String msg) {
                AlertUtil.error("保存文件失败！").show();
                logger.info("保存文件失败: \n" + msg);
            }

            @Override
            public void onPackOver() {
                stopLoading();
            }
        });

        // 获取待翻译文字
        pboUtil.setOnFindTransWordListener((words, isOver) -> {
            loadWordProgressBar.setVisible(false);

            if (words == null || words.isEmpty()) {
                if (isOver) {
                    wordMsgLabel.textProperty().set("未找到待翻译文本");
                }
            } else {
                // 展示翻译按钮
                translateWordBtn.setVisible(true);
                // 展示打包按钮
                packBtn.setVisible(true);

                // 绑定TableView
                boolean isCsvItem = (words.get(0) instanceof WordCsvItem);
                bindWordTable(words, isCsvItem);
            }
        });

    }

    /**
     * 视图样式
     */
    @Override
    public void initViewStyle() {
        wordMsgLabel.textProperty().setValue("请打开PBO文件");
    }

    /**
     * 视图事件
     */
    @Override
    public void initViewAction() {
        // 翻译设置
        translateSetupBtn.setOnAction(event -> open(SetupTranslateController.class, "翻译源设置"));
        // 代理设置
        proxySetupBtn.setOnAction(event -> open(SetupProxyController.class, "代理设置"));

        // 选择pbo文件
        EventHandler<ActionEvent> selectPboFileAction = actionEvent -> {
            // 文件选择器
            FileChooser fileChooser = new FileChooser();
            FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("PBO files (*.pbo)", "*.pbo");
            fileChooser.getExtensionFilters().add(extFilter);
            selectFile(fileChooser.showOpenDialog(getWindow()));
        };
        openFileSetupBtn.setOnAction(selectPboFileAction);
        openFile.setOnAction(selectPboFileAction);

        // 拖拽效果 start ---------------------
        fileBox.setOnDragEntered(dragEvent -> {
            Dragboard dragboard = dragEvent.getDragboard();
            if (dragboard.hasFiles() && isPboFile(dragboard.getFiles().get(0))) {
                disableBox();
                dragFileBox.setVisible(true);
            }
        });
        fileBox.setOnDragExited(dragEvent -> {
            if (!loadFileBox.isVisible()) {
                disableBox();
                openFileBox.setVisible(true);
            }
        });
        fileBox.setOnDragOver(dragEvent -> {
            Dragboard dragboard = dragEvent.getDragboard();
            if (dragEvent.getGestureSource() != fileBox && dragboard.hasFiles()) {
                /* allow for both copying and moving, whatever user chooses */
                dragEvent.acceptTransferModes(TransferMode.COPY_OR_MOVE);
            }
            dragEvent.consume();
        });
        fileBox.setOnDragDropped(dragEvent -> {
            disableBox();
            openFileBox.setVisible(true);

            Dragboard db = dragEvent.getDragboard();
            boolean success = false;
            File file = db.getFiles().get(0);
            if (db.hasFiles() && isPboFile(file)) {
                selectFile(file);
                success = true;
            }
            /* 让源知道字符串是否已成功传输和使用 */
            dragEvent.setDropCompleted(success);

            dragEvent.consume();
        });
        // 拖拽效果 end ---------------------

        // 翻译按钮
        translateWordBtn.setOnMouseClicked(mouseEvent -> {

            // 是否初次翻译
            if (transTag == null) {
                transNum = new HashSet<>();
                transTag = new AtomicBoolean(true);
                // 开始翻译
                startTranslate();
            } else {
                // 获取翻译列表
                ObservableList<WordItem> items = wordTableBox.getItems();
                // 未获取到翻译列表 或 翻译完成 则不做处理
                if (items == null || items.isEmpty() || transNum.size() == items.size())
                    return;

                // 设置翻译标识
                transTag.set(!transTag.get());

                if (Boolean.FALSE.equals(transTag.get())) {
                    stopTranslate();
                } else {
                    startTranslate();
                }
            }


        });

        // 打包按钮
        packBtn.setOnAction(event -> pboUtil.pack(wordTableBox.getItems()));

        // 复制文本
        getRootPanel().getScene().getAccelerators()
                .put(new KeyCodeCombination(KeyCode.C, KeyCombination.CONTROL_ANY), new Runnable() {
                    @Override
                    public void run() {
                        TablePosition tablePosition = wordTableBox.getSelectionModel().getSelectedCells().get(0);
                        Object cellData = tablePosition.getTableColumn().getCellData(tablePosition.getRow());
                        ClipUtil.setClip(String.valueOf(cellData));
                    }
                });
    }

    /**
     * 开始翻译
     */
    private void startTranslate() {
        // 获取翻译列表
        ObservableList<WordItem> items = wordTableBox.getItems();
        if (items == null || items.isEmpty()) return;

        // 开始/继续 翻译
        String label = translateWordBtn.getText().replaceAll("已暂停|一键翻译", "正在翻译");
        translateWordBtn.textProperty().setValue(label);

        // 禁用打包按钮
        packBtn.setDisable(true);

        boolean isCsvItem = (items.get(0) instanceof WordCsvItem);
        // 循环提交翻译任务
        for (int i = 0; i < items.size(); i++) {
            // 跳过已翻译文本
            if (transNum.contains(i)) continue;

            WordItem item = items.get(i);

            // 提交翻译任务
            int finalI = i;
            translateUtil.translate(finalI, item.getOriginal(), new TranslateUtil.OnTranslateListener() {
                @Override
                public void onTranslate(String result) {
                    // 防止多线程执行时停止不及时
                    if (Boolean.FALSE.equals(transTag.get())) {
                        return;
                    }

                    // 含有中文则不翻译
                    if (!containsChinese(item.getChinese()))
                        item.setChinese(result);

                    // 设置简中文本
                    if (isCsvItem) {
                        WordCsvItem csvItem = ((WordCsvItem) item);
                        if (!containsChinese(csvItem.getChineseSimp()))
                            csvItem.setChineseSimp(result);
                    }

                    // 设置翻译进度
                    transNum.add(finalI);
                    String label;
                    if (transNum.size() >= items.size()) {
                        label = "翻译完成(" + items.size() + ")";
                        transTag.set(false);
                        // 启用打包按钮
                        packBtn.setDisable(false);
                    } else {
                        label = "正在翻译(" + transNum.size() + "/" + items.size() + ")";
                    }
                    translateWordBtn.textProperty().setValue(label);
                }
            });
        }
    }

    /**
     * 停止翻译
     */
    private void stopTranslate() {
        // 清除未完成的翻译任务
        translateUtil.clear();
        // 设置翻译状态
        String label = translateWordBtn.getText().replace("正在翻译", "已暂停");
        translateWordBtn.textProperty().setValue(label);
        // 启用打包按钮
        packBtn.setDisable(false);
    }

    /**
     * 选择待汉化pbo文件
     * <p>TODO 多文件汉化
     *
     * @param file 待汉化文件
     */
    private void selectFile(File file) {
        if (file == null || !file.exists()) return;

        filePath.textProperty().set(file.getName());

        // 重置文件界面
        disableBox();
        loadFileBox.setVisible(true);
        // 重置翻译文本状态
        wordBox.getChildren().remove(wordTableBox);
        wordTableBox = null;

        loadFileLabel.textProperty().setValue("正在加载模组文件");
        pboUtil.unpack(file);
    }

    /**
     * 展示文件夹内容
     *
     * @param file 根目录
     */
    private void showDirectory(File file) {
        if (file == null || !file.exists() || !file.isDirectory()) {
            return;
        }
        disableBox();
        treeFileBox.setVisible(true);

        // 加载pbo文件目录
        FileTreeItem fileTreeItem = new FileTreeItem(file, File::listFiles);
        treeFileBox.setRoot(fileTreeItem);
        treeFileBox.setShowRoot(false);
    }

    /**
     * 展示待翻译语句
     */
    private void showTranslateWord() {
        wordMsgLabel.textProperty().setValue("正在获取可翻译文本");
        loadWordProgressBar.setVisible(true);

        pboUtil.startFindWord();
    }

    /**
     * 绑定表格数据
     *
     * @param words     单词列表
     * @param isCsvItem 是否csv
     */
    private void bindWordTable(List<WordItem> words, boolean isCsvItem) {
        if (wordTableBox == null) {
            wordTableBox = new TableView<>();
            wordBox.getChildren().add(wordTableBox);
            // 可编辑
            wordTableBox.setEditable(true);
            // 单元格选择模式而不是行选择
            wordTableBox.getSelectionModel().setCellSelectionEnabled(true);
            // 不允许选择多个单元格
            wordTableBox.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
            // 鼠标事件清空
            wordTableBox.addEventFilter(MouseEvent.MOUSE_PRESSED, event -> {
                if (event.isControlDown()) {
                    return;
                }

                if (wordTableBox.getEditingCell() == null) {
                    wordTableBox.getSelectionModel().clearSelection();
                }
            });

            // 创建列
            wordTableBox.getColumns().add(createColumn("原始文本", WordItem::originalProperty));
            wordTableBox.getColumns().add(createColumn("中文", WordItem::chineseProperty));

            if (isCsvItem) {
                wordTableBox.getColumns().add(createColumn("简体中文", WordCsvItem::chineseSimpProperty));
            }
        }

        // 添加表数据
        wordTableBox.getItems().addAll(words);
    }

    private <T extends WordItem> TableColumn<WordItem, String> createColumn(String colName, Function<T, StringProperty> colField) {
        TableColumn<WordItem, String> tableColumn = new TableColumn<>(colName);
        tableColumn.setCellValueFactory(features -> colField.apply((T) features.getValue()));
        tableColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        tableColumn.setPrefWidth(150);
        tableColumn.setSortable(false);
        tableColumn.setEditable(!"原始文本".equals(colName));
        return tableColumn;
    }

    private void disableBox() {
        openFileBox.setVisible(false);
        dragFileBox.setVisible(false);
        loadFileBox.setVisible(false);
        treeFileBox.setVisible(false);
    }

    private void refreshWordBox() {
        if (wordTableBox != null) {
            wordBox.getChildren().remove(wordTableBox);
            wordTableBox = null;
        }
        wordMsgLabel.textProperty().setValue("请打开pbo文件");
        loadWordProgressBar.setVisible(false);
        translateWordBtn.textProperty().setValue("一键翻译");
        translateWordBtn.setVisible(false);
        packBtn.setVisible(false);
    }

    private boolean isPboFile(File file) {
        if (file == null) return false;
        return Pattern.compile(".*(.pbo)$").matcher(file.getName()).matches();
    }

    /**
     * 给定字符串是否含有中文
     *
     * @param str 需要判断的字符串
     * @return 是否含有中文
     */
    private boolean containsChinese(String str) {
        return Pattern.compile("[\u4e00-\u9fa5]").matcher(str).find();
    }
}