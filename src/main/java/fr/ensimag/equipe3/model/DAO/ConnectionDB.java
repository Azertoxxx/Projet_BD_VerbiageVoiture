package fr.ensimag.equipe3.model.DAO;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import static java.lang.System.exit;

/**
 * Design Pattern Singleton
 * Only one connection to the data base.
 */
public final class ConnectionDB {
    private final static ConnectionDB instance = new ConnectionDB();
    private Connection connection;
    public static ConnectionDB getInstance() {
        return instance;
    }

    private ConnectionDB() {
        String url = "jdbc:oracle:thin:@oracle1.ensimag.fr:1521:oracle1";
        String user = "marullv";
        String passwd = "marullv";
        connect(url, user, passwd);
    }

    private void connect(String url, String user, String passwd) {
        try {
            Class.forName("oracle.jdbc.OracleDriver");
            connection = DriverManager.getConnection(url, user, passwd);
            connection.setAutoCommit(false);
        }
        catch (ClassNotFoundException e) {
            System.err.println("Driver not found.");
            e.printStackTrace();
            exit(1);
        }
        catch (SQLException e) {
            System.err.println("Connection to the database failed: " );
            e.printStackTrace();
            exit(1);
        }
    }

    /**
     * Wrapper for connection.prepareStatement(query).
     */
    public PreparedStatement prepareStatement(String sql) throws SQLException {
        return connection.prepareStatement(sql);
    }

    /**
     * Applies changes in the data base.
     * @throws SQLException
     */
    public void commit() throws SQLException {
        connection.commit();
    }

    /**
     * Dismisses changes from the last commit.
     * Uses in the case a transaction is not commited.
     * @throws SQLException
     */
    public void rollback() throws SQLException {
        connection.rollback();
    }
}
