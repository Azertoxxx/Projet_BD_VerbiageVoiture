package fr.ensimag.equipe3.model;

import fr.ensimag.equipe3.model.DAO.PathDAO;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Objects;

/**
 * Represents a course of a passenger.
 */
public class Course {

    private int _idCourse;
    private User _user;
    private ArrayList<Section> _sections = new ArrayList<Section>();

    /**
     * Constructor
     * @param idCourse Identifier of the course
     * @param user User which reserves the course, cannot be null.
     * @param sections Reserved sections, at least one.
     */
    public Course(int idCourse, User user, ArrayList<Section> sections) {
        this.setUser(user);
        this.setSections(sections);
        _idCourse = idCourse;
    }

    public int getIdCourse() {
        return _idCourse;
    }

    public void setIdCourse(int idCourse) { _idCourse = idCourse;}

    public User getUser() {
        return _user;
    }

    public Iterable<Section> getSections() {
        return _sections;
    }

    public Section getFirstSection() { return _sections.get(0); }

    public Section getLastSection() {
        return _sections.get(getNumberOfSections() - 1);
    }

    public int getNumberOfSections() {
        return _sections.size();
    }

    public City getStart() {
        return _sections.get(0).getStart();
    }

    public City getEnd() {
        return _sections.get(_sections.size() - 1).getEnd();
    }

    private void setUser(User user) {
        if (user == null) {
            throw new IllegalArgumentException("Course's user cannot ne null.");
        }
        _user = user;
    }

    private void setSections(ArrayList<Section> sections) {
        if (sections == null) {
            throw new IllegalArgumentException("Course must contain at least one section.");
        }
        _sections = sections;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Course course = (Course) o;
        return _idCourse == course._idCourse;
    }

    @Override
    public int hashCode() {
        return Objects.hash(_idCourse);
    }
}
