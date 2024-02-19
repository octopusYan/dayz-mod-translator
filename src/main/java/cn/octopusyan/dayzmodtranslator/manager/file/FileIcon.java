package cn.octopusyan.dayzmodtranslator.manager.file;


import javafx.scene.canvas.Canvas;
import javafx.scene.image.PixelFormat;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritablePixelFormat;

import javax.swing.*;
import javax.swing.filechooser.FileSystemView;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.io.File;
import java.nio.IntBuffer;

/**
 * 文件图标
 *
 * @author octopus_yan@foxmail.com
 */
public class FileIcon {
    //设置图标
    public static Canvas getFileIconToNode(File file) {
        //获取系统文件的图标
        Image image = ((ImageIcon) FileSystemView.getFileSystemView().getSystemIcon(file)).getImage();
        //构建图片缓冲区，设定图片缓冲区的大小和背景，背景为透明
        BufferedImage bi = new BufferedImage(image.getWidth(null), image.getHeight(null), BufferedImage.BITMASK);
        //把图片画到图片缓冲区
        bi.getGraphics().drawImage(image, 0, 0, null);
        //将图片缓冲区的数据转换成int型数组
        int[] data = ((DataBufferInt) bi.getData().getDataBuffer()).getData();
        //获得写像素的格式模版
        WritablePixelFormat<IntBuffer> pixelFormat = PixelFormat.getIntArgbInstance();
        //新建javafx的画布
        Canvas canvas = new Canvas(bi.getWidth() + 2, bi.getHeight() + 2);
        //获取像素的写入器
        PixelWriter pixelWriter = canvas.getGraphicsContext2D().getPixelWriter();
        //根据写像素的格式模版把int型数组写到画布
        pixelWriter.setPixels(0, 0, bi.getWidth(), bi.getHeight(), pixelFormat, data, 0, bi.getWidth());
        //设置树节点的图标
        return canvas;

    }

    public static String getFileName(File file) {
        return FileSystemView.getFileSystemView().getSystemDisplayName(file);
    }
}
