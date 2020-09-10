package fr.ensimag.equipe3.controller;

import fr.ensimag.equipe3.model.City;
import fr.ensimag.equipe3.model.Course;
import fr.ensimag.equipe3.model.DAO.CourseDAO;
import fr.ensimag.equipe3.model.DAO.PathDAO;
import fr.ensimag.equipe3.model.DAO.SectionDAO;
import fr.ensimag.equipe3.model.Path;
import fr.ensimag.equipe3.model.Vehicle;
import fr.ensimag.equipe3.util.Validator;
import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleObjectProperty;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;

import java.sql.SQLException;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;

public class PathListController extends HomeController {
    @FXML
    private TableView<Path> _suggestedPathList;

    @FXML
    private TableColumn<Path, LocalDate> _suggestedDateCol;

    @FXML
    private TableColumn<Path, LocalTime> _suggestedHourCol;

    @FXML
    private TableColumn<Path, Vehicle> _suggestedVehicleCol;

    @FXML
    private TableColumn<Path, City> _suggestedStartCol;

    @FXML
    private TableColumn<Path, City> _suggestedEndCol;

    @FXML
    private TableView<Course> _reservedCourseList;

    @FXML
    private TableColumn<Course, LocalDate> _reservedDateCol;

    @FXML
    private TableColumn<Course, LocalTime> _reservedHourCol;

    @FXML
    private TableColumn<Course, Vehicle> _reservedVehicleCol;

    @FXML
    private TableColumn<Course, City> _reservedStartCol;

    @FXML
    private TableColumn<Course, City> _reservedEndCol;
    @FXML
    private Text _status;

    @FXML
    private VBox _vboxCenter;

    @Override
    @FXML
    public void initialize() throws NotConnectedException {
        super.initialize();

        initSuggestedTable();
        initReservedTale();
        try {
            displaySuggestedPath();
            displayReservedCourse();
        }
        catch (SQLException e) {
            _status.setFill(Color.RED);
            _status.setText("Erreur de connexion : impossible d'afficher la liste des trajets.");
            e.printStackTrace();
        }
    }

    private void initSuggestedTable() {
        _suggestedDateCol.setCellValueFactory(new PropertyValueFactory<Path, LocalDate>("localDate"));
        _suggestedHourCol.setCellValueFactory(new PropertyValueFactory<Path, LocalTime>("hour"));
        _suggestedVehicleCol.setCellValueFactory(new PropertyValueFactory<Path, Vehicle>("vehicle"));
        _suggestedStartCol.setCellValueFactory(new PropertyValueFactory<Path, City>("start"));
        _suggestedEndCol.setCellValueFactory(new PropertyValueFactory<Path, City>("end"));

        _suggestedPathList.getSortOrder().setAll(_suggestedDateCol);

        _suggestedPathList.setRowFactory(table -> {
            final TableRow<Path> row = new TableRow<Path>();
            final ContextMenu menu = new ContextMenu();
            final MenuItem deleteItem = new MenuItem("Annuler le trajet");
            deleteItem.setOnAction(event -> {
                /**
                 * TODO:
                 * here we need to add the proper action: remove the path from
                 * the DB.
                 */
                table.getItems().remove(row.getItem());
            });

            final MenuItem moreInfo = new MenuItem("Plus d'informations sur le trajet");
            moreInfo.setOnAction(event -> {
                ViewController.getInstance().setPath(row.getItem());
                ViewController.getInstance().switchView("pathInfoScreen");
            });

            menu.getItems().add(moreInfo);

            row.contextMenuProperty().bind(
                    Bindings.when(row.emptyProperty())
                            .then((ContextMenu)null)
                            .otherwise(menu)
            );

            row.setOnMouseClicked(new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent event) {
                    if (event.getButton().equals(MouseButton.PRIMARY)) {
                        if (event.getClickCount() == 2) {
                            ViewController.getInstance().setPath(row.getItem());
                            ViewController.getInstance().switchView("pathInfoScreen");
                        }
                    }
                }
            });

            return row;
        });
    }

    private void initReservedTale() {
        _reservedDateCol.setCellValueFactory(cellData -> new SimpleObjectProperty<LocalDate>(CourseDAO.getInstance().getStartDate(cellData.getValue())));
        _reservedHourCol.setCellValueFactory(cellData -> new SimpleObjectProperty<LocalTime>(SectionDAO.getInstance().getStartTime(cellData.getValue().getFirstSection())));
        _reservedVehicleCol.setCellValueFactory(cellData -> new SimpleObjectProperty<Vehicle>(CourseDAO.getInstance().getVehicule(cellData.getValue())));
        _reservedStartCol.setCellValueFactory(new PropertyValueFactory<Course, City>("start"));
        _reservedEndCol.setCellValueFactory(new PropertyValueFactory<Course, City>("end"));

        _reservedCourseList.getSortOrder().setAll(_reservedDateCol);

        _reservedCourseList.setRowFactory(table -> {
            final TableRow<Course> row = new TableRow<Course>();
            final ContextMenu menu = new ContextMenu();
            final MenuItem deleteItem = new MenuItem("Annuler le trajet");
            deleteItem.setOnAction(event -> {
                /**
                 * TODO:
                 * here we need to add the proper action: remove the path from
                 * the DB.
                 */
                table.getItems().remove(row.getItem());
            });
            final MenuItem moreInfo = new MenuItem("Plus d'informations sur le trajet");
            moreInfo.setOnAction(event -> {
                ViewController.getInstance().setCourse(row.getItem());
                ViewController.getInstance().switchView("courseInfoScreen");
            });
            menu.getItems().add(moreInfo);
            row.contextMenuProperty().bind(
                    Bindings.when(row.emptyProperty())
                            .then((ContextMenu)null)
                            .otherwise(menu)
            );
            row.setOnMouseClicked(new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent event) {
                    if (event.getButton().equals(MouseButton.PRIMARY)) {
                        if (event.getClickCount() == 2) {
                            ViewController.getInstance().setCourse(row.getItem());
                            ViewController.getInstance().switchView("courseInfoScreen");
                        }
                    }
                }
            });
            return row;
        });
    }

    private void displaySuggestedPath() throws SQLException  {
        _suggestedPathList.getItems().addAll(PathDAO.getInstance().getPathsByUser(ViewController.getInstance().getUser()));
    }

    private void displayReservedCourse() throws SQLException  {
        _reservedCourseList.getItems().addAll(CourseDAO.getInstance().getCourseByUser(ViewController.getInstance().getUser()));
    }


}
