package fr.ensimag.equipe3.model.DAO;

import fr.ensimag.equipe3.model.*;
import javafx.util.Pair;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;

/**
 * Link between a course and the data base.
 */
public class CourseDAO implements Dao<Course, Integer> {

    private final static CourseDAO instance = new CourseDAO();
    private final static String TABLE_NAME = "COURSE";
    private final static String TABLE_NAME_SECTION = "ISRESERVED";
    public static CourseDAO getInstance() { return instance; }

    /**
     * Allows to know the list of Reserved sections of a course.
     * @param idCourse Identifier of the course
     * @return the list of reserved sections
     * @throws SQLException
     */
    private ArrayList<Section> getSections(int idCourse) throws SQLException {
        String query = "SELECT * FROM " + TABLE_NAME_SECTION + " WHERE ID_COURSE = ?";

        PreparedStatement stmt = ConnectionDB.getInstance().prepareStatement(query);
        stmt.setInt(1, idCourse);
        ResultSet result = stmt.executeQuery();

        ArrayList<Pair<Integer, Integer>> idSections = new ArrayList<>();
        while (result.next()) {
            int idSection = result.getInt("ID_SECTION");
            int idPath = result.getInt("ID_PATH");
            idSections.add(new Pair<>(idPath, idSection));
        }

        result.close();
        stmt.close();

        ArrayList<Section> sections = new ArrayList<Section>();

        for (Pair<Integer, Integer> id : idSections) {
            Section section = SectionDAO.getInstance().get(new Pair<Integer, Integer>(id.getKey(), id.getValue()));
            sections.add(section);
        }

        return sections;
    }

    public ArrayList<Course> getCourseByUser(User user) throws SQLException {
        String query = "SELECT * FROM " + TABLE_NAME + " WHERE EMAIL = ?";

        PreparedStatement stmt = ConnectionDB.getInstance().prepareStatement(query);
        stmt.setString(1, user.getEmail());
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
            Course course = get(idCourse);
            courses.add(course);
        }

