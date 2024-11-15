module cn.octopusyan.dmt {
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
    requires static lombok;
    requires atlantafx.base;
    requires com.fasterxml.jackson.annotation;
    requires com.fasterxml.jackson.databind;
    requires com.fasterxml.jackson.dataformat.yaml;
    requires java.prefs;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.ikonli.feather;

    exports cn.octopusyan.dmt;
    exports cn.octopusyan.dmt.model to com.fasterxml.jackson.databind;
    opens cn.octopusyan.dmt.model to javafx.base;
    opens cn.octopusyan.dmt.common.base to javafx.fxml;
    opens cn.octopusyan.dmt.controller to javafx.fxml;
    opens cn.octopusyan.dmt.controller.component to javafx.fxml;
    opens cn.octopusyan.dmt.controller.setup to javafx.fxml;
    opens cn.octopusyan.dmt.controller.help to javafx.fxml;
    opens cn.octopusyan.dmt.view.filemanager to javafx.fxml;
}