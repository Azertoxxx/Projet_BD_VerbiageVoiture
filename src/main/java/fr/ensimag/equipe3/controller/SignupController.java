package fr.ensimag.equipe3.controller;

import fr.ensimag.equipe3.model.City;
import fr.ensimag.equipe3.model.DAO.CityDAO;
import fr.ensimag.equipe3.model.DAO.ConnectionDB;
import fr.ensimag.equipe3.model.DAO.UserDAO;
import fr.ensimag.equipe3.model.Gender;
import fr.ensimag.equipe3.model.User;
import fr.ensimag.equipe3.util.GeoApi;
import fr.ensimag.equipe3.util.ValidableForm;
import fr.ensimag.equipe3.util.Validator;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.util.Pair;

import java.sql.SQLException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.lang.Integer.parseInt;

/**
 * This is the controller for the signup view.
 * It handles the creation of a new user and saves it
 * to the database if all the provided information is valid.
 *
 * @pbdco.switches {@link LoginController}
 *
 * @pbdco.db_modified
 */
public class SignupController implements ValidableForm<User> {
    @FXML
    private TextField _email;

    @FXML
    private TextField _firstName;

    @FXML
    private TextField _name;

    @FXML
    private ComboBox<City> _city;

    @FXML
    private TextField _postalCode;

    @FXML
    private PasswordField _password;

    @FXML
    private PasswordField _confirmPassword;

    @FXML
    private Text _status;

    @FXML
    private ToggleGroup group1;

    @FXML
    private Text _accountLink;

    /**
     * Makes the link to login view clickable.
     */
    @FXML
    public void initialize() {
        ViewController.getInstance().makeClickable(_accountLink);

        _postalCode.textProperty().addListener(((observable, oldValue, newValue) -> {
            if (Validator.isNumber(_postalCode) && Validator.validPostalCode(newValue)) {
                try {
                    _city.getItems().clear();
                    _city.getItems().addAll(GeoApi.getCitiesFromPostalCode(parseInt(newValue)));
                    _city.getSelectionModel().select(0);
                } catch (Exception e) {
                    /* do nothing */
                }
            }
        }));
    }

    /**
     * Allows the user to submit the subscription form by pressing
     * 'Enter' rather than clicking on 'Submit' button.
     *
     * @param actionEvent   Parameter required by FXML, not used here.
     *
     * @throws SQLException
     */
    @FXML
    public void onEnter(ActionEvent actionEvent) throws SQLException {
        inscription();
    }

    /**
     * Processes the signup form and adds the new user, if everything is valid,
     * to the database.
     *
     * @throws SQLException
     */
    public void inscription() throws SQLException  {
        User user = processForm();
        if (user != null) {
            // First, we check whether the user already exists
            User other = UserDAO.getInstance().get(user.getEmail());
            if (other != null) {
                _status.setText("Cet utilisateur existe déjà.");
                _status.setFill(Color.RED);
                return;
            }

            UserDAO.getInstance().save(user);
            ConnectionDB.getInstance().commit();

            // The account has been successfuly created : we provide a link to retun to login view
            _status.setText("Votre compte a bien été créé. Cliquez ici pour retourner à la page de connexion.");
            _status.setFill(Color.GREEN);
            ViewController.getInstance().makeClickable(_status);
            _status.setOnMouseClicked(new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent mouseEvent) {
                    toLogin();
                }
            });
        }
    }

    @Override
    public User processForm() throws SQLException {
        _status.setFill(Color.BLACK);
        _status.setText("");

        if (!createConditions()) {
            _status.setFill(Color.RED);
            _status.setText("Certains champs sont invalides.");
            return null;
        }


        // We set the gender of the new user according to their choice
        Gender gender = Gender.UNSPECIFIED;
        if (group1.getSelectedToggle() != null) {
            RadioButton choice = (RadioButton) group1.getSelectedToggle();
            if (choice.getText().equals("Homme"))
                gender = Gender.MALE;
            else if (choice.getText().equals("Femme"))
                gender = Gender.FEMALE;
        }

        // We create the user only if password == password confirmation
        if (_password.getText().equals(_confirmPassword.getText())) {
            // The postal code and city name provided identify a City object
            City city = _city.getSelectionModel().getSelectedItem();
            CityDAO.getInstance().save(city);

            // Now we can create the complete User and save it to the database
            User user = new User(_email.getText(),
                    _firstName.getText(),
                    _name.getText(),
                    gender,
                    city,
                    _password.getText());
            return user;
        }

        _status.setText("Les deux mots de passe ne correspondent pas.");
        _status.setFill(Color.RED);
        return null;
    }

    @Override
    public boolean createConditions() {
        Validator validator = new Validator();
        // First, we're going to check whether the provided email is valid, this means
        // that follows the scheme: aaa@bbb.cc
        // This regex comes from http://emailregex.com
        Pattern p = Pattern.compile("(?:[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*|\"(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21\\x23-\\x5b\\x5d-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])*\")@(?:(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?|\\[(?:(?:(2(5[0-5]|[0-4][0-9])|1[0-9][0-9]|[1-9]?[0-9]))\\.){3}(?:(2(5[0-5]|[0-4][0-9])|1[0-9][0-9]|[1-9]?[0-9])|[a-z0-9-]*[a-z0-9]:(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21-\\x5a\\x53-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])+)\\])");
        Matcher m = p.matcher(_email.getText());
        validator.addCondition(_email,
                m.matches(),
                "Le format de l'e-mail fourni est invalide.");

        // Then, we check that first and last names and city name are not null
        validator.addCondition(_firstName,
                Validator.notEmpty(_firstName),
                "Le prénom ne peut pas etre vide.");
        validator.addCondition(_name,
                Validator.notEmpty(_name),
                "Le nom ne peut pas etre vide.");

        // We check whether the postal code is a rightful number
        validator.addCondition(_postalCode,
                Validator.isNumber(_postalCode) && Validator.getInteger(_postalCode) >= 10,
                "Le code postal doit etre un entier supérieur à 10.");

        // We want the password to be 12-character long, and contain one capital letter and one special character
        String password = _password.getText();
        boolean lengthCondition = password.length() >= 12;
        boolean upperCaseCondition = password.chars().anyMatch(Character::isUpperCase);
        boolean lowerCaseCondition = password.chars().anyMatch(Character::isLowerCase);
        boolean numberCondition = password.chars().anyMatch(Character::isDigit);
        boolean specialCondition = Pattern.compile("[^A-Za-z0-9 ]").matcher(password).find();
        validator.addCondition(_password,
                lengthCondition && upperCaseCondition && lowerCaseCondition && numberCondition && specialCondition,
                "Le mot de passe doit faire au moins 12 caractères, et doit contenir au moins un chiffre, une lettre majuscule, une lettre minuscule et un caractère spécial.");

        return validator.validateFields();
    }

    /**
     * The method is called when the user clicks on the link to
     * go back to the login view.
     */
    public void toLogin() {
        ViewController.getInstance().switchView("loginScreen");
    }

}
