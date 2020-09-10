package fr.ensimag.equipe3.model.DAO;

import fr.ensimag.equipe3.controller.ViewController;
import fr.ensimag.equipe3.model.*;
import javafx.util.Pair;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;

/**
 * Link between a section and the data base.
 */
public final class SectionDAO implements Dao<Section, Pair<Integer, Integer>> {
    private final static String TABLE_NAME = "SECTION";
    private final static SectionDAO instance = new SectionDAO();
    public static SectionDAO getInstance() {
        return instance;
    }
    private SectionDAO() { }

    @Override
    public Section get(Pair<Integer, Integer> ids) throws SQLException {
        int idPath = ids.getKey();
        int idSection = ids.getValue();
        String query = "SELECT * FROM " + TABLE_NAME + " WHERE ID_PATH = ? AND ID_SECTION = ?";

        PreparedStatement stmt = ConnectionDB.getInstance().prepareStatement(query);
        stmt.setInt(1, idPath);
        stmt.setInt(2, idSection);
        ResultSet result = stmt.executeQuery();

        if (!result.next()) {
            return null;
        }

        double startLat = result.getDouble("LATITUDE_BEGIN");
        double startLong = result.getDouble("LONGITUDE_BEGIN");
        Coordinates startPoint = CoordinatesDAO.getInstance().get(new Pair<>(startLat, startLong));
        double endLat = result.getDouble("LATITUDE_END");
        double endLong = result.getDouble("LONGITUDE_END");
        Coordinates endPoint = CoordinatesDAO.getInstance().get(new Pair<>(endLat, endLong));

        Section section = new Section(idPath, idSection, result.getDouble("distance"),
                Duration.ofMinutes(result.getLong("ESTIMATED_TIME")),
                Duration.ofMinutes(result.getLong("WAITING_TIME")),
                startPoint, endPoint);

        stmt.close();
        result.close();

        return section;
    }

    public ArrayList<Course> getCoursesBySection(Section section) throws SQLException {
        String query = "SELECT * FROM ISRESERVED WHERE ID_SECTION = ? AND ID_PATH = ?";

        PreparedStatement stmt = ConnectionDB.getInstance().prepareStatement(query);
        stmt.setInt(1, section.getIdSection());
        stmt.setInt(2, section.getIdPath());
        ResultSet result = stmt.executeQuery();

        ArrayList<Integer> idCourses = new ArrayList<>();
        while (result.next()) {
            int idCourse = result.getInt("ID_COURSE");
            idCourses.add(idCourse);
        }
        result.close();
        stmt.close();

        ArrayList<Course> courses = new ArrayList<>();
        for (int idCourse : idCourses) {
            Course course = CourseDAO.getInstance().get(idCourse);
            if (course != null)
                courses.add(course);
        }

        return courses;
    }

    @Override
    public void save(Section section) throws SQLException {
        String query = "INSERT INTO " + TABLE_NAME + " VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?)";

        PreparedStatement stmt = ConnectionDB.getInstance().prepareStatement(query);
        stmt.setInt(1, section.getIdPath());
        stmt.setInt(2, section.getIdSection());
        stmt.setDouble(3, section.getDistance());
        stmt.setDouble(4, section.getExpectedDuration().toMinutes());
        stmt.setDouble(5, section.getWaitingDuration().toMinutes());
        stmt.setDouble(6, section.getStartCoordinates().getLatitude());
        stmt.setDouble(7, section.getStartCoordinates().getLongitude());
        stmt.setDouble(8, section.getEndCoordinates().getLatitude());
        stmt.setDouble(9, section.getEndCoordinates().getLongitude());

        stmt.executeUpdate();
        stmt.close();
    }

    /**
     * Usefull if a section is cancelled
     * @param section
     * @throws SQLException
     */
    @Override
    public void update(Section section) throws SQLException {

    }


    public LocalTime getStartTime(Section section) {
        Path path = null;
        try {
            path = PathDAO.getInstance().get(section.getIdPath());
        }
        catch (SQLException e) {
            e.printStackTrace();
            return null;
        }

        if (path == null)
            return null;

        LocalTime startTime = path.getHour();
        Section s = path.getSection(0);
        int id = 0;
        while (s.getIdSection() < section.getIdSection()) {
            startTime = startTime.plus(s.getExpectedDuration());
            s = path.getSection(++id);
            startTime = startTime.plus(s.getWaitingDuration());
        }
        return startTime;
    }

