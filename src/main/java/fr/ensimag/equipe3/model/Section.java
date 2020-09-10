package fr.ensimag.equipe3.model;

import java.time.Duration;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Objects;

public class Section {
    private int _idSection;
    private int _idPath;
    private double _distance;
    private Duration _expectedDuration;
    private Duration _waitingDuration;
    private Coordinates _startCoordinates;
    private Coordinates _endCoordinates;

    public Section (int idPath, int idSection, double distance, Duration expectedDuration,
                    Duration waitingDuration, Coordinates startCoordinates, Coordinates endCoordinates) {
        this.setDistance(distance);
        this.setStartCoordinates(startCoordinates);
        this.setEndCoordinates(endCoordinates);
        _idPath = idPath;
        _idSection = idSection;
        _expectedDuration = expectedDuration;
        _waitingDuration = waitingDuration;
    }

    private void setDistance(double distance) {
        if (distance < 0) {
            throw new IllegalArgumentException("The distance must be positive.");
        }
        this._distance = distance;
    }

    private void setStartCoordinates(Coordinates startCoordinates) {
        if (startCoordinates == null) {
            throw new IllegalArgumentException("Start coordinates must be not null.");
        }
        this._startCoordinates = startCoordinates;
    }

    private void setEndCoordinates(Coordinates endCoordinates) {
        if (endCoordinates == null) {
            throw new IllegalArgumentException("End coordinates must be not null.");
        }
        this._endCoordinates = endCoordinates;
    }

    public double getDistance() { return _distance; }

    public int getIdSection() { return _idSection; }

    public int getIdPath() { return _idPath; }

    public City getStart() { return _startCoordinates.getCity(); }

    public City getEnd() { return _endCoordinates.getCity(); }

    public Duration getExpectedDuration() { return _expectedDuration; }

    public Duration getWaitingDuration() { return _waitingDuration; }

    public Coordinates getStartCoordinates() { return _startCoordinates; }

    public Coordinates getEndCoordinates() { return _endCoordinates; }

    public boolean getIsStartValidated() {
        return false;
    }

    public boolean getIsEndValidated() {
        return false;
    }

    public LocalTime getStartTime(ArrayList<Section> sections) {
        return null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Section section = (Section) o;
        return _idSection == ((Section) o)._idSection
                && _idPath == ((Section) o)._idPath;
    }

    @Override
    public int hashCode() {
        return Objects.hash(_idSection, _idPath);
    }
}
