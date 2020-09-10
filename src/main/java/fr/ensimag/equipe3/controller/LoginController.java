package fr.ensimag.equipe3.controller;

import fr.ensimag.equipe3.model.DAO.UserDAO;
import fr.ensimag.equipe3.model.User;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;

import java.sql.SQLException;

/**
 * This is the controller for the login view.
 * It handles the user's input and checks whether the submitted
 * data is valid.
 *
 * @pbdco.switches  {@link SignupController}
 * @pbdco.switches  {@link PathListController}
 *
 * @pbdco.db_unmodified
 */
public class  LoginController {

    @FXML
    private TextField _email;

    @FXML
    private PasswordField _password;

    @FXML
    private Label _status;

    @FXML
    private Text _noAccountLink;

    /**
     * Allows the to-signup-view link at the bottom of the screen
     * to be clickable i.e. cursor changes when hovered.
     */
    @FXML
    public void initialize() {
        ViewController.getInstance().makeClickable(_noAccountLink);

    }

    /**
     * This method is called when the user clicks on the button labelled "Connect".
     * It gets a user from the database with the email provided, and displays an error
     * message if the user doesn't exist or the password provided doesn't match the database's one.
     *
     * @throws SQLException
     */
    public void connexion() throws SQLException  {
        // First we reset the status label
        _status.setText("");
        _status.setTextFill(Color.BLACK);

        // Retrieve an user with provided email
        User user = UserDAO.getInstance().get(_email.getText());

        // Check whether user exists or password is valid
        if (user == null || !user.checkCredentials(_password.getText())) {
            _status.setText("L'adresse e-mail et/ou le mot de passe sont invalides.");
            _status.setTextFill(Color.RED);
            _status.setStyle("-fx-wont-weight: bold;");
        }
        else {  // If correct, we save the current user and switch to home screen
            ViewController.getInstance().setUser(user);
            ViewController.getInstance().switchView("pathListScreen");
        }
    }

    /**
     * Switches to signup view.
     */
    public void toSignUp() {
        ViewController.getInstance().switchView("signupScreen");
    }

    /**
     * This function simply allows the user to submit the form
     * by pressing the 'Enter' key rather than using the 'Connect' button.
     *
     * @param actionEvent   This parameter is required but not used.
     *
     * @throws SQLException
     */
    @FXML
    public void onEnter(ActionEvent actionEvent) throws SQLException {
        connexion();
    }
}
