package fr.ensimag.equipe3.model.DAO;

import java.sql.SQLException;

/**
 * This interface is meant to provide a template for objects that
 * communicate with a database.
 *
 * @param <T>   The type of the Object as represented in the model.
 * @param <S>   The type of the primary key identifiyng the object in the database.
 */
public interface Dao<T, S> {

    /**
     * Constructs a new object using data retrieved in the database.
     *
     * @param id    The primary key that identifies the object.
     *
     * @return      A model object made with data from the database.
     * @throws SQLException When the SELECT query fails.
     */
    T get(S id) throws SQLException;

    /**
     * Adds a row in the database with data from the object passed
     * as a parameter.
     *
     * @param t     The object representing the new row in the database.
     *
     * @throws SQLException When the INSERT query fails.
     */
    void save(T t) throws SQLException;

    /**
     * Replaces the information in an existing row with data gathered
     * by the object passed as a parameter.
     *
     * @param t     The object representing the existing row in the database.
     *
     * @throws SQLException When the UPDATE query fails.
     */
    void update(T t) throws SQLException;
}
