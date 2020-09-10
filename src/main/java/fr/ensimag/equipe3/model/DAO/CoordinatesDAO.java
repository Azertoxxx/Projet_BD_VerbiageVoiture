package fr.ensimag.equipe3.model.DAO;

import fr.ensimag.equipe3.model.City;
import fr.ensimag.equipe3.model.Coordinates;
import javafx.util.Pair;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Link between a coordinate and the data base.
 */
public final class CoordinatesDAO implements Dao<Coordinates, Pair<Double, Double>> {
    private final static CoordinatesDAO instance = new CoordinatesDAO();
    public static CoordinatesDAO getInstance() {
        return instance;
    }
    private final static String TABLE_NAME = "COORDINATES";

    private CoordinatesDAO() { }

    @Override
    public Coordinates get(Pair<Double, Double> id) throws SQLException {
        String query = "SELECT * FROM " + TABLE_NAME + " WHERE LATITUDE = ? AND LONGITUDE = ?";

        PreparedStatement stmt = ConnectionDB.getInstance().prepareStatement(query);
        stmt.setDouble(1, id.getKey());
        stmt.setDouble(2, id.getValue());
        ResultSet result = stmt.executeQuery();

        if (!result.next()) {
            return null;
        }

        City city = CityDAO.getInstance().get(new Pair<>(result.getString("CITY_NAME"),
                result.getInt("POSTAL_CODE")));

        Coordinates coor = new Coordinates(result.getDouble("LATITUDE"),
                result.getDouble("LONGITUDE"), city);

        result.close();
        stmt.close();

        return coor;
    }

    /**
     * Save the new coordinates in the data base.
     * If it is already in the data base, nothing happens.
     * @param coordinates
     * @throws SQLException
     */
    @Override
    public void save(Coordinates coordinates) throws SQLException {
        if (this.get(coordinates.getPair()) == null) {
            String query = "INSERT INTO " + TABLE_NAME + " VALUES(?, ?, ?, ?)";

            PreparedStatement stmt = ConnectionDB.getInstance().prepareStatement(query);
            stmt.setDouble(1, coordinates.getLatitude());
            stmt.setDouble(2, coordinates.getLongitude());
            stmt.setString(3, coordinates.getCity().getName());
            stmt.setInt(4, coordinates.getCity().getPostalCode());

            stmt.executeUpdate();
            stmt.close();
        }
    }

    @Override
    public void update(Coordinates coordinates) throws SQLException {
        // Cannot be updated
    }
}
