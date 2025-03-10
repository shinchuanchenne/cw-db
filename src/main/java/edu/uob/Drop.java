package edu.uob;
import java.io.*;

/*
DROP DATABASE database_name;
DROP TABLE table_name;
 */

public class Drop {

    public static String setDrop(String command, String currentDatabase) {

        // Check current Database is existed or not?
        if (currentDatabase == null) {
            return "[ERROR] You must define a database first";
        }

        // Find keyword;
        String[] word = command.trim().replace(";","").split(" ");

        // Check the syntax (DROP DATABASE/TABLE name) only 3 words.
        if (word.length != 3) {
            return "[ERROR] Invalid command syntax. Syntax: drop [database] [table]";
        }

        switch (word[1].toLowerCase()) {
            case "database":
                return dropDatabase(word, currentDatabase);
            case "table":
                return dropTable(word, currentDatabase);
            default:
                return "[ERROR] Invalid Drop command";
        }
    }

    public static String dropDatabase(String[] word, String currentDatabase) {
        // Get database's name
        String databaseName = word[2].trim().replace(";","");
        File dbFolder = new File("databases" + File.separator + databaseName);
        // Search whether database is existed.
        if (!dbFolder.exists()) {
            return "[ERROR] Database does not exist";
        }
        if (currentDatabase.equals(databaseName)) {
            currentDatabase = null;
        }
        // Delete selected database folder.
        return deleteRecursively(dbFolder);
    }
    public static String dropTable(String[] word, String currentDatabase) {
        // Get table name
        String tableName = word[2].trim().toLowerCase().replace(";","").concat(".tab");
        File tabFile = new File("databases" + File.separator + currentDatabase + File.separator + tableName);
        // Check whether table name exist.
        if (!tabFile.exists()) {
            return "[ERROR] Table not found";
        }
        tabFile.delete();
        if (!tabFile.exists()) {
            return "[OK]";
        }
        return "[ERROR] Could not delete tab file";
    }

    public static String deleteRecursively(File folder){
        File[] files = folder.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    deleteRecursively(file);
                }
                file.delete();
            }
        }

        if (!folder.delete()) {
            return "[ERROR] Failed to delete folder: " + folder.getName();
        }
        return "[OK]";
    }
}
