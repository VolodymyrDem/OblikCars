module com.work.oblikcars {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires net.synedra.validatorfx;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;
    requires eu.hansolo.tilesfx;
    requires java.sql;
    requires org.apache.poi.ooxml;
    requires javafx.swing;
    requires org.kordamp.ikonli.fontawesome5;

    opens com.work.oblikcars.pages to javafx.graphics, javafx.fxml;
    opens com.work.oblikcars.model to javafx.base;
    exports com.work.oblikcars;
    exports com.work.oblikcars.Factories;
    opens com.work.oblikcars.Factories to javafx.fxml;
    opens com.work.oblikcars.pages.users to javafx.fxml, javafx.graphics;
}