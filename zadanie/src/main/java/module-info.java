module com.example.zadanie5 {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.example.zadanie5 to javafx.fxml;
    exports com.example.zadanie5;
}