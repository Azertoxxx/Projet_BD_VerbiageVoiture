package fr.ensimag.equipe3.controller;

import fr.ensimag.equipe3.model.DAO.ConnectionDB;
import fr.ensimag.equipe3.model.DAO.UserDAO;
import fr.ensimag.equipe3.model.DAO.VehicleDAO;
import fr.ensimag.equipe3.model.Vehicle;
import javafx.beans.binding.Bindings;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.sql.SQLException;

public class VehiculesController extends HomeController {
    @FXML
    private TableView<Vehicle> _vehiclesList;

    @FXML
    private TableColumn<Vehicle, String> _licenseCol;

    @FXML
    private TableColumn<Vehicle, String> _brandCol;

    @FXML
    private TableColumn<Vehicle, String> _modelCol;

    @FXML
    private TableColumn<Vehicle, String> _energyCol;

    @FXML
    private TableColumn<Vehicle, Integer> _powerCol;

    @FXML
    private TableColumn<Vehicle, Integer> _seatsCol;

    @Override
    @FXML
    public void initialize() throws NotConnectedException {
        super.initialize();

        initTable();
        displayVehicles();
    }

    private void initTable() {
        _licenseCol.setCellValueFactory(new PropertyValueFactory<Vehicle, String>("licensePlate"));
        _brandCol.setCellValueFactory(new PropertyValueFactory<Vehicle, String>("brand"));
        _modelCol.setCellValueFactory(new PropertyValueFactory<Vehicle, String>("model"));
        _energyCol.setCellValueFactory(new PropertyValueFactory<Vehicle, String>("energy"));
        _powerCol.setCellValueFactory(new PropertyValueFactory<Vehicle, Integer>("fiscalPower"));
        _seatsCol.setCellValueFactory(new PropertyValueFactory<Vehicle, Integer>("seatsNumber"));

        /**
         * Contextual menu on row, this is an example taken from:
         * https://gist.github.com/james-d/7758918
         */

        _vehiclesList.setRowFactory(table -> {
            final TableRow<Vehicle> row = new TableRow<Vehicle>();
            final ContextMenu menu = new ContextMenu();
            final MenuItem deleteItem = new MenuItem("Supprimer le véhicule");
            deleteItem.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    ViewController.getInstance().getUser().removeVehicle(row.getItem());
                    try {
                        UserDAO.getInstance().deleteRemovedVehicle(ViewController.getInstance().getUser(), row.getItem());
                        VehicleDAO.getInstance().delete(row.getItem());
                        ConnectionDB.getInstance().commit();
                    }
                    catch (SQLException e) {
                        System.err.println("Couldn't update user " + ViewController.getInstance().getUser().getEmail());
                    }
                    table.getItems().remove(row.getItem());
                }
            });

            menu.getItems().add(deleteItem);

            row.contextMenuProperty().bind(
                    Bindings.when(row.emptyProperty())
                            .then((ContextMenu)null)
                            .otherwise(menu)
            );
            return row;
        });
    }

    private void displayVehicles() {
        for (Vehicle vehicle : ViewController.getInstance().getUser().getVehicles()) {
            _vehiclesList.getItems().add(vehicle);
        }
    }

    public void newVehicle() {
        ViewController.getInstance().switchView("newVehicleScreen");
    }
}
