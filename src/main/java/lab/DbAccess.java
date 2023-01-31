package lab;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.CompletableFuture;

public class DbAccess {
    private static DbAccess instance = null;
    private final static String url = "jdbc:sqlite:BAZA_BOOKS.db3";
    private final static String username = "root";
    private final static String password = "";

    private DbAccess() {

    }

    public static DbAccess getInstance() {
        if (instance == null) {
            instance = new DbAccess();
            try {
                Class.forName("org.sqlite.JDBC");
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
        return instance;
    }

    public CompletableFuture<ResultSet> findAllBooks() {
        CompletableFuture<ResultSet> future = new CompletableFuture<>();
        CompletableFuture.runAsync(() -> {
            try (Connection con = DriverManager.getConnection(url, username, password)) {
                var s = con.createStatement();
                var result = s.executeQuery("SELECT * FROM books");
                future.complete(result);
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
            future.complete(null);
        });
        return future;
    }

    public CompletableFuture<ResultSet> findRangeOfAllBooks(int from, int length) {
        CompletableFuture<ResultSet> future = new CompletableFuture<>();
        CompletableFuture.runAsync(() -> {
            try (Connection con = DriverManager.getConnection(url, username, password)) {
                var s = con.prepareStatement("SELECT * FROM books LIMIT ?, ?");
                s.setInt(1, from - 1);
                s.setInt(2, length);
                var result = s.executeQuery();

                future.complete(result);
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
            future.complete(null);
        });
        return future;
    }

    public CompletableFuture<ResultSet> findBooksByName(String name) {
        CompletableFuture<ResultSet> future = new CompletableFuture<>();
        CompletableFuture.runAsync(() -> {
            try (Connection con = DriverManager.getConnection(url, username, password)) {
                var s = con.prepareStatement("SELECT * FROM books WHERE name=?");
                s.setString(1, name);
                var result = s.executeQuery();
                future.complete(result);
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
            future.complete(null);
        });
        return future;
    }

    public CompletableFuture<ResultSet> findBooksById(Integer id) {
        CompletableFuture<ResultSet> future = new CompletableFuture<>();
        CompletableFuture.runAsync(() -> {
            try (Connection con = DriverManager.getConnection(url, username, password)) {
                var s = con.prepareStatement("SELECT * FROM books WHERE id=?");
                s.setInt(1, id);
                var result = s.executeQuery();
                future.complete(result);
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
            future.complete(null);
        });
        return future;
    }

    public CompletableFuture<Integer> updateBookName(Integer id, String name) {
        CompletableFuture<Integer> future = new CompletableFuture<>();
        CompletableFuture.runAsync(() -> {
            try (Connection con = DriverManager.getConnection(url, username, password)) {
                var s = con.prepareStatement("UPDATE books SET name=? WHERE id=?");
                s.setString(1, name);
                s.setInt(2, id);
                var result = s.executeUpdate();
                future.complete(result);
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
            future.complete(null);
        });
        return future;
    }

    public CompletableFuture<Integer> updateBookPrice(Integer id, Float newPrice) {
        CompletableFuture<Integer> future = new CompletableFuture<>();
        CompletableFuture.runAsync(() -> {
            try (Connection con = DriverManager.getConnection(url, username, password)) {
                var s = con.prepareStatement("UPDATE books SET price=? WHERE id=?");
                s.setFloat(1, newPrice);
                s.setInt(2, id);
                var result = s.executeUpdate();
                future.complete(result);
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
            future.complete(null);
        });
        return future;
    }

    public CompletableFuture<Integer> removeBook(Integer id) {
        CompletableFuture<Integer> future = new CompletableFuture<>();
        CompletableFuture.runAsync(() -> {
            try (Connection con = DriverManager.getConnection(url, username, password)) {
                var s = con.prepareStatement("DELETE FROM books WHERE id=?");
                s.setInt(1, id);
                var result = s.executeUpdate();
                future.complete(result);
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
            future.complete(null);
        });
        return future;
    }

    public CompletableFuture<Integer> insertBook(Book p) {
        CompletableFuture<Integer> future = new CompletableFuture<>();
        CompletableFuture.runAsync(() -> {
            try (Connection con = DriverManager.getConnection(url, username, password)) {
                var s = con.prepareStatement("INSERT INTO books (name, price) VALUES (?,?)");
                s.setString(1, p.getName());
                s.setFloat(2, p.getPrice());
                var result = s.executeUpdate();
                future.complete(result);
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
            future.complete(null);
        });
        return future;
    }

}
