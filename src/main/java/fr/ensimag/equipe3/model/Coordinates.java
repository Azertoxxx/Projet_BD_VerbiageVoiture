package fr.ensimag.equipe3.model;

import javafx.util.Pair;

public class Coordinates {
    private double _latitude;
    private double _longitude;
    private City _city;

    public Coordinates(double latitude, double longitude, City city) {
        this.setCity(city);
        this._latitude = latitude;
        this._longitude = longitude;
    }

    private void setCity(City city) {
        if (city == null) {
            throw new IllegalArgumentException("Coordinates's city cannot be null.");
        }
        this._city = city;
    }

    public double getLatitude() {
        return _latitude;
    }

    public double getLongitude() {
        return _longitude;
    }

    public City getCity() {
        return _city;
    }

    public Pair<Double, Double> getPair() {
        return new Pair<Double, Double>(_latitude, _longitude);
    }

}
