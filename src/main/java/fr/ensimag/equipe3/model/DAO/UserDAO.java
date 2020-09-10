package fr.ensimag.equipe3.model.DAO;

import fr.ensimag.equipe3.model.*;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

/**
 * Link between a user and the data base.
 */
public final class UserDAO implements Dao<User, String> {

    private final static String TABLE_NAME = "USERVV";
    private final static String TABLE_NAME_DRIVER = "CANDRIVE";
    private final static UserDAO instance = new UserDAO();
    public static UserDAO getInstance() {
        return instance;
    }

    private UserDAO() { }

    @Override
    public User get(String email) throws SQLException {
        String query = "SELECT * FROM " + TABLE_NAME + " WHERE email = ?";

        PreparedStatement stmt = ConnectionDB.getInstance().prepareStatement(query);
        stmt.setString(1, email);
        ResultSet result = stmt.executeQuery();

        if (!result.next()) {
            return null;
        }

        City city = new City(result.getString("city_name"),
                result.getInt("postal_code"));
        ArrayList<Vehicle> vehicles = getVehicules(email);

        User user = new User(result.getString("email"),
                result.getString("first_name"), result.getString("last_name"),
                Gender.valueOf(result.getString("gender")), city,
                result.getString("password"), result.getDouble("wallet"),
                vehicles);

        stmt.close();
        result.close();

        return user;
    }

    /**
     * Updated object user with user vehicule list.
     */
    private ArrayList<Vehicle> getVehicules(String email) throws SQLException {
        String query = "SELECT * FROM " + TABLE_NAME_DRIVER + " WHERE email = ?";

        PreparedStatement stmt = ConnectionDB.getInstance().prepareStatement(query);
        stmt.setString(1, email);
        ResultSet result = stmt.executeQuery();

        ArrayList<Vehicle> vehicles = new ArrayList<Vehicle>();

        while (result.next()) {
            String licensePlate = result.getString("LICENSE_PLATE");
            Vehicle vehicle = VehicleDAO.getInstance().get(licensePlate);
            vehicles.add(vehicle);
        }

        result.close();
        stmt.close();

        return vehicles;
    }

    @Override
    public void save(User user) throws SQLException {
        String query = "INSERT INTO " + TABLE_NAME + " VALUES(?, ?, ?, ?, ?, ?, ?, ?)";

        PreparedStatement stmt = ConnectionDB.getInstance().prepareStatement(query);
        stmt.setString(1, user.getEmail());
        stmt.setString(2, user.getFirstName());
        stmt.setString(3, user.getName());
        stmt.setString(4, user.getPassword());
        stmt.setDouble(5, user.getSolde());
        stmt.setString(6, user.getGender().name());
        stmt.setString(7, user.getCity().getName());
        stmt.setInt(8, user.getCity().getPostalCode());

        stmt.executeUpdate();

        stmt.close();
    }

    /**
     * The only one attribute the application can update is wallet.
     * @param user
     * @throws SQLException
     */
    @Override
    public void update(User user) throws SQLException {
        String query = "UPDATE " + TABLE_NAME + " SET \n" +
                "wallet = ?" +
                "WHERE email = ?";

        PreparedStatement stmt = ConnectionDB.getInstance().prepareStatement(query);
        stmt.setDouble(1, user.getSolde());
        stmt.setString(2, user.getEmail());

        stmt.executeUpdate();
        stmt.close();
    }

    /**
     * Adds in CANDRIVE the association between the user and the vehicle
     * @param user Current user
     * @param vehicle New vehicle
     */
    public void saveAdditionVehicle(User user, Vehicle vehicle) throws SQLException {
        if (!user.possessVehicle(vehicle)) {
            throw new IllegalArgumentException("The user " + user.getEmail()
                    + " does not possess " + vehicle.getLicensePlate() + ".");
        }

        String query = "INSERT INTO CANDRIVE VALUES (?, ?)";
        PreparedStatement stmt = ConnectionDB.getInstance().prepareStatement(query);
        stmt.setString(1, user.getEmail());
        stmt.setString(2, vehicle.getLicensePlate());

        stmt.executeUpdate();
        stmt.close();
    }

    /**
     * Deletes in CANDRIVE the association between the user and the vehicle
     * @param user Current user
     * @param vehicle Deleted Vehicle
     */
    public void deleteRemovedVehicle(User user, Vehicle vehicle) throws SQLException {
        if (user.possessVehicle(vehicle)) {
            throw new IllegalArgumentException("The user " + user.getEmail()
                    + " possesses the deleted vehicle " + vehicle.getLicensePlate() + ".");
        }

        String query = "DELETE FROM CANDRIVE WHERE LICENSE_PLATE = ? AND EMAIL = ?";
        PreparedStatement stmt = ConnectionDB.getInstance().prepareStatement(query);
        stmt.setString(1, vehicle.getLicensePlate());
        stmt.setString(2, user.getEmail());

        stmt.executeUpdate();
        stmt.close();
    }
}
