module com.example.lab {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires org.xerial.sqlitejdbc;


    opens lab to javafx.fxml;
    exports lab;
}