package edu.uob;

public class ErrorHandling {
    public static String plainTextCheck(String name, String type) {
        if (!name.matches("^[a-zA-Z0-9]+$")) {
            return "[ERROR] " + type + " must contain only alphanumeric characters";
        }
        if (name.equalsIgnoreCase("use")||name.equalsIgnoreCase("create")||
                name.equalsIgnoreCase("drop") || name.equalsIgnoreCase("alter") ||
                name.equalsIgnoreCase("insert") || name.equalsIgnoreCase("select") ||
                name.equalsIgnoreCase("update") || name.equalsIgnoreCase("delete") ||
                name.equalsIgnoreCase("join") || name.equalsIgnoreCase("table") ||
                name.equalsIgnoreCase("database") || name.equalsIgnoreCase("into") ||
                name.equalsIgnoreCase("values") || name.equalsIgnoreCase("from")||
                name.equalsIgnoreCase("where") || name.equalsIgnoreCase("set") ||
                name.equalsIgnoreCase("and") || name.equalsIgnoreCase("or") ||
                name.equalsIgnoreCase("on")) {
            return "[ERROR] " + type + " must not contain reserved words";
        }
        return "[OK]";
    }

    public static String valueCheck(String value) {
        value = value.trim();

        if (value.equalsIgnoreCase("use")|| value.equalsIgnoreCase("create")||
                value.equalsIgnoreCase("drop") || value.equalsIgnoreCase("alter") ||
                value.equalsIgnoreCase("insert") || value.equalsIgnoreCase("select") ||
                value.equalsIgnoreCase("update") || value.equalsIgnoreCase("delete") ||
                value.equalsIgnoreCase("join") || value.equalsIgnoreCase("table") ||
                value.equalsIgnoreCase("database") || value.equalsIgnoreCase("into") ||
                value.equalsIgnoreCase("values") || value.equalsIgnoreCase("from")||
                value.equalsIgnoreCase("where") || value.equalsIgnoreCase("set") ||
                value.equalsIgnoreCase("and") || value.equalsIgnoreCase("or") ||
                value.equalsIgnoreCase("on")) {
            return "[ERROR] Value must not contain reserved words";
        }

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
