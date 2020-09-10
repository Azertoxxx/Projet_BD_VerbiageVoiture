package fr.ensimag.equipe3.model.DAO;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

/**
 * Allows to know the possible vehicle energies.
 */
public final class EnergyDAO implements Dao<String, String> {
    private final static EnergyDAO instance = new EnergyDAO();
    public static EnergyDAO getInstance() {
        return instance;
    }
    private final static String TABLE_NAME = "ENERGY";

    private EnergyDAO() { }

    @Override
    public String get(String name) throws SQLException {
        String query = "SELECT * FROM " + TABLE_NAME + " WHERE ENERGY_NAME = ?";

        PreparedStatement stmt = ConnectionDB.getInstance().prepareStatement(query);
        stmt.setString(1, name);
        ResultSet result = stmt.executeQuery();

        /* Should never happen, means DB is broken */
        if (!result.next()) {
            return null;
        }

        String energyName = result.getString("ENERGY_NAME");

        result.close();
        stmt.close();

        return energyName;
    }

    public ArrayList<String> getAll() throws SQLException {
        String query = "SELECT * FROM " + TABLE_NAME;

        PreparedStatement stmt = ConnectionDB.getInstance().prepareStatement(query);
        ResultSet result = stmt.executeQuery();

        ArrayList<String> energies = new ArrayList<String>();
        while (result.next()) {
            energies.add(result.getString("ENERGY_NAME"));
        }

        result.close();
        stmt.close();

        return energies;
    }

    @Override
    public void save(String s) throws SQLException {
        // Cannot add an energy in the data base
    }

    @Override
    public void update(String s) throws SQLException {
        // Cannot be updated
    }
}
