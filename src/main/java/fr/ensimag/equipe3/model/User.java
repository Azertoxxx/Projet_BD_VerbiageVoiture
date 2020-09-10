package fr.ensimag.equipe3.model;

import java.util.ArrayList;
import java.util.Objects;

public class User {
    private String _email;
    private String _firstName;
    private String _name;
    private Gender _gender;
    private String _password;
    private double _solde;
    private City _city;
    private ArrayList<Vehicle> _vehicles;

    public User(String email, String firstName, String name, Gender gender,
                City city, String password, double solde, ArrayList<Vehicle> vehicles) {

        this.setSolde(solde);
        _email = email;
        _firstName = firstName;
        _name = name;
        _gender = gender;
        _city = city;
        _password = password;
        _vehicles = vehicles;
    }

    public User(String email, String firstName, String name, Gender gender,
                City city, String password) {

        this.setSolde(0);
        _email = email;
        _firstName = firstName;
        _name = name;
        _gender = gender;
        _city = city;
        _password = password;
        _vehicles = new ArrayList<Vehicle>();
    }

    public void setSolde(double solde) {
        if (solde < 0) {
            throw new IllegalArgumentException("The solde must be greater than 0 or equal.");
        }
        this._solde = solde;
    }

    public void addVehicle(Vehicle vehicle) {
        _vehicles.add(vehicle);
    }

    public void removeVehicle(Vehicle vehicle) {
        _vehicles.remove(vehicle);
    }

    public Iterable<Vehicle> getVehicles() {
        return _vehicles;
    }

    public boolean possessVehicle(Vehicle vehicle) {
        return _vehicles.contains(vehicle);
    }

    public String getEmail() {
        return _email;
    }

    public String getFirstName() {
        return _firstName;
    }

    public String getName() {
        return _name;
    }

    public Gender getGender() {
        return _gender;
    }

    public City getCity() {
        return _city;
    }

    public String getPassword() {
        return _password;
    }

    public double getSolde() {
        return _solde;
    }


    public boolean checkCredentials(String password) {
        return password.equals(_password);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof User)) return false;
        User user = (User) o;
        return _email.equals(user._email);
    }

    @Override
    public int hashCode() {
        return Objects.hash(_email);
    }
}
