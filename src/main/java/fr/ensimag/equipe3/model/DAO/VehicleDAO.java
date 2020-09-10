package fr.ensimag.equipe3.model.DAO;

import fr.ensimag.equipe3.model.Vehicle;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Link between a vehicle and the data base.
 */
public final class VehicleDAO implements Dao<Vehicle, String> {

    private final static VehicleDAO instance = new VehicleDAO();
    public static VehicleDAO getInstance() { return instance; }
    private final static String TABLE_NAME = "VEHICLE";
    private final static String TABLE_NAME_DRIVER = "CANDRIVE";

    private VehicleDAO() { }

    @Override
    public Vehicle get(String licensePlate) throws SQLException {
        String query = "SELECT * FROM " + TABLE_NAME + " WHERE LICENSE_PLATE = ?";

        PreparedStatement stmt = ConnectionDB.getInstance().prepareStatement(query);
        stmt.setString(1, licensePlate);
        ResultSet result = stmt.executeQuery();

        if (!result.next()) {
            return null;
        }

        Vehicle vehicle = new Vehicle(result.getString("LICENSE_PLATE"),
                result.getString("BRAND"), result.getString("MODEL"),
                result.getString("ENERGY_NAME"), result.getInt("FISCAL_POWER"),
                result.getInt("SEATS_NUMBER"));

        result.close();
        stmt.close();

        return vehicle;
    }

    @Override
    /**
     * Creates a new vehicle in the data base.
     * Only if it does not already exist.
     * @param vehicle New vehicle
     */
    public void save(Vehicle vehicle) throws SQLException {
        if (this.get(vehicle.getLicensePlate()) == null) {
            String query = "INSERT INTO " + TABLE_NAME + " VALUES(?, ?, ?, ?, ?, ?)";

            PreparedStatement stmt = ConnectionDB.getInstance().prepareStatement(query);
            stmt.setString(1, vehicle.getLicensePlate());
            stmt.setInt(2, vehicle.getFiscalPower());
            stmt.setInt(3, vehicle.getSeatsNumber());
            stmt.setString(4, vehicle.getEnergy());
            stmt.setString(5, vehicle.getModel());
            stmt.setString(6, vehicle.getBrand());

            stmt.executeUpdate();

            stmt.close();
        }
    }

    @Override
    public void update(Vehicle vehicle) throws SQLException {
        // Cannot be updated
    }

    /**
     * Deletes a vehicle only if no user can drive it.
     * @param vehicle Unused vehicle
     * @throws SQLException
     */
    public void delete(Vehicle vehicle) throws SQLException {
        String queryCandrive = "SELECT * FROM " + TABLE_NAME_DRIVER + " WHERE LICENSE_PLATE = ?";

        PreparedStatement stmt = ConnectionDB.getInstance().prepareStatement(queryCandrive);
        stmt.setString(1, vehicle.getLicensePlate());
        ResultSet result = stmt.executeQuery();

        if (!result.next()) {
            String queryDelete = "DELETE FROM " + TABLE_NAME + " WHERE LICENSE_PLATE = ?";
            PreparedStatement stmtDelete = ConnectionDB.getInstance().prepareStatement(queryDelete);
            stmtDelete.setString(1, vehicle.getLicensePlate());
            stmtDelete.executeUpdate();
            stmtDelete.close();
        }

        result.close();
        stmt.close();
    }
}
