package edu.uob;
import java.io.*;
import java.nio.Buffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/*
SELECT * FROM table_name;
SELECT * FROM table_name WHERE cond_attribute cond_operation cond_value;
SELECT attribute, attribute2 FROM table_name;
SELECT attribute, attribute2 FROM table_name WHERE cond_attribute cond_operation cond_value;
 */


public class Select {

    public static String setSelect(String command, String currentDatabase){
        command = LogicController.formatOperatorsAddSpace(command);


        if (command.contains("(")){
            command = command.replaceAll("\\(", "");
        }
        if (command.contains(")")){
            command = command.replaceAll("\\)", "");
        }

        // 1.7 Find keyword from and where
        String[] word = command.trim().replace(";","").split("\\s+");

        for (int i = 0; i < word.length; i++) {
            word[i] = word[i].replace(",", "");
        }


        int fromIndex = findFromIndex(word);
        int whereIndex = findWhereIndex(word);


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
    public static int findWhereIndex(String[] word){
        for (int i = 0; i < word.length; i++) {
            if (word[i].toLowerCase().equals("where")) {
                return i;
            }
        }
        return -1;
    }


    private static String case1(File tabFile) {
        StringBuilder result = new StringBuilder("[OK]\n");
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
        try (BufferedReader reader = new BufferedReader(new FileReader(tabFile))){
            String result = conditionalController(reader, word, fromIndex);
            if (result.startsWith("[ERROR]")){
                return result;
            }
            return "[OK]\n" + result;
        } catch (IOException e){
            return "[ERROR] Error reading table";
        }
    }
    private static String case3(File tabFile, String[] word, int fromIndex) {
        try (BufferedReader reader = new BufferedReader(new FileReader(tabFile))){
            String result = attributeController(reader, word, fromIndex);
            if (result.startsWith("[ERROR]")){
                return result;
            }
            return "[OK]\n" + result;
        } catch (IOException e){
            return "[ERROR] Error reading table";
        }
    }
    private static String case4(File tabFile, String[] word, int fromIndex, int whereIndex) {
        try (BufferedReader reader = new BufferedReader(new FileReader(tabFile))){

            String selectRow = conditionalController(reader, word, whereIndex);
            if (selectRow.startsWith("[ERROR]")){
                return selectRow;
            }
            BufferedReader fileReader = new BufferedReader(new StringReader(selectRow));
            String result = attributeController(fileReader, word, fromIndex);

            if (result.startsWith("[ERROR]")){
                return result;
            }
            return "[OK]\n" + result;
        } catch (IOException e){
            return "[ERROR] Error reading table";
        }
    }

    private static String attributeController(BufferedReader reader, String[] word, int fromIndex) throws IOException {
        StringBuilder result = new StringBuilder();
        // 1.7 for loop to delete ,
        for (int i = 0; i < word.length; i++) {
            if (word[i].contains(",")){
                word[i] = word[i].replace(",","");
            }
        }
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

    public static String conditionalController(BufferedReader reader, String[] word, int whereIndex) throws IOException {
        for (int i = whereIndex + 1; i < word.length; i++) {
            if (word[i].equalsIgnoreCase("and") || word[i].equalsIgnoreCase("or")) {
                return LogicController.multipleConditionalController(reader, word, whereIndex);
            }
        }
        return LogicController.singleConditionalController(reader, word, whereIndex);
    }

}
