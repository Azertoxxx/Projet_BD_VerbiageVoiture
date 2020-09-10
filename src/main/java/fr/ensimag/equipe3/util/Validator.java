package fr.ensimag.equipe3.util;

import javafx.geometry.Side;
import javafx.scene.control.*;
import javafx.util.Pair;
import org.apache.commons.lang.math.NumberUtils;

import java.time.Duration;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.lang.Double.parseDouble;
import static java.lang.Integer.parseInt;

public final class Validator {

    /** This is the array that contains all the contextual menus displayed */
    private ArrayList<ContextMenu> _hints = new ArrayList<ContextMenu>();

    /** This is the data structure that associates : <br>
     * - a field <br>
     * - a condition <br>
     * - a message to show if the condition is false
     */
    private HashMap<Control, Pair<Boolean, String>> _conditions = new HashMap<Control, Pair<Boolean, String>>();

    /**
     * Adds a condition to be checked.
     *
     * @param control       The field we are working on.
     * @param condition     The condition needed to be satisfied.
     * @param message       The message we display if the condition is false.
     */
    public void addCondition(Control control, Boolean condition, String message) {
        _conditions.put(control, new Pair<Boolean, String>(condition, message));
    }

    /**
     * This function is called when we want to check all the conditions.
     *
     * @return  True if all the conditions are true, false otherwise.
     */
    public boolean validateFields() {
        boolean valid = true;
        for (Map.Entry<Control, Pair<Boolean, String>> entry : _conditions.entrySet()) {
            if (!entry.getValue().getKey()) {
                invalidateField(entry.getKey(), entry.getValue().getValue());
                valid = false;
            }
        }
        return valid;
    }

    /**
     * Creates a new contextual menu and displays it near
     * the invalid field with the given message.
     *
     * @param node      The field to modify.
     * @param message   The message to display in the contextual menu.
     */
    private void invalidateField(Control node, String message) {
        node.setStyle("-fx-control-inner-background: #F08080");
        final ContextMenu tooltip = new ContextMenu();
        final MenuItem hint = new MenuItem(message);
        tooltip.getItems().add(hint);
        tooltip.setStyle("-fx-control-inner-background: #FFFFFF");
        tooltip.show(node, Side.RIGHT, 0, 0);
        _hints.add(tooltip);
    }

    public static boolean isNumber(TextInputControl field) {
        return NumberUtils.isNumber(field.getText());
    }

    public static boolean isValidHour(TextInputControl field) {
        Pattern pattern = Pattern.compile("^([0-1]?[0-9]|2[0-3]):[0-5][0-9]$");
        Matcher matcher = pattern.matcher(field.getText());
        return matcher.matches();
    }

    public static int getInteger(TextInputControl field) {
        return parseInt(field.getText());
    }

    public static double getDouble(TextInputControl field) {
        return parseDouble(field.getText());
    }

    public static boolean notEmpty(TextInputControl field) {
        return !field.getText().trim().isEmpty();
    }

    public static boolean validPostalCode(String postalCode) {
        return postalCode.length() == 5;
    }

    public static LocalTime stringToDuration(String duration) {
        return LocalTime.parse(duration);
    }

    public static int timeToMinutes(LocalTime time) {
        return time.getHour() * 60 + time.getMinute();
    }

    public static LocalTime minutesToTime(int minutes) {
        LocalTime t = LocalTime.MIN;
        while (minutes % 60 != 0) {
            t = t.plusHours(1);
            minutes %= 60;
        }
        t = t.plusMinutes(minutes);
        return t;
    }
}
