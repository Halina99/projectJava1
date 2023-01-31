package lab;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Pagination;
import javafx.scene.control.TableView;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Objects;
import java.util.concurrent.ExecutionException;

public class App extends Application {

    private static lab.MainController controller;
    private static Book currentlyEditedBook = null;
    private static Boolean addingNewBook = false;
    private static Pagination pagination = null;
    private static TableView<Book> table = null;
    private static final SimpleIntegerProperty currentPage = new SimpleIntegerProperty(1);
    private static final ObservableList<Book> allBooks = FXCollections.observableArrayList();
    private static final Integer PER_PAGE = 5;

    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource("table-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 800, 400);
        stage.setTitle("Sklep książek");
        stage.setScene(scene);
        stage.show();

        controller = fxmlLoader.getController();
        pagination = controller.getPagination();
        table = controller.getBooksTable();

        loadAllBooks();

        controller.addEditButtonListener(p -> {
            System.out.println("edit " + p.getName());
            onBookEditStartRequest(p);
        });
        controller.addDeleteButtonListener(p -> {
            System.out.println("delete " + p.getName());
            onBookDeleteRequest(p);
        });
        controller.getSaveBtn().setOnAction(clicked -> {
            onBookEditEndRequest(currentlyEditedBook);
        });
        controller.getAddNewBtn().setOnAction(clicked -> {
            System.out.println("Add new!");
            onBookEditStartRequest(new Book(), true);
        });

        currentPage.addListener((observableValue, oldValue, newValue) -> {
            pagination.setCurrentPageIndex((Integer) newValue);
        });

        controller.getRefreshBtn().setOnAction(clicked -> {
            loadAllBooks();
        });

        allBooks.addListener((ListChangeListener<Book>) change -> {
            Platform.runLater(() -> {
                System.out.println("Change page count");
                pagination.setPageCount((int) Math.ceil(change.getList().size() / (double) PER_PAGE));
                currentPage.set(Math.min(pagination.getCurrentPageIndex(), pagination.getPageCount()));
                while (change.next()) {
                    if (change.wasRemoved()) {
//                handle removal
                        change.getRemoved().forEach(v -> {
                            table.getItems().removeIf(p -> Objects.equals(p.getId(), v.getId()));
                        });
                        table.refresh();
                    }
                    if (change.wasAdded()) {
//                handle adding
                        int pageBefore = pagination.getCurrentPageIndex();
                        Platform.runLater(()-> {
                            currentPage.set(pageBefore);
                        });
                    }
                }
            });
        });

        pagination.setPageFactory(App::createPage);

    }

