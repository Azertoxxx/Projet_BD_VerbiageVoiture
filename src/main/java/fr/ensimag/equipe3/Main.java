package fr.ensimag.equipe3;


import fr.ensimag.equipe3.controller.ViewController;
import javafx.application.Application;
import javafx.stage.Stage;

/**
 * Main class for the project : makes a first stage and loads the
 * login view.
 */
public final class Main extends Application {
    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(final Stage primaryStage) {
        primaryStage.setTitle("VerbiageVoiture - Identification");
        ViewController.getInstance().initView(primaryStage);
    }
}
