package fr.ensimag.equipe3.model;

import javafx.util.Pair;

import java.util.Objects;

public class City {
    private String _name;
    private int _postalCode;

    public City(String _name, int _postalCode) {
        this._name = _name;
        this._postalCode = _postalCode;
    }

    public String getName() {
        return _name;
    }

    public int getPostalCode() {
        return _postalCode;
    }

    public Pair<String, Integer> getPair() {
        return new Pair<String, Integer>(_name, _postalCode);
    }

    @Override
    public String toString() {
        return _name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof City)) return false;
        City city = (City) o;
        return _name.equals(city._name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(_name, _postalCode);
    }
}
