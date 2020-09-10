package fr.ensimag.equipe3.controller;

import fr.ensimag.equipe3.model.City;
import fr.ensimag.equipe3.model.Course;
import fr.ensimag.equipe3.model.DAO.ConnectionDB;
import fr.ensimag.equipe3.model.DAO.CourseDAO;
import fr.ensimag.equipe3.model.DAO.PathDAO;
import fr.ensimag.equipe3.model.DAO.SectionDAO;
import fr.ensimag.equipe3.model.Path;
import fr.ensimag.equipe3.model.Section;
import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleObjectProperty;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.Callback;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;


public class ResultResearchController extends HomeController{
    @FXML
    private TableView<Course> _courseList;

    @FXML
    private TableColumn<Course, City> _startCol;

    @FXML
    private TableColumn<Course, City> _endCol;

    @FXML
    private TableColumn<Course, LocalDate> _dateCol;

    @FXML
    private TableColumn<Course, LocalTime> _hourCol;

    @FXML
    private TableColumn<Course, Integer> _nbSeatsCol;

    @Override
    @FXML
    public void initialize() throws NotConnectedException {
        super.initialize();

        initTable();
        try {
            displayPath(ViewController.getInstance().getStartCity(),
                    ViewController.getInstance().getEndCity());
        }
        catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

    private void initTable() {
        _startCol.setCellValueFactory(new PropertyValueFactory<Course, City>("start"));
        _endCol.setCellValueFactory(new PropertyValueFactory<Course, City>("end"));
        _dateCol.setCellValueFactory(cellData -> new SimpleObjectProperty<LocalDate>(CourseDAO.getInstance().getStartDate(cellData.getValue())));
        _hourCol.setCellValueFactory(cellData -> new SimpleObjectProperty<LocalTime>(SectionDAO.getInstance().getStartTime(cellData.getValue().getFirstSection())));
        _nbSeatsCol.setCellValueFactory(cellData -> new SimpleObjectProperty<Integer>(CourseDAO.getInstance().getNumberSeats(cellData.getValue())));

        _courseList.setRowFactory(table -> {
            final TableRow<Course> row = new TableRow<Course>();
            final ContextMenu menu = new ContextMenu();
            final MenuItem reserve = new MenuItem("Réserver ce parcours");

            reserve.setOnAction(event -> {
                Course course = row.getItem();
                try {
                    CourseDAO.getInstance().save(course);
                    ConnectionDB.getInstance().commit();
                }
                catch (SQLException e) {
                    e.printStackTrace();
                    System.err.println("Couldn't save course to database");
                }
                ViewController.getInstance().setPath(null);
                ViewController.getInstance().setStartCity(null);
                ViewController.getInstance().setEndCity(null);
                ViewController.getInstance().switchView("pathListScreen");
            });

            menu.getItems().add(reserve);

            row.contextMenuProperty().bind(
                    Bindings.when(row.emptyProperty())
                            .then((ContextMenu)null)
                            .otherwise(menu)
            );
            return row;
        });
    }

    private void displayPath(City startCity, City endCity) throws SQLException {
        ArrayList<Path> paths = PathDAO.getInstance().getPathsByCities(startCity, endCity);
        paths.removeIf(p -> {
           return p.getUser().getEmail().equals(ViewController.getInstance().getUser().getEmail())
                   || p.getLocalDate().isBefore(LocalDate.now())
                   || (p.getLocalDate().isEqual(LocalDate.now())
                        && p.getHour().isBefore(LocalTime.now()));
        });

        /* Now, we now that we have all the valid path we wanted
           to have.
           Now, we need to select the right sections in those paths
           and put them in a course object.
         */
        ArrayList<Course> courses = new ArrayList<>();
        for (Path p : paths) {
            Section start = p.getSection(0);
            for (int i = 0; !start.getStart().equals(startCity); i++)
                start = p.getSection(i);

            Section end = p.getLastSection();
            if (endCity != null) {
                for (int i = p.getNumberOfSections() - 1; !end.getEnd().equals(endCity); i--)
                    end = p.getSection(i);
            }

            ArrayList<Section> sections = new ArrayList<Section>(p.sublistSections(start.getIdSection(), end.getIdSection() + 1));
            int startDepartmentCode = startCity.getPostalCode() / 1000;
            sections.removeIf(s -> {
                return sections.get(0).getStart().getPostalCode() / 1000 != startDepartmentCode
                       || (endCity != null && sections.get(sections.size() - 1).getEnd().getPostalCode() / 1000 != endCity.getPostalCode() / 1000);
            });
            courses.add(new Course(-1, ViewController.getInstance().getUser(), sections));
        }

        _courseList.getItems().addAll(courses);
    }

    public void retour() throws SQLException {
        ViewController.getInstance().switchView("searchCourseScreen");
    }
}
