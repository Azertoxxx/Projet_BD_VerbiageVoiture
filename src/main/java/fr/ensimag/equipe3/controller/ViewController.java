package fr.ensimag.equipe3.controller;

import fr.ensimag.equipe3.model.City;
import fr.ensimag.equipe3.model.DAO.ConnectionDB;
import fr.ensimag.equipe3.model.DAO.UserDAO;
import fr.ensimag.equipe3.model.Path;
import fr.ensimag.equipe3.model.User;
import fr.ensimag.equipe3.model.Course;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;

import static java.lang.System.exit;

/**
 * Design Pattern Singleton.
 */
public class ViewController {
    private final static ViewController instance = new ViewController();

    private HashMap<String, String> _viewNames = new HashMap<String, String>();
    private Scene _scene;

    private User _user;

    private Path _path;

    private Course _course;

    private City _startCity;

    private City _endCity;

    private String _prefix = "";

    public void makeClickable(Node node) {
        node.setOnMouseEntered(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                _scene.setCursor(Cursor.HAND);
            }
        });

        node.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                _scene.setCursor(Cursor.CLOSED_HAND);
            }
        });

        node.setOnMouseExited(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                _scene.setCursor(Cursor.DEFAULT);
            }
        });
    }

    public void makeAllClickable(ArrayList<Node> nodes) {
        for (Node n : nodes) {
            makeClickable(n);
        }
    }

    public void setLoading(boolean value) {
        _scene.setCursor(value ? Cursor.WAIT : Cursor.DEFAULT);
    }

    private ViewController() {
        _user = null;
        _viewNames.put("loginScreen", "loginScreen.fxml");
        _viewNames.put("signupScreen", "signupScreen.fxml");
        _viewNames.put("homeScreen", "homeScreen.fxml");
        _viewNames.put("vehiculesScreen", "vehiculesScreen.fxml");
        _viewNames.put("newVehicleScreen", "newVehicleScreen.fxml");
        _viewNames.put("walletScreen", "wallet.fxml");
        _viewNames.put("searchCourseScreen", "SearchCourse.fxml");
        _viewNames.put("newPathScreen", "newPath.fxml");
        _viewNames.put("resultResearchScreen", "resultResearchScreen.fxml");
        _viewNames.put("pathListScreen", "pathListScreen.fxml");
        _viewNames.put("pathInfoScreen", "pathInfoScreen.fxml");
        _viewNames.put("courseInfoScreen", "courseInfoScreen.fxml");
    }

    public void setUser(User user) {
        _user = user;
    }

    public User getUser() {
        return _user;
    }

    public void setPath(Path path) {
        _path = path;
    }

    public Path getPath() {
        return _path;
    }

    public City getStartCity() {
        return _startCity;
    }

    public void setStartCity(City city) {
        _startCity = city;
    }

    public void setCourse(Course course){ _course = course;}

    public Course getCourse() { return _course;}

    public City getEndCity() {
        return _endCity;
    }

    public void setEndCity(City city) {
        _endCity = city;
    }


    public void initView(Stage primaryStage) {
        FXMLLoader loader = new FXMLLoader(getClass().getResource(_prefix + "loginScreen.fxml"));
        try {
            Parent root = loader.load();

            _scene = new Scene(root, 800, 800);
            primaryStage.setScene(_scene);
            primaryStage.show();
        }
        catch (IOException e) {
            System.err.println("Impossible de charger la vue loginScreen.");
            System.err.println(e.getMessage());
            e.printStackTrace();
            exit(1);
        }
    }

    public static ViewController getInstance() {
        return instance;
    }

    public void switchView(String viewName) {
        if (!_viewNames.containsKey(viewName)) {
            System.err.println("La vue demandée n'existe pas");
        }
        else {

            try {
                ConnectionDB.getInstance().rollback();
            } catch (SQLException e) {
                System.err.println("Rollback fails.");
            }

            if (_user != null) {
                User save = _user;
                try {
                    _user = UserDAO.getInstance().get(_user.getEmail());
                }
                catch (SQLException e) {
                    System.err.println("Couldn't update user, keeping old value.");
                    _user = save;
                }
            }

            FXMLLoader loader = new FXMLLoader(getClass().getResource(_prefix + _viewNames.get(viewName)));
            try {
                Parent root = loader.load();
                _scene.setRoot(root);
            } catch (IOException e) {
                System.err.println("Impossible de charger la vue " + viewName + ".");
                e.printStackTrace();
                exit(1);
            }
        }
    }
}
