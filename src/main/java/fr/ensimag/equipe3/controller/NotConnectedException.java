package fr.ensimag.equipe3.controller;

/**
 * This exception is thrown when we try to load a view that
 * needs a connected user to be initialized, and user is set to null.
 */
public class NotConnectedException extends Exception {
    public NotConnectedException(String message) {
        super(message);
    }
}