        return courses;
    }

    public boolean hasCourseConfirmedInCarBySection(Course course) throws SQLException {
        Section section = course.getFirstSection();
        String query = "SELECT * FROM ISINCAR WHERE ID_PATH = ? AND ID_SECTION = ? AND ID_COURSE = ?";

        PreparedStatement stmt = ConnectionDB.getInstance().prepareStatement(query);
        stmt.setInt(1, section.getIdPath());
        stmt.setInt(2, section.getIdSection());
        stmt.setInt(3, course.getIdCourse());
        ResultSet result = stmt.executeQuery();

        boolean confirmed = result.next();
        result.close();
        stmt.close();

        return confirmed;
    }

    public void setInCar(Course course) throws SQLException {
        Section section = course.getFirstSection();
        String query = "INSERT INTO ISINCAR VALUES (?, ?, ?)";

        PreparedStatement stmt = ConnectionDB.getInstance().prepareStatement(query);
        stmt.setInt(1, section.getIdPath());
        stmt.setInt(2, section.getIdSection());
        stmt.setInt(3, course.getIdCourse());
        stmt.executeUpdate();
        stmt.close();
    }

    public boolean hasCourseConfirmedOutCarBySection(Course course) throws SQLException {
        Section section = course.getLastSection();
        String query = "SELECT * FROM ISOUTCAR WHERE ID_PATH = ? AND ID_SECTION = ? AND ID_COURSE = ?";

        PreparedStatement stmt = ConnectionDB.getInstance().prepareStatement(query);
        stmt.setInt(1, section.getIdPath());
        stmt.setInt(2, section.getIdSection());
        stmt.setInt(3, course.getIdCourse());
        ResultSet result = stmt.executeQuery();

        boolean confirmed = result.next();
        result.close();
        stmt.close();

        return confirmed;
    }

    public boolean canConfirmInCar(Course course) throws SQLException {
        Section section = course.getFirstSection();
        Path path = PathDAO.getInstance().get(section.getIdPath());

        LocalDate pathDate = path.getLocalDate();
        LocalDate currentDate = LocalDate.now();
        LocalTime sectionTime = SectionDAO.getInstance().getStartTime(section);
        LocalTime currentTime = LocalTime.now();
        if (currentDate.isEqual(pathDate))
            return (currentTime.equals(sectionTime) || currentTime.isAfter(sectionTime))
                    && SectionDAO.getInstance().getGone(section);
        return false;
    }

    public void setOutCar(Course course) throws SQLException {
        Section section = course.getLastSection();
        String query = "INSERT INTO ISOUTCAR VALUES (?, ?, ?)";

        PreparedStatement stmt = ConnectionDB.getInstance().prepareStatement(query);
        stmt.setInt(1, section.getIdPath());
        stmt.setInt(2, section.getIdSection());
        stmt.setInt(3, course.getIdCourse());
        stmt.executeUpdate();
        stmt.close();
    }

    public boolean canConfirmOutCar(Course course) throws SQLException {
        Section section = course.getLastSection();
        Path path = PathDAO.getInstance().get(section.getIdPath());

        LocalDate pathDate = path.getLocalDate();
        LocalDate currentDate = LocalDate.now();
        LocalTime sectionTime = SectionDAO.getInstance().getStartTime(section);
        LocalTime currentTime = LocalTime.now();
        if (currentDate.isEqual(pathDate))
            return hasCourseConfirmedInCarBySection(course)
                    && SectionDAO.getInstance().getArrived(section);
        return false;
    }

    @Override
    public Course get(Integer idCourse) throws SQLException {
        String query = "SELECT * FROM " + TABLE_NAME + " WHERE ID_COURSE = ?";

        PreparedStatement stmt = ConnectionDB.getInstance().prepareStatement(query);
        stmt.setInt(1, idCourse);
        ResultSet result = stmt.executeQuery();

        if (!result.next()) {
            return null;
        }

        int id = result.getInt("ID_COURSE");
        String userEmail = result.getString("EMAIL");

        stmt.close();
        result.close();

        User user = UserDAO.getInstance().get(userEmail);
        ArrayList<Section> sections = this.getSections(idCourse);

        return new Course(id, user, sections);
    }

    public int getNextId() throws SQLException {
        String query = "SELECT max(ID_COURSE) FROM " + TABLE_NAME;
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
    public void save(Course course) throws SQLException {
        String query = "INSERT INTO " + TABLE_NAME + " VALUES(?, ?)";

        PreparedStatement stmt = ConnectionDB.getInstance().prepareStatement(query);
        course.setIdCourse(getNextId());
        stmt.setInt(1, course.getIdCourse());
        stmt.setString(2, course.getUser().getEmail());
        stmt.executeUpdate();
        stmt.close();

        for (Section section : course.getSections()) {
            query = "INSERT INTO " + TABLE_NAME_SECTION + " VALUES(?, ?, ?)";
            stmt = ConnectionDB.getInstance().prepareStatement(query);
            stmt.setInt(1, course.getIdCourse());
            stmt.setInt(2, section.getIdPath());
            stmt.setInt(3, section.getIdSection());
            stmt.executeUpdate();
            stmt.close();
        }
    }

    /**
     * Usefull when a Section is cancelled
     * @param course
     * @throws SQLException
     */
    @Override
    public void update(Course course) throws SQLException {

    }

    public LocalDate getStartDate(Course course)  {
        Section firstSection = course.getFirstSection();
        Path path;
        try {
            path = PathDAO.getInstance().get(firstSection.getIdPath());
        }
        catch (SQLException e) {
            e.printStackTrace();
            return null;
        }

        return path == null ? null : path.getLocalDate();
    }

    public int getNumberSeats(Course course) {
        Path path;
        Section firstSection = course.getFirstSection();
        try {
            path = PathDAO.getInstance().get(firstSection.getIdPath());
        }
        catch (SQLException e) {
            e.printStackTrace();
            return -1;
        }

        return path == null ? -1 : path.getNbSeats();
    }

    public Vehicle getVehicule(Course course)  {
        Path path;
        Section firstSection = course.getFirstSection();
        try {
            path = PathDAO.getInstance().get(firstSection.getIdPath());
        }
        catch (SQLException e) {
            e.printStackTrace();
            return null;
        }

        return path.getVehicle();
    }

    public boolean hasBeenPaid(Course course, User user) throws SQLException {
        String query = "SELECT * FROM ISPAYED WHERE ID_COURSE = ? AND EMAIL = ?";

        PreparedStatement stmt = ConnectionDB.getInstance().prepareStatement(query);
        stmt.setInt(1, course.getIdCourse());
        stmt.setString(2, user.getEmail());
        ResultSet result = stmt.executeQuery();

        boolean paid = result.next();
        result.close();
        stmt.close();

        return paid;
    }

    public void setPaid(Course course, User user) throws SQLException {
        String query = "INSERT INTO ISPAYED VALUES(?, ?)";

        PreparedStatement stmt = ConnectionDB.getInstance().prepareStatement(query);
        stmt.setInt(1, course.getIdCourse());
        stmt.setString(2, user.getEmail());
        stmt.executeUpdate();
        stmt.close();
    }
}