    public void setGone(Section section) throws SQLException {
        String query = "INSERT INTO ISGONE VALUES (?, ?, ?)";

        PreparedStatement stmt = ConnectionDB.getInstance().prepareStatement(query);
        stmt.setInt(1, section.getIdPath());
        stmt.setInt(2, section.getIdSection());
        Path path = PathDAO.getInstance().get(section.getIdPath());
        stmt.setString(3, path.getUser().getEmail());

        stmt.executeUpdate();
        stmt.close();
    }

    public boolean canConfirmGone(Section section) throws SQLException {
        Path path = PathDAO.getInstance().get(section.getIdPath());

        LocalDate pathDate = path.getLocalDate();
        LocalDate currentDate = LocalDate.now();
        LocalTime sectionTime = SectionDAO.getInstance().getStartTime(section);
        LocalTime currentTime = LocalTime.now();
        if (section.getIdSection() == 0) {  // We are in the first section
            if (currentDate.isEqual(pathDate))
                return currentTime.equals(sectionTime) || currentTime.isAfter(sectionTime);
            return false;
        }
        else {  // We are in another section
            /* We check that we have confirmed the previous sections */
            for (int i = 0; i < section.getIdSection(); i++) {
                Section s = path.getSection(i);
                if (!getGone(s) || !getArrived(s))
                    return false;
            }

            return true;
        }
    }

    public boolean canConfirmArrived(Section section) throws SQLException {
        Path path = PathDAO.getInstance().get(section.getIdPath());

        LocalDate pathDate = path.getLocalDate();
        LocalDate currentDate = LocalDate.now();
        LocalTime sectionTime = SectionDAO.getInstance().getStartTime(section);
        LocalTime currentTime = LocalTime.now();
        ArrayList<Course> courses = getCoursesBySection(section);
        if (section.getIdSection() == 0) {  // We are in the first section
            if (!getGone(section))
                return false;

            if (currentDate.isEqual(pathDate) && (currentTime.equals(sectionTime) || currentTime.isAfter(sectionTime))) {
                /* Now we check that all passengers have confirmed they got in car in this section */
                for (Course course : courses) {
                    if (!CourseDAO.getInstance().hasCourseConfirmedInCarBySection(course))
                        return false;
                }
                return true;
            }
            else
                return false;
        }
        else {  // We are in another section
            for (int i = 0; i < section.getIdSection(); i++) {
                Section s = path.getSection(i);
                if (!getGone(s) || !getArrived(s))
                    return false;
            }

            /* Now we check that all passengers have confirmed they got in car in this section */
            for (Course course : courses) {
                if (!CourseDAO.getInstance().hasCourseConfirmedInCarBySection(course))
                    return false;
            }

            return getGone(section);
        }
    }

    public boolean getGone(Section section) throws SQLException {
        String query = "SELECT * FROM ISGONE WHERE ID_PATH = ? AND ID_SECTION = ? AND EMAIL = ?";

        PreparedStatement stmt = ConnectionDB.getInstance().prepareStatement(query);
        stmt.setInt(1, section.getIdPath());
        stmt.setInt(2, section.getIdSection());
        Path path = PathDAO.getInstance().get(section.getIdPath());
        stmt.setString(3, path.getUser().getEmail());
        ResultSet result = stmt.executeQuery();

        boolean gone = false;
        if (result.next())
            gone = true;

        result.close();
        stmt.close();

        return gone;
    }

    public void setArrived(Section section) throws SQLException {
        String query = "INSERT INTO ISARRIVED VALUES (?, ?, ?)";

        PreparedStatement stmt = ConnectionDB.getInstance().prepareStatement(query);
        stmt.setInt(1, section.getIdPath());
        stmt.setInt(2, section.getIdSection());
        Path path = PathDAO.getInstance().get(section.getIdPath());
        stmt.setString(3, path.getUser().getEmail());

        stmt.executeUpdate();
        stmt.close();
    }

    public boolean getArrived(Section section) throws SQLException {
        String query = "SELECT * FROM ISARRIVED WHERE ID_PATH = ? AND ID_SECTION = ? AND EMAIL = ?";

        PreparedStatement stmt = ConnectionDB.getInstance().prepareStatement(query);
        stmt.setInt(1, section.getIdPath());
        stmt.setInt(2, section.getIdSection());
        Path path = PathDAO.getInstance().get(section.getIdPath());
        stmt.setString(3, path.getUser().getEmail());
        ResultSet result = stmt.executeQuery();

        boolean arrived = false;
        if (result.next())
            arrived = true;

        result.close();
        stmt.close();

        return arrived;
    }
}
