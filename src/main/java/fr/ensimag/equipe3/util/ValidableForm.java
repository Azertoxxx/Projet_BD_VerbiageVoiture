package fr.ensimag.equipe3.util;

import java.sql.SQLException;

/**
 * This interface gives the possibility to validate
 * different fields in a form.
 */
public interface ValidableForm<T> {
    /**
     * This is the method that has to be implemented in order
     * to specify the validation conditions.
     *
     * @return True if all conditions are valid, false otherwise.
     */
    boolean createConditions();

    T processForm() throws SQLException;
}
