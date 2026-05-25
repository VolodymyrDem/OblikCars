module com.work.oblikcars {
    // --- JavaFX ---
    requires javafx.base;        // ⬅ треба для PropertyValueFactory і загалом JavaFX base
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;
    requires javafx.swing;

    // --- 3rd party / JDK ---
    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires net.synedra.validatorfx;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;
    requires eu.hansolo.tilesfx;
    requires org.kordamp.ikonli.fontawesome5;
    requires org.apache.poi.ooxml;
    requires java.sql;

    // --- opens для FXML/рефлексії ---
    // Контролери верхнього рівня
    opens com.work.oblikcars.pages to javafx.graphics, javafx.fxml;

    // Контролер вашого вікна реєстру пробігу
    opens com.work.oblikcars.pages.registers.mileage to javafx.graphics, javafx.fxml;

    // Інші існуючі контролери/вікна
    opens com.work.oblikcars.pages.handbooks.cars to javafx.fxml;
    opens com.work.oblikcars.pages.users to javafx.fxml, javafx.graphics;
    opens com.work.oblikcars.Factories to javafx.fxml;

    // Моделі/DTO, які читаються JavaFX від рефлексії (TableView, PropertyValueFactory тощо)
    opens com.work.oblikcars.model to javafx.base;

    // 👉 DTO вашого реєстру пробігу — саме це і потрібно для помилки з логів
    opens com.work.oblikcars.dto.Registers.MileageRegister to javafx.base;
    opens com.work.oblikcars.dto.Registers.CarRegister to javafx.base;
    opens com.work.oblikcars.dto.Registers.RegistrationRegister to javafx.base;

    // Інші DTO (як у вас було)
    opens com.work.oblikcars.dto.Journals.CarDepreciationJournal to javafx.base;
    opens com.work.oblikcars.dto.Journals.CarDisposalJournal to javafx.base;
    opens com.work.oblikcars.dto.Journals.ListJournal to javafx.base;
    opens com.work.oblikcars.dto.Journals.InspectionJournal to javafx.base;
    opens com.work.oblikcars.dto.Journals.InsuranceJournal to javafx.base;
    opens com.work.oblikcars.dto.Journals.InsuranceCaseJournal to javafx.base;
    opens com.work.oblikcars.dto.Journals.RegistrationJournal to javafx.base;
    opens com.work.oblikcars.dto.handbooks.CarHandbook to javafx.base;
    opens com.work.oblikcars.dto.Registers.CarDepreciationRegister to javafx.base;
    opens com.work.oblikcars.dto.Registers.CarDisposalRegister to javafx.base;
    opens com.work.oblikcars.dto.Registers.InspectionRegister to javafx.base;
    opens com.work.oblikcars.dto.Registers.InsuranseRegister to javafx.base;
    opens com.work.oblikcars.dto.Registers.ListRegister to javafx.base;

    // --- exports (як у вас було) ---
    exports com.work.oblikcars;
    exports com.work.oblikcars.Factories;

    // Експортуємо (не обов’язково для цієї помилки, але корисно для узгодженості з іншими DTO)
    exports com.work.oblikcars.dto.Journals.CarDepreciationJournal;
    exports com.work.oblikcars.dto.Journals.CarDisposalJournal;
    exports com.work.oblikcars.dto.Journals.ListJournal;
    exports com.work.oblikcars.dto.Journals.InspectionJournal;
    exports com.work.oblikcars.dto.Journals.InsuranceJournal;
    exports com.work.oblikcars.dto.Journals.InsuranceCaseJournal;
    exports com.work.oblikcars.dto.Journals.RegistrationJournal;
    exports com.work.oblikcars.dto.handbooks.CarHandbook;
    exports com.work.oblikcars.dto.Registers.CarDepreciationRegister;
    exports com.work.oblikcars.dto.Registers.CarDisposalRegister;
    exports com.work.oblikcars.dto.Registers.InspectionRegister;
    exports com.work.oblikcars.dto.Registers.InsuranseRegister;
    exports com.work.oblikcars.dto.Registers.ListRegister;

    // За бажанням можна також експортувати пакет реєстру пробігу:
    // (не потрібно для TableView, але стане в нагоді, якщо ці DTO використовуватимуться ззовні модуля)
    exports com.work.oblikcars.dto.Registers.MileageRegister;
}
