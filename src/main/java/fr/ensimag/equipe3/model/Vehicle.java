package fr.ensimag.equipe3.model;

import java.util.Objects;

public class Vehicle {
    private String _licensePlate;
    private int _fiscalPower;
    private int _seatsNumber;
    private String _model;
    private String _brand;
    private String _energy;

    public Vehicle(String licensePlate, String brand, String model, String energy,
                   int fiscalPower, int seatsNumber) {
        this.setSeatsNumber(seatsNumber);
        this.setFiscalPower(fiscalPower);
        _licensePlate = licensePlate;
        _brand = brand;
        _model = model;
        _energy = energy;
    }

    private void setFiscalPower(int fiscalPower) {
        if (fiscalPower < 1) {
            throw new IllegalArgumentException("The fiscal power must be greater than 0.");
        }
        this._fiscalPower = fiscalPower;
    }

    private void setSeatsNumber(int seatsNumber) {
        if (seatsNumber < 1) {
            throw new IllegalArgumentException("The seat count must be greater than 0.");
        }
        this._seatsNumber = seatsNumber;
    }

    public String getLicensePlate() {
        return _licensePlate;
    }

    public int getFiscalPower() {
        return _fiscalPower;
    }

    public int getSeatsNumber() {
        return _seatsNumber;
    }

    public String getModel() {
        return _model;
    }

    public String getBrand() {
        return _brand;
    }

    public String getEnergy() {
        return _energy;
    }


    @Override
    public String toString() {
        return _licensePlate;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Vehicle)) return false;
        Vehicle vehicle = (Vehicle) o;
        return _licensePlate.equals(vehicle._licensePlate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(_licensePlate);
    }
}
