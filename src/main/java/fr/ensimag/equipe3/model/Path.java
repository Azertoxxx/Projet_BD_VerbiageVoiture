package fr.ensimag.equipe3.model;

import java.sql.Date;
import java.time.LocalTime;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

/**
 * Represents a driver's path.
 */
public class Path {

    private int _idPath;
    private Date _date;
    private LocalTime _hour;
    private int _nbSeats;
    private User _user;
    private Vehicle _vehicule;
    private ArrayList<Section> _sections;

    public Path(int idPath, User user, Vehicle vehicule, int nbSeats, Date date, LocalTime hour) {
        this._idPath = idPath;
        this._date = date;
        this._hour = hour;
        this._nbSeats = nbSeats;
        this._user = user;
        this._vehicule = vehicule;
        this._sections = new ArrayList<Section>();
    }

    public Iterable<Section> getSections() { return _sections; }

    public Section getSection(int index) {
        return _sections.get(index);
    }

    public Section getLastSection() {
        return _sections.get(_sections.size() - 1);
    }

    public int getNumberOfSections() {
        return _sections.size();
    }

    public List<Section> sublistSections(int start, int end) {
        return _sections.subList(start, end);
    }

    public void addSection(Section section) {
        _sections.add(section);
    }

    public void addAllSections(Collection<? extends Section> sections) {
        _sections.addAll(sections);
    }


    public int getIdPath() { return _idPath; }

    public Date getDate() { return _date; }

    public LocalDate getLocalDate() {
        return _date.toLocalDate();
    }

    public LocalTime getHour() { return _hour; }

    public int getNbSeats() { return _nbSeats; }

    public User getUser() { return _user; }

    public Vehicle getVehicle() { return _vehicule; }

    public City getStart() {
        return this.getSection(0).getStartCoordinates().getCity();
    }

    public City getEnd() {
        return this.getLastSection().getEndCoordinates().getCity();
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Path path = (Path) o;
        return _idPath == path._idPath;
    }

    @Override
    public int hashCode() {
        return Objects.hash(_idPath);
    }
}
