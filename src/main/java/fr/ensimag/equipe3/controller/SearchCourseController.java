package fr.ensimag.equipe3.controller;

import fr.ensimag.equipe3.model.City;
import fr.ensimag.equipe3.model.DAO.CityDAO;
import fr.ensimag.equipe3.util.GeoApi;
import fr.ensimag.equipe3.util.Validator;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.util.Pair;

import java.sql.SQLException;

import static java.lang.Integer.parseInt;

public class SearchCourseController extends HomeController {
    @FXML
    private ComboBox<City> _ldepart;

    @FXML
    private ComboBox<City> _larrivee;

    @FXML
    private TextField _startPostalCode;

    @FXML
    private TextField _endPostalCode;

    @FXML
    private Text _status;

    @FXML
    public void onEnter(ActionEvent actionEvent) throws SQLException {
        search();
    }

    @Override
    @FXML
    public void initialize() throws NotConnectedException {
        super.initialize();
        _startPostalCode.textProperty().addListener(((observable, oldValue, newValue) -> {
            if (Validator.isNumber(_startPostalCode) && Validator.validPostalCode(newValue)) {
                try {
                    _ldepart.getItems().clear();
                    _ldepart.getItems().addAll(GeoApi.getCitiesFromPostalCode(parseInt(newValue)));
                    _ldepart.getSelectionModel().select(0);
                } catch (Exception e) {
                    /* do nothing */
                }
            }
        }));
        _endPostalCode.textProperty().addListener(((observable, oldValue, newValue) -> {
            if (Validator.isNumber(_endPostalCode) && Validator.validPostalCode(newValue)) {
                try {
                    _larrivee.getItems().clear();
                    _larrivee.getItems().addAll(GeoApi.getCitiesFromPostalCode(parseInt(newValue)));
                    _larrivee.getSelectionModel().select(0);
                } catch (Exception e) {
                    /* do nothing */
                }
            }
        }));
    }

    public void search() throws SQLException {
        City startCity = _ldepart.getSelectionModel().getSelectedItem();
        City endCity = _larrivee.getSelectionModel().getSelectedItem();

        /*
        City otherStart = CityDAO.getInstance().get(new Pair<>(startCity.getName(), startCity.getPostalCode()));
        if (otherStart == null) {
            _status.setFill(Color.RED);
            _status.setText("Il n'existe aucun trajet partant de cette ville.");
            return;
        }
        if (endCity != null) {
            City otherEnd = CityDAO.getInstance().get(new Pair<>(endCity.getName(), endCity.getPostalCode()));
            if (otherEnd == null) {
                _status.setFill(Color.RED);
                _status.setText("Il n'existe aucun trajet partant de cette ville.");
                return;
            }
        }
         */

        ViewController.getInstance().setStartCity(startCity);
        ViewController.getInstance().setEndCity(endCity);
        ViewController.getInstance().switchView("resultResearchScreen");
    }
}
