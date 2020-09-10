package fr.ensimag.equipe3.model.DAO;

import fr.ensimag.equipe3.model.City;
import javafx.util.Pair;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Link between a city and the data base.
 */
public final class CityDAO implements Dao<City, Pair<String, Integer>> {

    private final static CityDAO instance = new CityDAO();
    public static CityDAO getInstance() {
        return instance;
    }
    private final static String TABLE_NAME = "CITY";

    private CityDAO() {}

    @Override
    public City get(Pair<String, Integer> id) throws SQLException {
        String query = "SELECT * FROM " + TABLE_NAME + " WHERE CITY_NAME = ? AND POSTAL_CODE = ?";

        PreparedStatement stmt = ConnectionDB.getInstance().prepareStatement(query);
        stmt.setString(1, id.getKey());
        stmt.setInt(2, id.getValue());
        ResultSet result = stmt.executeQuery();

        if (!result.next())
            return null;

        City city = new City(result.getString("CITY_NAME"),
                result.getInt("POSTAL_CODE"));

        result.close();
        stmt.close();

        return city;
    }

    @Override
    /**
     * Creates a new city in the data base.
     * Only if it does not already exist.
     * @param city New city
     */
    public void save(City city) throws SQLException {
        if (this.get(city.getPair()) == null) {
            String query = "INSERT INTO " + TABLE_NAME + " VALUES (?, ?)";

            PreparedStatement stmt = ConnectionDB.getInstance().prepareStatement(query);
            stmt.setString(1, city.getName());
            stmt.setInt(2, city.getPostalCode());

            stmt.executeUpdate();
            stmt.close();
        }
    }

    @Override
    public void update(City city) throws SQLException {
        // Cannot be updated
    }
}
