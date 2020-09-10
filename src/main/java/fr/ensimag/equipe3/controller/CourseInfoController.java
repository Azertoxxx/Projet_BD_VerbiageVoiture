package fr.ensimag.equipe3.controller;

import fr.ensimag.equipe3.model.*;
import fr.ensimag.equipe3.model.DAO.*;
import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleObjectProperty;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;

import java.sql.SQLException;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;

public class CourseInfoController extends HomeController {
    @FXML
    private Text _title;

    @FXML
    private Text _startCourse;

    @FXML
    private Text _endCourse;

    @FXML
    private Text _stateCourse;

    @FXML
    private Text _stepsCourse;

    @FXML
    private Text _vehicleCourse;

    @FXML
    private TableView<Section> _sectionsList;

    @FXML
    private TableColumn<Section, Integer> _sectionId;

    @FXML
    private TableColumn<Section, City> _startSection;

    @FXML
    private TableColumn<Section, City> _endSection;

    @FXML
    private TableColumn<Section, LocalTime> _hourSection;

    @FXML
    private TableColumn<Section, Duration> _estimatedTime;

    @FXML
    private Text _status;

    @FXML
    private Button _buttonInCar;

    @FXML
    private Button _buttonOutCar;

    @FXML
    private Button _buttonPay;

    private Course _course = ViewController.getInstance().getCourse();

    @Override
    @FXML
    public void initialize() throws NotConnectedException {
        super.initialize();
        ViewController.getInstance().setPath(null);

        String title = "Parcours du ";
        title += CourseDAO.getInstance().getStartDate(_course);
        title += " " + SectionDAO.getInstance().getStartTime(_course.getFirstSection());
        title += " : ";
        title += _course.getStart() + "-" + _course.getEnd();
        _title.setText(title);

        try {
            if (!CourseDAO.getInstance().hasCourseConfirmedInCarBySection(_course)) {
                _buttonInCar.setDisable(false);
                _buttonOutCar.setDisable(true);
                _buttonPay.setDisable(true);
            } else if (!CourseDAO.getInstance().hasCourseConfirmedOutCarBySection(_course)) {
                _buttonInCar.setDisable(true);
                _buttonOutCar.setDisable(false);
                _buttonPay.setDisable(true);
            } else {
                _buttonInCar.setDisable(true);
                _buttonOutCar.setDisable(true);
                _buttonPay.setDisable(CourseDAO.getInstance().hasBeenPaid(_course, ViewController.getInstance().getUser()));
            }
        }
        catch (SQLException e) {
            e.printStackTrace();
        }

        initCourseInfo(_course);
        initSectionsInfo(_course);
    }

    private void initCourseInfo(Course _course) {
        _startCourse.setText(_course.getStart().toString());
        _endCourse.setText(_course.getEnd().toString());

        LocalDate currentDate = LocalDate.now();
        LocalDate courseDate = CourseDAO.getInstance().getStartDate(_course);
        if (currentDate.isBefore(courseDate)) {
            _stateCourse.setText("Non commencé");
        }
        else if (currentDate.isAfter(courseDate)) {
            _stateCourse.setText("Terminé");
        }
        else {
            LocalTime startTime = SectionDAO.getInstance().getStartTime(_course.getFirstSection());
            LocalTime totalTime = startTime;
            for (Section s : _course.getSections()) {
                totalTime = totalTime.plus(s.getWaitingDuration());
                totalTime = totalTime.plus(s.getExpectedDuration());
            }
            LocalTime currentTime = LocalTime.now();
            if (currentTime.isBefore(startTime))
                _stateCourse.setText("Non commencé");
            else if (currentTime.isBefore(totalTime))
                _stateCourse.setText("En cours");
            else
                _stateCourse.setText("Terminé");
        }

        _stepsCourse.setText(String.valueOf(_course.getNumberOfSections()));
        _vehicleCourse.setText(CourseDAO.getInstance().getVehicule(_course).toString());
    }

