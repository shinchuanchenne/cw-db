package edu.uob;
import java.io.*;

/*
INSERT INTO table_name VALUE ( value_list, value_list2, value_list3);
 */

public class Insert {


    public static String setInsert(String command, String currentDatabase) {
        // 1.6 Separate each command. (separate( with space.)
        if (command.contains("(")){
            command = command.replace("(", " ( ");
        }
        if (command.contains(",")){
            command = command.replace(",", " , ");
        }
        String[] word = command.trim().split("\\s+");

        // 1.6 Check whether syntax is correct
        if (!word[1].toLowerCase().equals("into") || !word[3].toLowerCase().equals("values")) {
            return "[ERROR] Invalid INSERT command";
        }
        // 1.6 Check whether tableName is existed in currentDatabase.
        String tableName = word[2].trim().concat(".tab");
        File tabFile = new File("databases" + File.separator + currentDatabase + File.separator + tableName);
        if (!tabFile.exists()) {
            return "[ERROR] Table not found";
        }


        // 1.6 Find ( and ) after value
        int startIndex = command.indexOf("(");
        int endIndex = command.indexOf(")");

        if (startIndex == -1 || endIndex == -1 || startIndex > endIndex) {
            return "[ERROR] Invalid INSERT syntax (missing parentheses)";
        }

        // 1.6 Catch values
        String values = command.substring(startIndex + 1, endIndex).trim();
        String[] valueList = values.split("\\s*, \\s*"); // Make sure ' will delete



        for (String value : valueList) {
            if(!ErrorHandling.valueCheck(value).equals("[OK]")) {
                return "[ERROR] Invalid value: " + value;
            }
        }

        for (int i = 0; i < valueList.length; i++) {
            if (valueList[i].matches("'[^']*'")) {  // Make sure that is StringLiteral
                valueList[i] = valueList[i].substring(1, valueList[i].length() - 1);
            }
        }

        // 1.6 Make sure numbers of valueList is equal to first row of file.
        int numberOfValues = valueList.length; //Record numbers of given values

        // 1.6 READ the file first line
        try (BufferedReader reader = new BufferedReader(new FileReader(tabFile))){
            String header = reader.readLine();
            String[] attributeList = header.split("\t");
            int numberOfAttributes = attributeList.length;
            // 1.61 [EXIT] Return count mismatch
            if (numberOfAttributes - 1 != numberOfValues) { // REMEMBER - id
                return "[ERROR] Column count mismatch";
            }
            // 1.6 Get the last row
            String lastLine = "";
            String line;
            while ((line = reader.readLine()) != null) {
                lastLine = line;
            }
            int newId = 1; // Give ID
            if (!lastLine.isEmpty()) {
                String[] lastRow = lastLine.split("\t");
                newId = Integer.parseInt(lastRow[0]) + 1;
            }

            String newRow = newId + "\t" + String.join("\t", valueList) + "\n";
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(tabFile, true))) {
                writer.write(newRow);
                writer.flush();
                return "[OK]";
            } catch (IOException ioe) {
                return "[ERROR] Failed to write table";
            }
        } catch (IOException ioe) {
            return "[ERROR] Failed to read table";
        }
    }
}