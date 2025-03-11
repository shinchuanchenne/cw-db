package edu.uob;

public class ErrorHandling {
    public static String plainTextCheck(String name, String type) {
        if (!name.matches("^[a-zA-Z0-9]+$")) {
            return "[ERROR] " + type + " must contain only alphanumeric characters";
        }
        return "[OK]";
    }

    public static String valueCheck(String value) {
        value = value.trim();
        if (value.equalsIgnoreCase("null")) {
            return "[OK]";
        }
        if (value.equalsIgnoreCase("true") || value.equalsIgnoreCase("false")) {
            return "[OK]";
        }
        if (value.matches("[-+]?\\d+")) {
            return "[OK]";
        }
        if (value.matches("[-+]?\\d+\\.\\d+")) {
            return "[OK]";
        }
        if (value.matches("'[^']*'")) {
            return "[OK]";
        }
        return "[ERROR] Invalid value name format: " + value;
    }
}
