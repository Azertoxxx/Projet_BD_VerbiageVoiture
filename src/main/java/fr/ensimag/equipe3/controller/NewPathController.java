package fr.ensimag.equipe3.controller;

import fr.ensimag.equipe3.model.*;
import fr.ensimag.equipe3.model.DAO.CityDAO;
import fr.ensimag.equipe3.model.DAO.ConnectionDB;
import fr.ensimag.equipe3.model.DAO.CoordinatesDAO;
import fr.ensimag.equipe3.model.DAO.PathDAO;
import fr.ensimag.equipe3.util.GeoApi;
import fr.ensimag.equipe3.util.ValidableForm;
import fr.ensimag.equipe3.util.Validator;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextField;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;

import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.ZoneId;

import static java.lang.Double.parseDouble;
import static java.lang.Integer.parseInt;
import static java.lang.Long.parseLong;

public class NewPathController extends HomeController implements ValidableForm<Path> {
    @FXML
    private Text _status;

    @FXML
    private ComboBox<Vehicle> _vehiculeList;

    @FXML
    private TextField _seats;

    @FXML
    private TextField _hour;

    @FXML
    private DatePicker _date;

    @FXML
    private TextField _estimatedTime;

    @FXML
    private TextField _distance;

    @FXML
    private TextField _startLat;

    @FXML
    private TextField _startLong;

    @FXML
    private TextField _endLat;

    @FXML
    private TextField _endLong;

    @FXML
    private Text _waitingText;

    @FXML
    private TextField _waitingTime;

    @FXML
    private Button _newPathButton;

    @FXML
    private Button _newSectionButton;

    @Override
    @FXML
    public void initialize() throws NotConnectedException {
        super.initialize();

        _waitingTime.managedProperty().bind(_waitingTime.visibleProperty());
        _waitingText.managedProperty().bind(_waitingText.visibleProperty());

        _waitingText.setVisible(false);
        _waitingTime.setVisible(false);

        initVehicleList();

        if (ViewController.getInstance().getPath() != null) {
            _vehiculeList.setDisable(true);
            _seats.setDisable(true);
            _hour.setDisable(true);
            _date.setDisable(true);

            Path path = ViewController.getInstance().getPath();
            Coordinates lastCoord = path.getLastSection().getEndCoordinates();
            _startLat.setText(Double.toString(lastCoord.getLatitude()));
            _startLong.setText(Double.toString(lastCoord.getLongitude()));
            _startLat.setDisable(true);
            _startLong.setDisable(true);

            _waitingText.setVisible(true);
            _waitingTime.setVisible(true);
        }
    }

    private void initVehicleList() {
        for (Vehicle vehicle : ViewController.getInstance().getUser().getVehicles()) {
            _vehiculeList.getItems().addAll(vehicle);
        }
        if (_vehiculeList.getItems().size() == 0) {
            _status.setFill(Color.RED);
            _status.setText("Vous devez avoir au moins une voiture pour proposer un trajet.");

            _newPathButton.setDisable(true);
            _newSectionButton.setDisable(true);
        }
        _vehiculeList.getSelectionModel().select(0);
    }

    @FXML
    public void newPath() throws SQLException  {
        Path path = ViewController.getInstance().getPath();
        if (path == null) {
            path = processForm();
        }
        if (path != null) {
            processNewSection(path);

            PathDAO.getInstance().save(path);
            ConnectionDB.getInstance().commit();

            ViewController.getInstance().setPath(null);
            ViewController.getInstance().switchView("pathListScreen");
        }
    }

