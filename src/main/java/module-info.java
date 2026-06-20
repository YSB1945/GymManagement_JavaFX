module com.gymproject {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;

    opens com.gymproject.main        to javafx.graphics;
    opens com.gymproject.controllers to javafx.fxml;
    opens com.gymproject.models      to javafx.base;

    exports com.gymproject.main;
}