package edu.uob;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/*
ALTER TABLE table_name ADD attribute_name;
ALTER TABLE table_name DROP attribute_name;
 */
public class Alter {

    public static String setAlter(String command, String currentDatabase) {
        // Check current Database is existed or not?
        if (currentDatabase == null) {
            return "[ERROR] You must define a database first";
        }

        // Find keyword;
        String[] word = command.trim().replace(";","").split(" ");

        // Check the syntax (DROP DATABASE/TABLE name) only 3 words.
        if (word.length != 5) {
            return "[ERROR] Invalid Alter syntax.";
        }
        if (!word[1].toLowerCase().equals("table")) {
            return "[ERROR] Invalid Alter syntax. Missing 'TABLE' keyword.";
        }

        String alterAttribute = word[4].replace("\'", "");
        // Can not modify ID
        if (alterAttribute.equals("id")) {
            return "[ERROR] You can not modify ID";
        }

        //Check whether tableName is existed in currentDatabase.
        String tableName = word[2].trim().concat(".tab");
        File tabFile = new File("databases" + File.separator + currentDatabase + File.separator + tableName);
        if (!tabFile.exists()) {
            return "[ERROR] Table not found";
        }

        // Find add or drop
        switch (word[3].toLowerCase()) {
            case "add":
                return alter2Add(command, currentDatabase,alterAttribute, tabFile);
            case "drop":
                return alter2Drop(command, currentDatabase,alterAttribute, tabFile);
            default:
                return "[ERROR] Invalid Alter syntax. You must include 'add' or 'drop' keyword.";
        }

    }

    public static String alter2Add(String command, String currentDatabase, String alterAttribute, File tabFile) {
        // Open the file
        try (BufferedReader reader = new BufferedReader(new FileReader(tabFile))){
            String header = reader.readLine(); // Read header
            String[] attributeList = header.split("\t");

            // Check alter column is existed?
            if (Arrays.asList(attributeList).contains(alterAttribute)) {
                return "[ERROR] Column already exists";
            }

            StringBuilder newColumn = new StringBuilder();
            newColumn.append(header).append("\t").append(alterAttribute).append("\n");
            String line;
            while ((line = reader.readLine()) != null) {
                newColumn.append(line).append("\t").append("").append("\n");
            }

            // Write back file
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(tabFile))) {
                writer.write(newColumn.toString());
                return "[OK]";
            } catch (IOException e) {
                return "[ERROR] Failed to write to file";
            }
        } catch (IOException e){
            return "[ERROR] Cannot read table file";
        }

    }
    public static String alter2Drop(String command, String currentDatabase, String alterAttribute, File tabFile) {

        try (BufferedReader reader = new BufferedReader(new FileReader(tabFile))){
            String header = reader.readLine(); // Read header
            String[] attributeList = header.split("\t");

            // Check alter column is existed?
            int dropColumnIndex = -1;
            for (int i = 0; i < attributeList.length; i++) {
                if (attributeList[i].equals(alterAttribute)) {
                    dropColumnIndex = i;
                    break;
                }
            }
            if (dropColumnIndex == -1){
                return "[ERROR] Column does not exist";
            }


            StringBuilder newTable = new StringBuilder();
            // Update header
            List<String> newHeaderList = new ArrayList<>(Arrays.asList(attributeList));
            newHeaderList.remove(dropColumnIndex);
            newTable.append(String.join("\t", newHeaderList)).append("\n");

            String line;
            while ((line = reader.readLine()) != null) {
                String[] rowValues = line.split("\t");
                List<String> newRow = new ArrayList<>(Arrays.asList(rowValues));
                newRow.remove(dropColumnIndex);
                newTable.append(String.join("\t", newRow)).append("\n");
            }

            try(BufferedWriter writer = new BufferedWriter(new FileWriter(tabFile))) {
                writer.write(newTable.toString());
                return "[OK]";
            } catch (IOException e) {
                return "[ERROR] Cannot write to file";
            }

        } catch (IOException e){
            return "[ERROR] Cannot read table file";
        }
    }
}