    private void processNewSection(Path path) throws SQLException {
        double startLat = parseDouble(_startLat.getText());
        double startLong = parseDouble(_startLong.getText());
        double endLat = parseDouble(_endLat.getText());
        double endLong = parseDouble(_endLong.getText());

        Coordinates startCoordinates = null;
        Coordinates endCoordinates = null;
        try {
            City startCity = GeoApi.getCityByGPS(startLat, startLong);
            CityDAO.getInstance().save(startCity);
            startCoordinates = new Coordinates(startLat, startLong, startCity);
            CoordinatesDAO.getInstance().save(startCoordinates);

            City endCity = GeoApi.getCityByGPS(endLat, endLong);
            CityDAO.getInstance().save(endCity);
            endCoordinates = new Coordinates(endLat, endLong, endCity);
            CoordinatesDAO.getInstance().save(endCoordinates);
        } catch (Exception e) {
            e.printStackTrace();
        }

        Duration waitingDuration;
        if (ViewController.getInstance().getPath() != null) {
            waitingDuration = Duration.ofMinutes(parseLong(_waitingTime.getText()));
        } else {
            waitingDuration = Duration.ofMinutes(0);
        }

        Section section = new Section(path.getIdPath(),
                ViewController.getInstance().getPath() == null ? 0 : path.getNumberOfSections(),
                parseDouble(_distance.getText()),
                Duration.ofMinutes(parseLong(_estimatedTime.getText())),
                waitingDuration, startCoordinates, endCoordinates);

        path.addSection(section);
    }

    @FXML
    public void toNewSection() throws SQLException {
        Path path = ViewController.getInstance().getPath();
        if (path == null) {
            path = processForm();
        }

        if (path != null) {
            processNewSection(path);

            ViewController.getInstance().setPath(path);
            ViewController.getInstance().switchView("newPathScreen");
        }
    }

    @Override
    public boolean createConditions() {
        Validator validator = new Validator();

        validator.addCondition(_seats,
                Validator.isNumber(_seats) && Validator.getInteger(_seats) > 0,
                "Le nombre de places libres doit être un nombre entier positif.");

        validator.addCondition(_startLat,
                Validator.isNumber(_startLat),
                "La latitude doit être un nombre (entier ou à virgule).");

        validator.addCondition(_startLong,
                Validator.isNumber(_startLong),
                "La longitude doit être un nombre (entier ou à virgule).");

        validator.addCondition(_endLat,
                Validator.isNumber(_endLat),
                "La latitude doit être un nombre (entier ou à virgule).");

        validator.addCondition(_endLong,
                Validator.isNumber(_endLong),
                "La longitude doit être un nombre (entier ou à virgule).");

        validator.addCondition(_estimatedTime,
                Validator.isNumber(_estimatedTime) && Validator.getInteger(_estimatedTime) > 0,
                "Le temps estimé doit être un nombre entier, représentant les minutes.");

        validator.addCondition(_hour,
                Validator.isValidHour(_hour),
                "L'heure doit être sous le format HH:MM.");

        return validator.validateFields();
    }

    @Override
    public Path processForm() throws SQLException {
        _status.setFill(Color.BLACK);
        _status.setText("");

        if (!createConditions()) {
            _status.setFill(Color.RED);
            _status.setText("Certains champs sont invalides.");
            return null;
        }

        if (_vehiculeList.getSelectionModel().getSelectedItem().getSeatsNumber()
                <= parseInt(_seats.getText())) {
            _status.setFill(Color.RED);
            _status.setText("Le nombre de places ne peut excéder la capacité du véhicule.");
            return null;
        }

        LocalDate selectedDate = _date.getValue();
        LocalDate today = LocalDate.now();
        if (selectedDate.isBefore(today)) {
            _status.setFill(Color.RED);
            _status.setText("La date sélectionnée ne peut être antérieure à la date du jour.");
            return null;
        }

        java.sql.Date sqlDate = new java.sql.Date(Date.from(
                Instant.from(
                        _date.getValue().atStartOfDay(ZoneId.systemDefault()))).getTime());

        Path path = new Path(PathDAO.getInstance().getNextId(), ViewController.getInstance().getUser(),
                _vehiculeList.getSelectionModel().getSelectedItem(), parseInt(_seats.getText()),
                sqlDate, Validator.stringToDuration(_hour.getText()));
        return path;
    }
}
