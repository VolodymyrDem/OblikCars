module com.work.oblikcars {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.work.oblikcars to javafx.fxml;
    exports com.work.oblikcars;
}