    private static void loadAllBooks() {
        DbAccess.getInstance().findAllBooks().thenAccept(resultSet -> {
            ObservableList<Book> items = allBooks;
            items.clear();

            while (true) {
                try {
                    if (!resultSet.next()) break;

                    items.add(new Book(
                            resultSet.getInt("id"),
                            resultSet.getString("name"),
                            resultSet.getFloat("price")
                    ));
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }

            Platform.runLater(() -> currentPage.set(0));

        });
    }

    private static Node createPage(int n) {
        var items = table.getItems();

        items.clear();
        items.addAll(allBooks.subList(
                Math.min(Math.max((n) * PER_PAGE, 0), allBooks.size()),
                Math.min(Math.max((n + 1) * PER_PAGE, 0), allBooks.size())
        ));

        System.out.println(allBooks);
        System.out.println(items);

        return table;
    }

    public static void onBookEditStartRequest(Book p) {
        onBookEditStartRequest(p, false);
    }

    public static void onBookEditStartRequest(Book p, Boolean isNew) {
        currentlyEditedBook = p;
        addingNewBook = isNew;
        if (p == null)
            currentlyEditedBook = new Book();
        controller.bookLoadedToEdit.set(true);
        if (!addingNewBook) {
            controller.getNameField().textProperty().set(currentlyEditedBook.getName());
            controller.getPriceField().textProperty().set(currentlyEditedBook.getPrice().toString());
            controller.getActionLabel().textProperty().bind(controller.getNameField().textProperty().concat(" - editing ").concat(p == null ? "" : p.getId()));
        } else {
            controller.getNameField().textProperty().set("");
            controller.getPriceField().textProperty().set("");
            controller.getActionLabel().textProperty().bind(controller.getNameField().textProperty().concat(" - adding"));
        }
    }

    public static void onBookEditEndRequest(Book p) {

        if (addingNewBook) {
            try {
                String name = controller.getNameField().getText();
                if (name == null || name.isBlank())
                    throw new Exception("Nie podano nazwy");
                String s = controller.getPriceField().getText();
                if (s.isBlank())
                    throw new Exception("Nie podano ceny");
                Float price = Float.valueOf(s);

                currentlyEditedBook.setName(name);
                currentlyEditedBook.setPrice(price);


                if (DbAccess.getInstance().insertBook(currentlyEditedBook).get() != 1)
                    throw new Exception("Książka nie została dodana");

                DbAccess.getInstance().findBooksByName(currentlyEditedBook.getName()).thenAccept(v -> {
                    allBooks.add((Book) v);
                });

            } catch (NumberFormatException e) {
                e.printStackTrace();
                new Alert(Alert.AlertType.ERROR, "Nie można dodać książki, błąd: Zły format ceny.", ButtonType.OK).show();
                return;
            } catch (Exception e) {
                e.printStackTrace();
                new Alert(Alert.AlertType.ERROR, "Nie można dodać książki, błąd: " + e.getLocalizedMessage(), ButtonType.OK).show();

                return;
            }
        } else {
//            edited end
            try {
                String name = controller.getNameField().getText();
                if (name == null || name.isBlank())
                    throw new Exception("Nie podano nazwy");
                String s = controller.getPriceField().getText();
                if (s.isBlank())
                    throw new Exception("Nie podano ceny");
                Float price = Float.valueOf(s);

                if (!currentlyEditedBook.getName().equals(name)) {
                    var result = DbAccess.getInstance().updateBookName(currentlyEditedBook.getId(), name).get();
                    if (result == 0)
                        throw new Exception("Nothing was changed");
                    currentlyEditedBook.setName(name);
                }
                if (!currentlyEditedBook.getPrice().equals(price)) {
                    var result = DbAccess.getInstance().updateBookPrice(currentlyEditedBook.getId(), price).get();
                    if (result == 0)
                        throw new Exception("Nothing was changed");
                    currentlyEditedBook.setPrice(price);
                }

                Book bookCopy = currentlyEditedBook;

                Platform.runLater(() -> {
                    System.out.println(bookCopy);
                    allBooks.stream().filter(v -> Objects.equals(v.getId(), bookCopy.getId())).findAny().ifPresent(book -> {
                        book.setPrice(bookCopy.getPrice());
                        book.setName(bookCopy.getName());
                    });
                    controller.getBooksTable().getItems().stream().filter(v -> Objects.equals(v.getId(), bookCopy.getId())).findAny().ifPresent(book -> {
                        book.setPrice(bookCopy.getPrice());
                        book.setName(bookCopy.getName());
                    });
                });

            } catch (Exception e) {
                e.printStackTrace();
            }

        }

//        reset:
        controller.getNameField().textProperty().set("");
        controller.getPriceField().textProperty().set("");
        controller.getActionLabel().textProperty().unbind();
        controller.getActionLabel().textProperty().set("");
        controller.bookLoadedToEdit.set(false);
        currentlyEditedBook = null;
        addingNewBook = false;
    }

    public static void onBookDeleteRequest(Book p) {
        if (currentlyEditedBook == p) {
            Alert a = new Alert(Alert.AlertType.WARNING, "Ta książka jest obecnie edytowana, jeśli ją usuniesz, zmiany zostaną utracone! Czy chcesz kontynuować?", ButtonType.OK, ButtonType.CANCEL);
            var resp = a.showAndWait();

            if (!Objects.equals(resp.orElse(ButtonType.CANCEL), ButtonType.OK))
                return;
        }

        if (deleteBook(p) == null)
            new Alert(Alert.AlertType.WARNING, "Nie można usunąć książki", ButtonType.OK).show();
    }

    public static Integer deleteBook(Book p) {
        try {
            return DbAccess.getInstance().removeBook(p.getId()).get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        return null;
    }


    public static void main(String[] args) {
        launch();
    }
}