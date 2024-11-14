package cn.octopusyan.dmt.common.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.awt.datatransfer.*;
import java.io.IOException;

/**
 * 剪切板工具
 *
 * @author octopus_yan
 */
public class ClipUtil {
    //获取系统剪切板
    private static final Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
    private static final Logger log = LoggerFactory.getLogger(ClipUtil.class);

    public static void setClip(String data) {
        //构建String数据类型
        StringSelection stringSelection = new StringSelection(data);
        //添加文本到系统剪切板
        clipboard.setContents(stringSelection, null);
    }

    public static String getString() {
        Transferable content = clipboard.getContents(null);//从系统剪切板中获取数据
        if (content.isDataFlavorSupported(DataFlavor.stringFlavor)) {//判断是否为文本类型
            try {
                //从数据中获取文本值
                return (String) content.getTransferData(DataFlavor.stringFlavor);
            } catch (UnsupportedFlavorException | IOException e) {
                log.error("", e);
                return null;
            }
        }
        return null;
    }
}
