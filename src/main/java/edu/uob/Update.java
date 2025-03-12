package edu.uob;

import java.io.*;
import java.util.*;

public class Update {
    public static String setUpdate (String command, String currentDatabase) {

        // UPDATE school SET mark = 38 WHERE name == 'Chris';
        //   0       1    2    3  4  5   6    7   8    9

        // UPDATE school SET name = Luis, age = 38 WHERE name == 'Chris';

        // 1.8 Find keyword
        // Find set
        String[] word = command.trim().replace(";","").split("\\s+");
        int setIndex = findSetIndex(word);
        if (setIndex != 2) {
            return "[ERROR] Invalid Update command (no set)";
        }
        // Can not set ID
        if (word[3].toLowerCase().equals("id")){
            return "[ERROR] You can not modify ID";
        }

        // Find where
        int whereIndex = Select.findWhereIndex(word);
        if (whereIndex == -1) {
            return "[ERROR] Invalid Update command (Missing where clause)";
        }

        // Find table name
        String tableName = word[1].trim().toLowerCase().concat(".tab");
        // Check whether table name exist.
        File tabFile = new File("databases" + File.separator + currentDatabase + File.separator + tableName);
        if (!tabFile.exists()) {
            return "[ERROR] Table not found";
        }

        // Find setName
        String setClasue = String.join("", Arrays.copyOfRange(word, setIndex + 1, whereIndex));
        String[] assignments = setClasue.split(",");
        // Storage column names and values to be set.
        List<String> setColumnNames = new ArrayList<>();
        List<String> setValues = new ArrayList<>();

        for (String assignment : assignments) {
            String[] columnValuePair = assignment.split("=");
            if (columnValuePair.length != 2) {
                return "[ERROR] Invalid Update command (invalid set syntax)";
            }
            setColumnNames.add(columnValuePair[0].trim());
            setValues.add(columnValuePair[1].replace("\'", "").trim());

        }
        System.out.println("<DEBUG> Parsed column name :" + setColumnNames);
        System.out.println("<DEBUG> Parsed value :" + setValues);


        // Find where condition: after
        String whereColumnName = word[whereIndex + 1].trim();
        System.out.println("<DEBUG> Where name: " + whereColumnName);
        String whereOperator = word[whereIndex + 2].trim();
        System.out.println("<DEBUG> Where operator: " + whereOperator);
        String whereValue = word[whereIndex + 3].trim().replace(";", "");
        System.out.println("<DEBUG> Where value: " + whereValue);


        // Open the Table file:

        // Make sure that setColumnNames is in the header.
        try (BufferedReader reader = new BufferedReader(new FileReader(tabFile))) {
            String header = reader.readLine(); // Read header
            String[] attributeList = header.split("\t");

            // Storage column's header location
            List<Integer> setColumnIndexes = new ArrayList<>();
            for (String column : setColumnNames) {
                int columnIndex = -1;
                for (int i = 0; i < attributeList.length; i++) {
                    if (attributeList[i].trim().equals(column.trim())) {
                        columnIndex = i;
                        break;
                    }
                }
                if (columnIndex == -1) {
                    return "[ERROR] Column: " + column + " not found";
                }
                setColumnIndexes.add(columnIndex);
            }

            System.out.println("<DEBUG> Parsed column indexes :" + setColumnIndexes);

            // 6 Make sure where clause 'COLUMN' is existed
            int whereColumnIndex = -1;
            for (int i = 0; i < attributeList.length; i++) {
                if (attributeList[i].trim().equalsIgnoreCase(whereColumnName.trim())) {
                    whereColumnIndex = i;
                    break;
                }
            }

            if (whereColumnIndex == -1) {
                return "[ERROR] Column: " + whereColumnName + " not found";
            }
            System.out.println("<DEBUG> WHERE ColumnIndex :" + whereColumnIndex);


            StringBuilder newContent = new StringBuilder();
            newContent.append(header).append("\n");  // Write header

            String line;
            boolean updated = false;


            // Change conditional columnValue
            while ((line = reader.readLine()) != null) {
                String[] rowValues = line.split("\t");

                // If column is less than header, filled with NULL
                // 1	Simon	65	TRUE	35
                if (rowValues.length < attributeList.length) {
                    rowValues = Arrays.copyOf(rowValues, attributeList.length);
                    Arrays.fill(rowValues, rowValues.length - (attributeList.length - rowValues.length), rowValues.length, "NULL");
                }


                if (LogicController.compareValues(rowValues[whereColumnIndex], whereValue, whereOperator)){
                    for (int i = 0; i < setColumnIndexes.size(); i++) {
                        int columnIndex = setColumnIndexes.get(i);

                        // 避免 ArrayIndexOutOfBoundsException
                        if (columnIndex >= rowValues.length) {
                            return "[ERROR] Invalid table format or missing values in some rows";
                        }

                        rowValues[columnIndex] = setValues.get(i);
                    }
                    updated = true;
                    System.out.println("<DEBUG> Parsed row :" + String.join("\t", rowValues));
                } else {
                    System.out.println("<DEBUG> Unchanged row :" + line);
                }
                newContent.append(String.join("\t", rowValues)).append("\n");
            }
            if (!updated) {
                return "[ERROR] no matching value for update";
            }

            try (BufferedWriter writer = new BufferedWriter(new FileWriter(tabFile))) {
                writer.write(newContent.toString());
                return "[OK]";
            } catch (IOException e){
                return "[ERROR] Failed to write table";
            }

        } catch (IOException e){
            return "[ERROR] IO Error";
        }

    }

    // Find keyword 'where'
    public static int findSetIndex(String[] word){
        for (int i = 0; i < word.length; i++) {
            if (word[i].toLowerCase().equals("set")) {
                System.out.println("<DEBUG> Set is in :" + i);
                return i;
            }
        }
        return -1;
    }

}
