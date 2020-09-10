package fr.ensimag.equipe3.controller;

import fr.ensimag.equipe3.model.DAO.UserDAO;
import fr.ensimag.equipe3.model.Gender;
import fr.ensimag.equipe3.model.User;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

import java.net.URL;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * This is the base controller for the lateral panel and header bar
 * of the application.
 * It initializes the user avatar, displayed first and last name accordingly.
 *
 * @pbdco.switches {@link VehiculesController}
 * @pbdco.switches {@link LoginController}
 *
 * @pbdco.db_modified
 */
public class HomeController {
    /** The user's first name */
    @FXML
    private Text _firstName;

    /** The user's last name */
    @FXML
    private Text _name;

    /** The user's avatar */
    @FXML
    private ImageView _avatar;

    /** The link to vehicle list view */
    @FXML
    private Text _vehiclesLink;

    /** The link to wallet management view */
    @FXML
    private Text _walletLink;

    /** The link to the user's course list view */
    @FXML
    private Text _courseLink;

    /** The link to logout */
    @FXML
    private Text _logoutLink;

    @FXML
    private VBox _centerPanel;

    /**
     * This method chooses the correct avatar according to the user's
     * gender. It sets the first and last names labels to the correct value.
     */
    @FXML
    public void initialize() throws NotConnectedException {
        // Make all links clickable
        ViewController.getInstance().makeAllClickable(new ArrayList<Node>(Arrays.asList(_vehiclesLink,
                                                                                        _walletLink,
                                                                                        _courseLink,
                                                                                        _logoutLink)));

        // Set names accordingly
        User user = ViewController.getInstance().getUser();
        if (user == null) {
            System.err.println("No user logged in");
            throw new NotConnectedException("You have to log in before using this view.");
        }

        _firstName.setText(user.getFirstName());
        _name.setText(user.getName());

        Gender gender = user.getGender();
        String avatarResource = "";
        switch (gender) {
        case MALE:
            avatarResource = "male.png";
            break;

        case FEMALE:
            avatarResource = "female.png";
            break;

        default:
            avatarResource = "unspecified.jpg";
        }

        URL link = getClass().getResource(avatarResource);
        Image avatarImage = new Image(link.toString());
        _avatar.setImage(avatarImage);
    }

    /**
     * Logs the current user out: the user is removed from the view controller
     * and we switch back to login screen.
     *
     * NB: we need to update the current user data, in case it has been modified.
     * This method is inherited, so even if this class doesn't allow the current
     * user to be modified, a subclass might modify it.
     *
     * @throws SQLException     When the update fails.
     */
    public void logout() throws SQLException {
        UserDAO.getInstance().update(ViewController.getInstance().getUser());
        ViewController.getInstance().setUser(null);
        ViewController.getInstance().switchView("loginScreen");
    }

    /**
     * Switches to vehicles list view.
     */
    public void toVehicules() {
        ViewController.getInstance().switchView("vehiculesScreen");
    }

    /**
     * Switches to wallet management view.
     */
    public void toWallet() {
        ViewController.getInstance().switchView("walletScreen");
    }

    /**
     * Switches to search course view.
     */
    public void toSearchCourse() {
        ViewController.getInstance().switchView("searchCourseScreen");
    }

    /**
     * Switches to new path view.
     */
    public void toNewPath() {
        ViewController.getInstance().switchView("newPathScreen");
    }

    /**
     * Switches to path list.
     */
    public void toPathList() {
        ViewController.getInstance().switchView("pathListScreen");
    }
}
