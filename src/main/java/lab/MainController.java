package lab;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;
import java.util.function.Consumer;

public class MainController {
    private final ArrayList<Consumer<Book>> editListeners = new ArrayList<>();
    private final ArrayList<Consumer<Book>> deleteListeners = new ArrayList<>();

    public ResourceBundle getResources() {
        return resources;
    }

    public URL getLocation() {
        return location;
    }

    public TextField getNameField() {
        return nameField;
    }

    public Pagination getPagination() {
        return pagination;
    }

    public TextField getPriceField() {
        return priceField;
    }

    public TableView<Book> getBooksTable() {
        return booksTable;
    }

    public Button getSaveBtn() {
        return saveBtn;
    }

    @FXML
    private ResourceBundle resources;

    @FXML
    private URL location;

    @FXML
    private TextField nameField;

    @FXML
    private Pagination pagination;

    @FXML
    private TextField priceField;

    @FXML
    private TableView<Book> booksTable;

    @FXML
    private TableColumn<Book, Integer> id;

    @FXML
    private TableColumn<Book, String> name;

    @FXML
    private TableColumn<Book, Float> price;

    @FXML
    private Button refreshBtn;

    @FXML
    private Button saveBtn;

    public Label getActionLabel() {
        return actionLabel;
    }

    @FXML
    private Label actionLabel;


    @FXML
    private SplitMenuButton addNewBtn;

    @FXML
    private MenuItem cloneBtnMenuItem;

    public SplitMenuButton getAddNewBtn() {
        return addNewBtn;
    }

    public MenuItem getCloneBtnMenuItem() {
        return cloneBtnMenuItem;
    }

    public void addEditButtonListener(Consumer<Book> listener) {
        this.editListeners.add(listener);
    }

    public void addDeleteButtonListener(Consumer<Book> listener) {
        this.deleteListeners.add(listener);
    }

    public SimpleBooleanProperty bookLoadedToEdit = new SimpleBooleanProperty(false);

    @FXML
    void initialize() {
        name.setCellValueFactory(new PropertyValueFactory<>("name"));
        id.setCellValueFactory(new PropertyValueFactory<>("id"));
        price.setCellValueFactory(new PropertyValueFactory<>("price"));

        nameField.disableProperty().bind(bookLoadedToEdit.not());
        priceField.disableProperty().bind(bookLoadedToEdit.not());
        saveBtn.disableProperty().bind(bookLoadedToEdit.not());




        booksTable.getItems().addAll(new Book(1, "Fizyka", 3.45f), new Book(2, "Statystyka Opisowa", 2.70f));

    }

    public Button getRefreshBtn() {
        return refreshBtn;
    }
}
