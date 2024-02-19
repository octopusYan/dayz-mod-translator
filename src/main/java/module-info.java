module cn.octopusyan.dayzmodtranslator {
    requires java.net.http;
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.swing;
    requires org.apache.commons.io;
    requires org.apache.commons.lang3;
    requires org.apache.commons.exec;
    requires org.slf4j;
    requires ch.qos.logback.core;
    requires ch.qos.logback.classic;
    requires com.alibaba.fastjson2;

    exports cn.octopusyan.dayzmodtranslator;
    opens cn.octopusyan.dayzmodtranslator to javafx.fxml;
    exports cn.octopusyan.dayzmodtranslator.base;
    opens cn.octopusyan.dayzmodtranslator.base to javafx.fxml;
    exports cn.octopusyan.dayzmodtranslator.controller;
    opens cn.octopusyan.dayzmodtranslator.controller to javafx.fxml;
    exports cn.octopusyan.dayzmodtranslator.manager.word;
    opens cn.octopusyan.dayzmodtranslator.manager.word to javafx.base;
}