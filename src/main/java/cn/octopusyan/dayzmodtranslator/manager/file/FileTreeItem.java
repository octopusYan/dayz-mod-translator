package cn.octopusyan.dayzmodtranslator.manager.file;

import javafx.collections.ObservableList;
import javafx.scene.control.TreeItem;

import javax.swing.filechooser.FileSystemView;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * 文件目录
 *
 * @author octopus_yan@foxmail.com
 */
public class FileTreeItem extends TreeItem<String> {
    public static File ROOT_FILE = FileSystemView.getFileSystemView().getRoots()[0];

    //判断树节点是否被初始化，没有初始化为真
    private boolean notInitialized = true;

    private final File file;
    private final Function<File, File[]> supplier;

    public FileTreeItem(File file) {
        super(FileIcon.getFileName(file), FileIcon.getFileIconToNode(file));
        this.file = file;
        supplier = (File f) -> {
            if (((FileTreeItem) this.getParent()).getFile() == ROOT_FILE) {
                String name = FileIcon.getFileName(f);
                if (name.equals("网络") || name.equals("家庭组")) {
                    return new File[0];
                }
            }
            return f.listFiles();
        };
    }

    public FileTreeItem(File file, Function<File, File[]> supplier) {
        super(FileIcon.getFileName(file), FileIcon.getFileIconToNode(file));
        this.file = file;
        this.supplier = supplier;
    }


    //重写getchildren方法，让节点被展开时加载子目录
    @Override
    public ObservableList<TreeItem<String>> getChildren() {

        ObservableList<TreeItem<String>> children = super.getChildren();
        //没有加载子目录时，则加载子目录作为树节点的孩子
        if (this.notInitialized && this.isExpanded()) {

            this.notInitialized = false;    //设置没有初始化为假

            /*
             *判断树节点的文件是否是目录，
             *如果是目录，着把目录里面的所有的文件添加入树节点的孩子中。
             */
            if (this.getFile().isDirectory()) {
                List<FileTreeItem> fileList = new ArrayList<>();
                for (File f : supplier.apply(this.getFile())) {
                    if (f.isDirectory())
                        children.add(new FileTreeItem(f));
                    else
                        fileList.add(new FileTreeItem(f));
                }
                children.addAll(fileList);
            }
        }
        return children;
    }

    //重写叶子方法，如果该文件不是目录，则返回真
    @Override
    public boolean isLeaf() {

        return !file.isDirectory();
    }

    /**
     * @return the file
     */
    public File getFile() {
        return file;
    }


}
