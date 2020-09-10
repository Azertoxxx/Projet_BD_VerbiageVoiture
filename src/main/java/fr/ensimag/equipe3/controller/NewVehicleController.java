package fr.ensimag.equipe3.controller;

import fr.ensimag.equipe3.model.DAO.ConnectionDB;
import fr.ensimag.equipe3.model.DAO.EnergyDAO;
import fr.ensimag.equipe3.model.DAO.UserDAO;
import fr.ensimag.equipe3.model.DAO.VehicleDAO;
import fr.ensimag.equipe3.model.User;
import fr.ensimag.equipe3.model.Vehicle;
import fr.ensimag.equipe3.util.ValidableForm;
import fr.ensimag.equipe3.util.Validator;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.TextField;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;

import java.sql.SQLException;
import java.util.ArrayList;

import static java.lang.Integer.parseInt;
import static java.lang.System.exit;

public class NewVehicleController extends HomeController implements ValidableForm<Vehicle> {
    @FXML
    private Text _status;

    @FXML
    private TextField _licensePlate;

    @FXML
    private ComboBox<String> _energyName;

    @FXML
    private TextField _fiscalPower;

    @FXML
    private TextField _seatsNumber;

    @FXML
    private TextField _brand;

    @FXML
    private TextField _model;

    final private ArrayList<ContextMenu> _hints = new ArrayList<ContextMenu>();


    @Override
    @FXML
    public void initialize() throws NotConnectedException {
        super.initialize();

        try {
            displayEnergies();
        }
        catch (SQLException e) {
            System.err.println("Couldn't retrieve energy list.");
            exit(1);
        }
    }

    private void displayEnergies() throws SQLException {
        ArrayList<String> energies = EnergyDAO.getInstance().getAll();
        _energyName.getItems().addAll(energies);
        _energyName.getSelectionModel().select(0);
    }

    public void submitNewVehicle() throws SQLException {
        Vehicle vehicle = processForm();
        if (vehicle != null) {
            /**
             * First, we check whether the vehicle exists.
             * If it doesn't exist, we add it to VEHICLE table.
             * If it does, then we just retrieve its data and add it
             * to the User's list.
             */
            Vehicle other = VehicleDAO.getInstance().get(vehicle.getLicensePlate());
            if (other == null)
                VehicleDAO.getInstance().save(vehicle);
            else
                vehicle = other;

            User user = ViewController.getInstance().getUser();
            user.addVehicle(vehicle);

            UserDAO.getInstance().saveAdditionVehicle(user, vehicle);
            ConnectionDB.getInstance().commit();

            ViewController.getInstance().switchView("vehiculesScreen");
        }
    }

    @Override
    public Vehicle processForm() {
        _status.setFill(Color.BLACK);
        _status.setText("");

        if (!createConditions()) {
            _status.setFill(Color.RED);
            _status.setText("Certains champs sont invalides.");
            return null;
        }

        Vehicle vehicle = new Vehicle(_licensePlate.getText(),
                _brand.getText(), _model.getText(),
                _energyName.getSelectionModel().getSelectedItem(),
                parseInt(_fiscalPower.getText()), parseInt(_seatsNumber.getText()));

        return vehicle;
    }

    @Override
    public boolean createConditions() {
        Validator validator = new Validator();

        // We validate immatriculation format : max 10 characters
        validator.addCondition(_licensePlate,
                _licensePlate.getText().length() <= 10,
                "L'immatriculation ne peut pas faire plus de 10 caractères.");

        // We validate that brand and model aren't null
        validator.addCondition(_brand,
                Validator.notEmpty(_brand),
                "La marque ne peut pas etre vide.");
        validator.addCondition(_model,
                Validator.notEmpty(_model),
                "Le modèle ne peut pas etre vide.");

        // We need to validate whether fiscal power is a number
        // and is high enough
        validator.addCondition(_fiscalPower,
                Validator.isNumber(_fiscalPower) && Validator.getInteger(_fiscalPower) >= 1,
                "La puissance fiscale doit etre un entier supérieur posifif non nul.");

        // Then, we validate whether the number of seats is a number
        // and is non-zero
        validator.addCondition(_seatsNumber,
                Validator.isNumber(_seatsNumber) && Validator.getInteger(_seatsNumber) > 0,
                "Le nombre de places doit etre un entier au moins égal à 1.");

        return validator.validateFields();
    }
}
