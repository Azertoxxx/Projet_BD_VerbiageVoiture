package fr.ensimag.equipe3.controller;

import fr.ensimag.equipe3.model.DAO.ConnectionDB;
import fr.ensimag.equipe3.model.DAO.UserDAO;
import fr.ensimag.equipe3.model.User;
import fr.ensimag.equipe3.util.ValidableForm;
import fr.ensimag.equipe3.util.Validator;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;

import java.sql.SQLException;

public class WalletController extends HomeController implements ValidableForm<Double> {
    @FXML
    private Label _solde;

    @FXML
    private TextField _amount;

    @FXML
    private TextField _cardNumber;

    @FXML
    private Text _status;

    @Override
    @FXML
    public void initialize() throws NotConnectedException {
       super.initialize();

        User user = ViewController.getInstance().getUser();
        if (user == null) {
            System.err.println("No user logged in");
            throw new NotConnectedException("You have to log in before using this view.");
        }
        //display user's solde
        double solde = user.getSolde();
        String soldeStr = String.valueOf(solde);
        _solde.setText(soldeStr + "€");
    }

    public void credit() throws SQLException {
        double amount = processForm();

        if (amount != -1) {
            User user = ViewController.getInstance().getUser();

            double newSolde = user.getSolde() + amount;
            user.setSolde(newSolde);
            UserDAO.getInstance().update(user);
            ConnectionDB.getInstance().commit();

            ViewController.getInstance().switchView("walletScreen");
        }
    }

    public void debit() throws SQLException {
        Double amount = processForm();

        if (amount != -1) {
            User user = ViewController.getInstance().getUser();

            if (amount > user.getSolde()) {
                _status.setFill(Color.RED);
                _status.setText("Vous n'avez pas assez de crédit pour débiter cette somme.");
            } else {
                double newSolde = user.getSolde() - amount;
                user.setSolde(newSolde);
                UserDAO.getInstance().update(user);
                ConnectionDB.getInstance().commit();

                ViewController.getInstance().switchView("walletScreen");
            }
        }
    }

    @Override
    public Double processForm() {
        _status.setFill(Color.BLACK);
        _status.setText("");

        if (!createConditions()) {
            _status.setFill(Color.RED);
            _status.setText("Certains champs sont invalides.");
            return Double.valueOf(-1);
        }
        return Double.parseDouble(_amount.getText());
    }

    @Override
    public boolean createConditions() {
        Validator validator = new Validator();

        validator.addCondition(_amount,
                Validator.isNumber(_amount) && Validator.getDouble(_amount) > 0,
                "Le montant doit être un nombre positif.");

        validator.addCondition(_cardNumber,
                Validator.isNumber(_cardNumber) && _cardNumber.getLength() == 16,
                "Le numéro de carte doit être une séquence d'exactement 16 chiffres.");

        return validator.validateFields();
    }
}
