module com.supervisor {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.net.http;
    requires org.json;
    requires java.ini.parser;
    requires com.opencsv;
    requires javafx.graphics;
    requires com.google.gson;
    requires com.fasterxml.jackson.databind;
    requires org.fxmisc.richtext;
    requires com.sun.jna;
    requires org.apache.commons.lang3;
    requires org.controlsfx.controls;
    requires json.simple;
    requires java.sql;


    exports LogicClasses;
    exports GlobalPackage;
    opens GlobalPackage to javafx.fxml;
    exports ControllerClasses;
    opens ControllerClasses to javafx.fxml;
    exports Model;
    opens Model to javafx.base, javafx.fxml;
    opens LogicClasses to javafx.fxml;
}