    private void initSectionsInfo(Course _course) {
        _sectionId.setCellValueFactory(new PropertyValueFactory<Section, Integer>("id"));
        _startSection.setCellValueFactory(new PropertyValueFactory<Section, City>("start"));
        _endSection.setCellValueFactory(new PropertyValueFactory<Section, City>("end"));
        _hourSection.setCellValueFactory(cellData -> new SimpleObjectProperty<LocalTime>(SectionDAO.getInstance().getStartTime(cellData.getValue())));
        _estimatedTime.setCellValueFactory(new PropertyValueFactory<Section, Duration>("expectedDuration"));

        _sectionsList.getSortOrder().clear();
        _sectionsList.getSortOrder().add(_sectionId);
        _sectionsList.sort();

        _estimatedTime.setCellFactory(cell -> new TableCell<Section, Duration>() {
            @Override
            protected void updateItem(Duration duration, boolean empty) {
                super.updateItem(duration, empty);
                if (empty)
                    setText(null);
                else {
                    long minutes = duration.toMinutes();
                    String result = String.format("%d:%02d", minutes / 60, minutes % 60);
                    setText(result);
                }
            }
        });

        for (Section section : _course.getSections()) {
            _sectionsList.getItems().add(section);
        }
    }

    public void confirmInCar() {
        Section currentSection = _course.getFirstSection();
        try {
            if (CourseDAO.getInstance().canConfirmInCar(_course)) {
                CourseDAO.getInstance().setInCar(_course);
                ConnectionDB.getInstance().commit();
                ViewController.getInstance().setCourse(_course);
                ViewController.getInstance().switchView("courseInfoScreen");
            }
        }
        catch (SQLException e) {
            e.printStackTrace();
            System.err.println("Couldn't validate gone status of section (path=" + currentSection.getIdPath()
                    + ", id=" + currentSection.getIdSection() + ")");
        }
    }

    public void confirmOutCar() {
        Section currentSection = _course.getLastSection();
        try {
            if (CourseDAO.getInstance().canConfirmOutCar(_course)) {
                CourseDAO.getInstance().setOutCar(_course);
                ConnectionDB.getInstance().commit();
                ViewController.getInstance().setCourse(_course);
                ViewController.getInstance().switchView("courseInfoScreen");
            }
        }
        catch (SQLException e) {
            e.printStackTrace();
            System.err.println("Couldn't validate arrived status of section (path=" + currentSection.getIdPath()
                    + ", id=" + currentSection.getIdSection() + ")");
        }
    }

    public void pay() throws SQLException {
        User user = ViewController.getInstance().getUser();
        Path path = PathDAO.getInstance().get(_course.getFirstSection().getIdPath());
        User driver = path.getUser();
        Vehicle vehicle = CourseDAO.getInstance().getVehicule(_course);

        double costByKm = vehicle.getFiscalPower() * 0.10;
        if (vehicle.getEnergy().equals("DIESEL"))
            costByKm *= 1.5;
        else if (vehicle.getEnergy().equals("ELECTRIQUE"))
            costByKm *= 0.5;

        double toPay = 0;
        for (Section s : _course.getSections()) {
            toPay += s.getDistance() * costByKm;
        }
        if (user.getSolde() < toPay) {
            _status.setFill(Color.RED);
            _status.setText("Vous n'avez pas assez de crédit pour payer ce trajet. Veuillez recharger votre porte-monnaie.");
        }
        else {
            user.setSolde(user.getSolde() - toPay);
            driver.setSolde(driver.getSolde() + toPay);
            try {
                CourseDAO.getInstance().setPaid(_course, user);
                UserDAO.getInstance().update(user);
                UserDAO.getInstance().update(driver);
                ConnectionDB.getInstance().commit();
                ViewController.getInstance().setCourse(_course);
                ViewController.getInstance().switchView("courseInfoScreen");
            }
            catch (Exception e) {
                e.printStackTrace();
                System.err.println("Couldn't mark course as paid.");
            }
        }
    }
}
