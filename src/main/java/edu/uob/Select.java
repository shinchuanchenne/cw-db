package edu.uob;
import java.io.*;
import java.nio.Buffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Select {

    public static String setSelect(String command, String currentDatabase){
        //1.7 Check current Database is set or not.
        if (currentDatabase == null) {
            return "[ERROR] You must define a database first";
        }

        // 1.7 Find keyword from and where
        System.out.println("<DEBUG> " + command);
        String[] word = command.trim().replace(";","").split(" ");
        System.out.println("<DEBUG> " + Arrays.toString((word)));

        int fromIndex = findFromIndex(word);
        int whereIndex = findIndex(word);


        // Syntax error if from is -1
        if (fromIndex == -1 || word[fromIndex + 1] == null) {
            return "[ERROR] Invalid SELECT command";
        }

        // Find table name
        String tableName = word[fromIndex + 1].trim().toLowerCase().concat(".tab");
        // 1.7 Check whether table name exist.
        File tabFile = new File("databases" + File.separator + currentDatabase + File.separator + tableName);
        if (!tabFile.exists()) {
            return "[ERROR] Table not found";
        }

        // Select case
        if (word[1].equals("*")){
            if (whereIndex == -1){
                return case1(tabFile);
            } else {
                return case2(tabFile, word, whereIndex);
            }
        } else {
            if (whereIndex == -1){
                return case3(tabFile, word, fromIndex);
            } else {
                return case4(tabFile, word, fromIndex, whereIndex);
            }
        }
    }

    // FIND keyword 'from'
    private static int findFromIndex(String[] word){
        for (int i = 0; i < word.length; i++) {
            if (word[i].toLowerCase().equals("from")) {
                return i;
            }
        }
        return -1;
    }
    // Find keyword 'where'
    private static int findIndex(String[] word){
        for (int i = 0; i < word.length; i++) {
            if (word[i].toLowerCase().equals("where")) {
                return i;
            }
        }
        return -1;
    }


    private static String case1(File tabFile) {
        StringBuilder result = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new FileReader(tabFile))){
            // 1.7.1 if SELECT * FROM table_name
            String line;
            while ((line = reader.readLine()) != null) {
                result.append(line).append("\n");
            }
        } catch (IOException e) {
            return "[ERROR] Error reading table";
        }
        return result.toString();
    }

    private static String case2(File tabFile, String[] word, int fromIndex) {
        StringBuilder result = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new FileReader(tabFile))){
            return "";
        } catch (IOException e){
            return "[ERROR] Error reading table";
        }
    }
    private static String case3(File tabFile, String[] word, int fromIndex) {
        try (BufferedReader reader = new BufferedReader(new FileReader(tabFile))){
            return attributeController(reader, word, fromIndex);
        } catch (IOException e){
            return "[ERROR] Error reading table";
        }
    }
    private static String case4(File tabFile, String[] word, int fromIndex, int whereIndex) {
        return "";
    }

    private static String attributeController(BufferedReader reader, String[] word, int fromIndex) throws IOException {
        StringBuilder result = new StringBuilder();
        // 1.7 for loop to delete ,
        for (int i = 0; i < word.length; i++) {
            if (word[i].contains(",")){
                word[i] = word[i].replace(",","");
            }
        }
        //System.out.println(Arrays.toString((word)));

        // 1.7 Grab attributeList
        List<String> attributeList = new ArrayList<>();
        for (int i = 1; i < fromIndex; i++) {
            attributeList.add(word[i]);
        }

        // 1.7.(34) Read header (Table)
        String header = reader.readLine();
        String[] attributeListFromTable = header.split("\t");

        List<Integer> columnIndexes = new ArrayList<>();// 1.7.3 Define an array to record num of row
        //1.7.(34) Check input attribute is in table
        for (String attribute : attributeList) {
            int index = Arrays.asList(attributeListFromTable).indexOf(attribute);
            if (index == -1) {
                return "[ERROR] Column " + attribute + " not found";
            }
            columnIndexes.add(index);
        }
        // 1.7.(34) Record attribute location
        List<String> selectedHeader = new ArrayList<>();
        for (int index : columnIndexes) {
            selectedHeader.add(attributeListFromTable[index]);
        }

        // 1.7.(34) Add header(From table)
        result.append(String.join("\t", selectedHeader)).append("\n");

        // 1.7.3 Print selected attribute (SELECT (ATTRIBUTE) WHERE (TABLE NAME))
        String line;
        while ((line = reader.readLine()) != null) {
            String[] rowValues = line.split("\t");
            List<String> selectedValues = new ArrayList<>();
            for (int index : columnIndexes) {
                selectedValues.add(rowValues[index]);
            }
            result.append(String.join("\t", selectedValues)).append("\n");
        }
        return result.toString();
    }


    private static String conditionalController(BufferedReader reader, String[] word, int whereIndex) throws IOException {

        // 1.7.2 FIND ATTRIBUTE
        String attributeName = word[whereIndex + 1];
        System.out.println("<DEBUG> " + attributeName);
        // 1.7.2 FIND Comparison
        String comparisonOperator = word[whereIndex + 2];
        System.out.println("<DEBUG> " + comparisonOperator);

        if (comparisonOperator.equals("==") || comparisonOperator.equals("<") || comparisonOperator.equals(">")
                || comparisonOperator.equals("<=") || comparisonOperator.equals(">=") || comparisonOperator.equals("!=")
                || comparisonOperator.toLowerCase().equals("like")) {
            // 1.7.3 FIND VALUES
            String valueName = word[whereIndex + 3].replace(";", "").trim();
            System.out.println("<DEBUG> " + valueName);

            // 1.7.3 Make sure that attribute is included
            String header = reader.readLine();
            String[] attributeListFromTable = header.split("\t");
            boolean isFound = false;
            int columnIndex = -1;
            for (int i = 0; i < attributeListFromTable.length; i++) {
                if (attributeListFromTable[i].equals(attributeName)) {
                    isFound = true;
                    columnIndex = i;
                    break;
                }
            }
            if (!isFound) {
                return "[ERROR] Column " + attributeName + " not found";
            }
        }
        return "";
    }

}

