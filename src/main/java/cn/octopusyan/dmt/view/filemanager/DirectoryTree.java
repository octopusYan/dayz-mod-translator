/* SPDX-License-Identifier: MIT */

package cn.octopusyan.dmt.view.filemanager;

import atlantafx.base.theme.Tweaks;
import cn.octopusyan.dmt.utils.FileUtil;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.image.ImageView;

import java.io.File;
import java.nio.file.Files;
import java.util.Comparator;

public final class DirectoryTree extends TreeView<File> {
    public static final FileIconRepository fileIcon = new FileIconRepository();

    // 文件夹在前
    static final Comparator<TreeItem<File>> FILE_TYPE_COMPARATOR = Comparator.comparing(
            item -> !Files.isDirectory(item.getValue().toPath())
    );

    public DirectoryTree() {
        super();

        getStyleClass().add(Tweaks.ALT_ICON);

        setCellFactory(_ -> new TreeCell<>() {
            @Override
            protected void updateItem(File item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    setText(item.getName());

                    var image = new ImageView(item.isDirectory() ?
                            FileIconRepository.FOLDER :
                            fileIcon.getByMimeType(FileUtil.getMimeType(item.toPath()))
                    );
                    image.setFitWidth(20);
                    image.setFitHeight(20);
                    setGraphic(image);
                }
            }
        });
    }

    public void loadRoot(File value) {
        var root = new TreeItem<>(value);
        root.setExpanded(true);
        setRoot(root);

        // scan file tree two levels deep for starters
        scan(root, 5);

        // scan deeper as the user navigates down the tree
        root.addEventHandler(TreeItem.branchExpandedEvent(), event -> {
            TreeItem parent = event.getTreeItem();
            parent.getChildren().forEach(child -> {
                var item = (TreeItem<File>) child;
                if (item.getChildren().isEmpty()) {
                    scan(item, 1);
                }
            });
        });
    }

    public static void scan(TreeItem<File> parent, int depth) {
        File[] files = parent.getValue().listFiles();
        depth--;

        if (files != null) {
            for (File f : files) {
                var item = new TreeItem<>(f);
                parent.getChildren().add(item);

                if (depth > 0) {
                    scan(item, depth);
                }
            }
            // 文件类型+名称排序
            parent.getChildren().sort(FILE_TYPE_COMPARATOR.thenComparing(TreeItem::getValue));
        }
    }
}
