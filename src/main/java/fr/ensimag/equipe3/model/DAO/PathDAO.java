package fr.ensimag.equipe3.model.DAO;

import fr.ensimag.equipe3.model.City;
import fr.ensimag.equipe3.model.Path;
import fr.ensimag.equipe3.model.Section;
import fr.ensimag.equipe3.model.User;
import fr.ensimag.equipe3.util.Validator;
import javafx.util.Pair;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;

/**
 * Link between a path and the data base.
 */
public final class PathDAO implements Dao<Path, Integer>{

    private final static PathDAO instance = new PathDAO();

    private String tableName = "PATH";

    public static PathDAO getInstance() { return instance; }

    private PathDAO() {
    }

    private Path _path;

    @Override
    public Path get(Integer _idTrajet) throws SQLException {
        String query = "SELECT * FROM " + tableName + " WHERE ID_PATH = ?";

        PreparedStatement stmt = ConnectionDB.getInstance().prepareStatement(query);
        stmt.setInt(1, _idTrajet);
        ResultSet result = stmt.executeQuery();

        if (!result.next()) {
            return null;
        }

        Path path = new Path(result.getInt("ID_PATH"),
                UserDAO.getInstance().get(result.getString("email")),
                VehicleDAO.getInstance().get(result.getString("license_plate")),
                result.getInt("seats_number_at_start"),
                result.getDate("start_date"),
                Validator.stringToDuration(result.getString("hour")));

        ArrayList<Section> sections = this.getSections(path);
        path.addAllSections(sections);

        stmt.close();
        result.close();

        return path;
    }

    public ArrayList<Path> getPathsByCities(City startCity, City endCity) throws SQLException {
        String query = "SELECT P.ID_PATH IDP, S.ID_SECTION IDS FROM PATH P" +
                " INNER JOIN SECTION S ON P.ID_PATH = S.ID_PATH" +
                " INNER JOIN COORDINATES C ON S.LATITUDE_BEGIN = C.LATITUDE AND S.LONGITUDE_BEGIN = C.LONGITUDE" +
                " INNER JOIN CITY Ci ON C.CITY_NAME = Ci.CITY_NAME AND C.POSTAL_CODE = Ci.POSTAL_CODE" +
                " WHERE Ci.CITY_NAME = ?";
        PreparedStatement stmt = ConnectionDB.getInstance().prepareStatement(query);
        stmt.setString(1, startCity.getName());
        // We're going to handle postal code in the caller
        //stmt.setInt(2, startCity.getPostalCode());
        ResultSet result = stmt.executeQuery();

        ArrayList<Path> paths = new ArrayList<>();
        while (result.next()) {
            int idPath = result.getInt("IDP");
            Path path = get(idPath);
            /* Here we have a path that contains a section that starts with
               startCity. Now, we will check whether this path also contains
               a following section which ends with endCity (if not null)
             */
            if (endCity == null)
                paths.add(path);
            else {
                int idSection = result.getInt("IDS");
                for (int i = idSection; i < path.getNumberOfSections(); i++) {
                    Section s = path.getSection(i);
                    if (s.getEnd().equals(endCity)) {
                        paths.add(path);
                        break;
                    }
                }
            }
        }

        stmt.close();
        result.close();

        return paths;
    }

    public ArrayList<Path> getPathsByUser(User user) throws SQLException {
        String query = "SELECT * FROM " + tableName + " WHERE EMAIL = ?";

        PreparedStatement stmt = ConnectionDB.getInstance().prepareStatement(query);
        stmt.setString(1, user.getEmail());
        ResultSet result = stmt.executeQuery();

        ArrayList<Path> paths = new ArrayList<>();
        while (result.next()) {
            int idPath = result.getInt("ID_PATH");
            Path path = get(idPath);
            paths.add(path);
        }

        result.close();
        stmt.close();

        return paths;
    }


    /**
     * Updated object user with user vehicule list.
     */
    private ArrayList<Section> getSections(Path path) throws SQLException {
        String query = "SELECT * FROM SECTION WHERE ID_PATH = ?";

        PreparedStatement stmt = ConnectionDB.getInstance().prepareStatement(query);
        stmt.setInt(1, path.getIdPath());
        ResultSet result = stmt.executeQuery();

        ArrayList<Section> sections = new ArrayList<Section>();

        while (result.next()) {
            int idSection = result.getInt("ID_SECTION");
            Section section = SectionDAO.getInstance().get(new Pair<Integer, Integer>(path.getIdPath(), idSection));
            sections.add(section);
        }

        result.close();
        stmt.close();

        return sections;
    }

    public int getNextId() throws SQLException {
        String query = "SELECT max(ID_PATH) FROM " + tableName;
        PreparedStatement stmt = ConnectionDB.getInstance().prepareStatement(query);
        ResultSet result = stmt.executeQuery();

        if (!result.next())
            return 0;

        int nextId = result.getInt(1) + 1;

        result.close();
        stmt.close();

        return nextId;
    }

    @Override
    public void save(Path path) throws SQLException {
        String query = "INSERT INTO " + tableName + " VALUES(?, ?, ?, ?, ?, ?)";

        PreparedStatement stmt = ConnectionDB.getInstance().prepareStatement(query);
        stmt.setInt(1, getNextId());
        stmt.setString(2, path.getHour().toString());
        stmt.setInt(3, path.getNbSeats());
        stmt.setDate(4, path.getDate());
        stmt.setString(5, path.getVehicle().getLicensePlate());
        stmt.setString(6, path.getUser().getEmail());
        stmt.executeUpdate();
        stmt.close();

        /**
         * Here we need to put in the database each sections
         * of the path as well.
         */
        for (Section s : path.getSections()) {
            SectionDAO.getInstance().save(s);
        }
    }

    @Override
    public void update(Path path) throws SQLException {

    }
}