/*
    //1.7 SELECT
    private String select(String command) {

        // 1.7 Find table_name ( after keyword FROM)
        int fromIndex = -1;
        int whereIndex = -1;

        // 1.7 if has where, locate where
        for (int i = 0; i < word.length; i++) {
            if (word[i].toLowerCase().equals("where")) {
                whereIndex = i;
                break;
            }
        }
        // 1.7



        //1.7 Open the file
        try (BufferedReader reader = new BufferedReader(new FileReader(tabFile))){

            // 1.7 set a string builder
            StringBuilder result = new StringBuilder();
            String line;
            if (word[1].equals("*")) {
                if (whereIndex == -1) {
                    // 1.7.1 if SELECT * FROM table_name
                    while ((line = reader.readLine()) != null) {
                        result.append(line).append("\n");
                    }
                } else {
                    // 1.7.2 * and where ( SELECT * FROM table_name WHERE (Attribute) == (Value)

                    // 1.7.2 FIND ATTRIBUTE
                    String attributeName = word[whereIndex + 1];
                    System.out.println("<DEBUG> " + attributeName);
                    // 1.7.2 FIND Comparison
                    String comparisonOperator = word[whereIndex + 2];
                    System.out.println("<DEBUG> " + comparisonOperator);


                    if (comparisonOperator.equals("==") || comparisonOperator.equals("<") || comparisonOperator.equals(">")
                    || comparisonOperator.equals("<=") || comparisonOperator.equals(">=") || comparisonOperator.equals("!=")
                    || comparisonOperator.toLowerCase().equals("like")) {
                        // 1.7.3 FIND VALUES
                        String valueName = word[whereIndex + 3].replace(";", "").trim();
                        System.out.println("<DEBUG> " + valueName);

                        // 1.7.3 Make sure that attribute is included
                        String header = reader.readLine();
                        String[] attributeListFromTable = header.split("\t");
                        boolean isFound = false;
                        int columnIndex = -1;
                        for (int i = 0; i < attributeListFromTable.length; i++) {
                            if (attributeListFromTable[i].equals(attributeName)){
                                isFound = true;
                                columnIndex = i;
                                break;
                            }
                        }
                        if (!isFound) {
                            return "[ERROR] Column " + attributeName + " not found";
                        }





                    } else {
                        // comparison is not equal
                        return "[ERROR] Invalid SELECT command, Comparison operator mismatch";
                    }
                }
            } else {
                // 1.7 for loop to delete ,
                for (int i = 0; i < word.length; i++) {
                    if (word[i].contains(",")){
                        word[i] = word[i].replace(",", "");
                    }
                }
                //System.out.println(Arrays.toString((word)));

                // 1.7 Grab attributeList
                List<String> attributeList = new ArrayList<>();
                for (int i = 1; i < fromIndex; i++) {
                    attributeList.add(word[i]);
                }
                //System.out.println(attributeList);
                if (whereIndex == -1) {
                    // 1.7.(34) if SELECT (ATTRIBUTE) FROM table_name
                    String header = reader.readLine();
                    String[] attributeListFromTable = header.split("\t");

                    List<Integer> columnIndexes = new ArrayList<>(); // 1.7.3 Define an array to record num of row

                    //1.7.(34) Check input attribute is in table
                    for (String attr: attributeList){
                        boolean found = false;
                        for (String tableAttr : attributeListFromTable) {
                            if (tableAttr.equals(attr)) {
                                found = true;
                                break;
                            }
                        }

                        if (!found) {
                            return "[ERROR] Column " + attr + " not found in table";
                        }

                        // 1.7.(34) Record attribute location
                        for (int i = 0; i < attributeListFromTable.length; i++) {
                            if(attr.equals(attributeListFromTable[i])){
                                columnIndexes.add(i);
                                break;
                            }
                        }
                        System.out.println("<DEBUG> " + columnIndexes);
                    }

                    // 1.7.(34) Add header
                    List<String> selectedHeader = new ArrayList<>();
                    for (int index: columnIndexes) {
                        selectedHeader.add(attributeListFromTable[index]);
                    }
                    result.append(String.join("\t", selectedHeader)).append("\n");

                    // 1.7.3 Print selected attribute (SELECT (ATTRIBUTE) WHERE (TABLE NAME);
                    while ((line = reader.readLine()) != null) { //Read every row
                        String[] rowValues = line.split("\t");
                        List<String> selectedValues = new ArrayList<>();
                        for (int index : columnIndexes) {
                            selectedValues.add(rowValues[index]);
                        }
                        result.append(String.join("\t", selectedValues)).append("\n");
                        System.out.println("<DEBUG> " + result);
                    }

                    System.out.println("<DEBUG> " + columnIndexes);

                }
            }
            return result.toString();
        } catch (IOException ioe){
            return "[ERROR] Failed to read table";
        }
    }
 */