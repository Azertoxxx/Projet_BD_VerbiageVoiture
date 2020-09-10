package fr.ensimag.equipe3.controller;

import fr.ensimag.equipe3.model.City;
import fr.ensimag.equipe3.model.DAO.ConnectionDB;
import fr.ensimag.equipe3.model.DAO.PathDAO;
import fr.ensimag.equipe3.model.DAO.SectionDAO;
import fr.ensimag.equipe3.model.Path;
import fr.ensimag.equipe3.model.Section;
import fr.ensimag.equipe3.model.Vehicle;
import fr.ensimag.equipe3.util.Validator;
import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;

import java.sql.Date;
import java.sql.SQLException;
import java.sql.Time;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;

public class PathInfoController extends HomeController {
    @FXML
    private Text _title;

    @FXML
    private Text _startPath;

    @FXML
    private Text _endPath;

    @FXML
    private Text _statePath;

    @FXML
    private Text _stepsPath;

    @FXML
    private Text _vehiclePath;

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
    private TableColumn<Section, Boolean> _startValidated;

    @FXML
    private TableColumn<Section, Boolean> _endValidated;

    @FXML
    private Text _status;

    private Path _path = ViewController.getInstance().getPath();

    @Override
    @FXML
    public void initialize() throws NotConnectedException {
        super.initialize();
        ViewController.getInstance().setPath(null);

        String title = "Trajet du ";
        title += _path.getDate() + " " + _path.getHour();
        title += " : ";
        title += _path.getStart() + "-" + _path.getEnd();
        _title.setText(title);

        initPathInfo(_path);
        initSectionsInfo(_path);
    }

    private void initPathInfo(Path _path) {
        _startPath.setText(_path.getStart().toString());
        _endPath.setText(_path.getEnd().toString());

        LocalDate currentDate = LocalDate.now();
        if (currentDate.isBefore(_path.getLocalDate())) {
            _statePath.setText("Non commencé");
        }
        else if (currentDate.isAfter(_path.getLocalDate())) {
            _statePath.setText("Terminé");
        }
        else {
            LocalTime startTime = _path.getHour();
            LocalTime totalTime = startTime;
            for (Section s : _path.getSections()) {
                totalTime = totalTime.plus(s.getWaitingDuration());
                totalTime = totalTime.plus(s.getExpectedDuration());
            }
            LocalTime currentTime = LocalTime.now();
            if (currentTime.isBefore(startTime))
                _statePath.setText("Non commencé");
            else if (currentTime.isBefore(totalTime))
                _statePath.setText("En cours");
            else
                _statePath.setText("Terminé");
        }

        _stepsPath.setText(String.valueOf(_path.getNumberOfSections()));
        _vehiclePath.setText(_path.getVehicle().toString());
    }

    private void initSectionsInfo(Path _path) {
        _sectionId.setCellValueFactory(new PropertyValueFactory<Section, Integer>("id"));
        _startSection.setCellValueFactory(new PropertyValueFactory<Section, City>("start"));
        _endSection.setCellValueFactory(new PropertyValueFactory<Section, City>("end"));
        _hourSection.setCellValueFactory(cellData -> new SimpleObjectProperty<LocalTime>(SectionDAO.getInstance().getStartTime(cellData.getValue())));
        _estimatedTime.setCellValueFactory(new PropertyValueFactory<Section, Duration>("expectedDuration"));
        _startValidated.setCellValueFactory(cellData -> {
            try {
                return new SimpleObjectProperty<Boolean>(SectionDAO.getInstance().getGone(cellData.getValue()));
            } catch (SQLException throwables) {
                throwables.printStackTrace();
                System.err.println("Couldn't retrieve gone status of section (path=" + cellData.getValue().getIdPath()
                        + ", id=" + cellData.getValue().getIdSection() + ")");
                return null;
            }
        });
        _endValidated.setCellValueFactory(cellData -> {
            try {
                return new SimpleObjectProperty<Boolean>(SectionDAO.getInstance().getArrived(cellData.getValue()));
            } catch (SQLException throwables) {
                throwables.printStackTrace();
                System.err.println("Couldn't retrieve arrived status of section (path=" + cellData.getValue().getIdPath()
                        + ", id=" + cellData.getValue().getIdSection() + ")");
                return null;
            }
        });

        _sectionsList.getSortOrder().clear();
        _sectionsList.getSortOrder().add(_sectionId);
        _sectionsList.sort();

        _sectionsList.setRowFactory(table -> {
            final TableRow<Section> row = new TableRow<>();
            final ContextMenu menu = new ContextMenu();
            final MenuItem goneValid = new MenuItem("Confirmer le départ");
            final MenuItem arrivedValid = new MenuItem("Confirmer l'arrivée");

            goneValid.setOnAction(event -> {
                Section currentSection = row.getItem();
                try {
                    if (SectionDAO.getInstance().canConfirmGone(currentSection)) {
                        SectionDAO.getInstance().setGone(currentSection);
                        ConnectionDB.getInstance().commit();
                        ViewController.getInstance().setPath(_path);
                        ViewController.getInstance().switchView("pathInfoScreen");
                    }
                    else {
                        _status.setFill(Color.RED);
                        _status.setText("Vous devez avoir confirmé le départ et l'arrivée des autres tronçons d'abord.");
                    }
                }
                catch (SQLException e) {
                    e.printStackTrace();
                    System.err.println("Couldn't validate gone status of section (path=" + currentSection.getIdPath()
                    + ", id=" + currentSection.getIdSection() + ")");
                }
            });

            arrivedValid.setOnAction(event -> {
                Section currentSection = row.getItem();
                try {
                    if (SectionDAO.getInstance().canConfirmArrived(currentSection)) {
                        SectionDAO.getInstance().setArrived(currentSection);
                        ConnectionDB.getInstance().commit();
                        ViewController.getInstance().setPath(_path);
                        ViewController.getInstance().switchView("pathInfoScreen");
                    }
                    else {
                        _status.setFill(Color.RED);
                        _status.setText("Vous devez avoir confirmé le départ et l'arrivée des autres tronçons d'abord.");
                    }
                }
                catch (SQLException e) {
                    e.printStackTrace();
                    System.err.println("Couldn't validate arrived status of section (path=" + currentSection.getIdPath()
                            + ", id=" + currentSection.getIdSection() + ")");
                }
            });

            menu.getItems().add(goneValid);
            menu.getItems().add(arrivedValid);

            row.contextMenuProperty().bind(
                    Bindings.when(row.emptyProperty())
                            .then((ContextMenu)null)
                            .otherwise(menu)
            );

            return row;
        });

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

        _startValidated.setCellFactory(cell -> new TableCell<Section, Boolean>() {
            @Override
            protected void updateItem(Boolean item, boolean empty) {
                super.updateItem(item, empty);
                if (empty)
                    setText(null);
                else {
                    if (item)
                        setText("Oui");
                    else
                        setText("Non");
                }
            }
        });
        _endValidated.setCellFactory(cell -> new TableCell<Section, Boolean>() {
            @Override
            protected void updateItem(Boolean item, boolean empty) {
                super.updateItem(item, empty);
                if (empty)
                    setText(null);
                else {
                    if (item)
                        setText("Oui");
                    else
                        setText("Non");
                }
            }
        });

        for (Section section : _path.getSections()) {
            _sectionsList.getItems().add(section);
        }
    }
}
