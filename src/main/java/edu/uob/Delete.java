package edu.uob;

import java.io.*;
import java.util.Arrays;

/*
DELETE FROM table_name WHERE cond_attribute cond_operation cond_value;
 */

public class Delete {

    public static String setDelete(String command, String currentDatabase){

        //1.8 Check current Database is set or not.
        if (currentDatabase == null) {
            return "[ERROR] You must define a database first";
        }

        command = command.replace(">=", " >= ")
                .replace("<=", " <= ")
                .replace("!=", " != ")
                .replace(">", " > ")
                .replace("<", " < ")
                .replace("==", " == ")
                .replace("LIKE", " LIKE ");

        // 1.8 Find keyword
        String[] word = command.trim().replace(";","").split("\\s+");
        // 1.8 Make sure syntax "DELETE" "FROM" "WHERE"
        if (!word[1].toLowerCase().equals("from") || !word[3].toLowerCase().equals("where")) {
            return "[ERROR] Invalid delete command";
        }

        // 1.8 Find table name
        String tableName = word[2].trim().toLowerCase().concat(".tab");
        // 1.8 Find Attribute,  name
        String attributeName = word[4].trim().toLowerCase();
        // 1.8 Find comparison
        String comparisonOperator = word[5];
        // 1.8 Find Value
        String valueName = word[6].replace(";", "").trim();

        // 1.8 Check whether table name exist.
        File tabFile = new File("databases" + File.separator + currentDatabase + File.separator + tableName);
        if (!tabFile.exists()) {
            return "[ERROR] Table not found";
        }

        // Check comparison is valid.
        if (!(comparisonOperator.equals("==") || comparisonOperator.equals("<") || comparisonOperator.equals(">")
                || comparisonOperator.equals("<=") || comparisonOperator.equals(">=") || comparisonOperator.equals("!=")
                || comparisonOperator.toLowerCase().equals("like"))) {
            return "[ERROR] Comparison operator " + comparisonOperator + " not supported";
        }

        // Check whether attribute is in the table.
        try (BufferedReader reader = new BufferedReader(new FileReader(tabFile))) {
            String header = reader.readLine();
            String[] attributeListFromTable = header.split("\t");
            int columnIndex = -1;
            for (int i = 0; i < attributeListFromTable.length; i++) {
                if (attributeListFromTable[i].equals(attributeName)) {
                    columnIndex = i;
                    break;
                }
            }
            if (columnIndex == -1) {
                return "[ERROR] Column " + attributeName + " not found";
            }

            //
            StringBuilder updatedContent = new StringBuilder();
            boolean deleted = false;
            updatedContent.append(header).append("\n");
            String line;
            while ((line = reader.readLine()) != null) {

                if(columnIndex >= word.length) {
                    continue;
                }

                String[] words = line.split("\t");
                String columnValue = words[columnIndex];
                if (LogicController.compareValues(columnValue, valueName, comparisonOperator)) {
                    deleted = true;
                    continue;
                }
                updatedContent.append(line).append("\n");
            }

            // update .tab file

            if (!deleted) {
                return "[ERROR] No rows matched the condition";
            }
            try(BufferedWriter writer = new BufferedWriter(new FileWriter(tabFile))) {
                writer.write(updatedContent.toString());
                return "[OK]";
            }

        } catch (IOException e){
            return "[ERROR] Unable to open file";
        }
    }
}
