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
        //1.7 Check current Database is set or not.
        if (currentDatabase == null) {
            return "[ERROR] You must define a database first";
        }

        command = command.replace(">=", " >= ")
                .replace("<=", " <= ")
                .replace("!=", " != ")
                .replace(">", " > ")
                .replace("<", " < ")
                .replace("==", " == ")
                .replace("LIKE", " LIKE ")
                .replace("like", " LIKE ")
                .replace("AND", " AND ")
                .replace("and", " AND ")
                .replace("OR", " OR ")
                .replace("or", " OR ");
        ;


        if (command.contains("(")){
            command = command.replaceAll("\\(", "");
        }
        if (command.contains(")")){
            command = command.replaceAll("\\)", "");
        }

        // 1.7 Find keyword from and where
        System.out.println("<DEBUG> " + command);
        String[] word = command.trim().replace(";","").split("\\s+");
        System.out.println("<DEBUG> " + Arrays.toString((word)));

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
                System.out.println("<DEBUG> Where is in :" + i);
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

    public static String conditionalController(BufferedReader reader, String[] word, int whereIndex) throws IOException {
        for (int i = whereIndex + 1; i < word.length; i++) {
            if (word[i].equalsIgnoreCase("and") || word[i].equalsIgnoreCase("or")) {
                return multipleConditionalController(reader, word, whereIndex);
            }
        }
        return singleConditionalController(reader, word, whereIndex);
    }



    public static String singleConditionalController(BufferedReader reader, String[] word, int whereIndex) throws IOException {
        StringBuilder result = new StringBuilder();

        System.out.println("<DEBUG> WHERE Condition: " + Arrays.toString(Arrays.copyOfRange(word, whereIndex, word.length)));
        // Make sure attribute Name is after whereIndex
        if (whereIndex + 1 >= word.length) {
            return "[ERROR] Invalid WHERE syntax: Missing column name";
        }
        // 1.7.2 FIND ATTRIBUTE
        String attributeName = word[whereIndex + 1];
        System.out.println("<DEBUG> Attribute Name: " + attributeName);

        // Make sure comparisonOperator is after whereIndex`
        if (whereIndex + 2 >= word.length) {
            return "[ERROR] Invalid WHERE syntax: Missing comparison operator";
        }

        // 1.7.2 FIND Comparison
        String comparisonOperator = word[whereIndex + 2];
        System.out.println("<DEBUG> Comparison Operator: " + comparisonOperator);

        // Check comparison is valid.
        if (!(comparisonOperator.equals("==") || comparisonOperator.equals("<") || comparisonOperator.equals(">")
                || comparisonOperator.equals("<=") || comparisonOperator.equals(">=") || comparisonOperator.equals("!=")
                || comparisonOperator.toLowerCase().equals("like"))) {
            return "[ERROR] Comparison operator " + comparisonOperator + " not supported";
        }

        // Make sure valueName is after whereIndex
        if (whereIndex + 3 >= word.length) {
            return "[ERROR] Invalid WHERE syntax: Missing value";
        }

        // 1.7.3 FIND VALUES
        String valueName = word[whereIndex + 3].replace(";", "").trim();
        System.out.println("<DEBUG> Value Name: " + valueName);

        // 1.7.3 Make sure that attribute is included
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

        // 1.7.3 Adding Header to result
        boolean found = false;

        result.append(header).append("\n");

        String line;
        while ((line = reader.readLine()) != null) {
            String[] rowValues = line.split("\t");

            if (columnIndex >= rowValues.length) {
                continue;
            }
            String columnValue = rowValues[columnIndex];

            if (compareValues(columnValue, valueName, comparisonOperator)){
                result.append(line).append("\n");
                found = true;
            }
        }
        if (!found) {
            return "[ERROR] No matching value";
        }
        return result.toString();
    }

    public static String multipleConditionalController(BufferedReader reader, String[] word, int whereIndex) throws IOException {
        System.out.println("<DEBUG> MULTIPLE CONDITIONAL CONTROLLER");


        List<Integer> operatorIndexes = new ArrayList<>();
        List<String> operators = new ArrayList<>();
        List<String[]> conditions = new ArrayList<>();

        for (int i = whereIndex + 1; i < word.length; i++) {
            if (word[i].toLowerCase().equals("and") || word[i].toLowerCase().equals("or")) {
                operatorIndexes.add(i);
                operators.add(word[i]);
            }
        }

        System.out.println("<DEBUG> Found operators at indexes: " + operatorIndexes);
        System.out.println("<DEBUG> Operators list: " + operators);

        // 1.7.2 FIND ATTRIBUTE

        int start = whereIndex + 1;
        for (int index : operatorIndexes) {
            String[] condition = Arrays.copyOfRange(word, start, index);
            conditions.add(condition);
            start = index + 1;
        }
        conditions.add(Arrays.copyOfRange(word, start, word.length));
        System.out.println("<DEBUG> Conditions list: " + conditions.stream()
                .map(Arrays::toString)
                .toList());

        //Make sure that attribute is included
        String header = reader.readLine();
        String[] attributeListFromTable = header.split("\t");
        List<Integer> columnIndexes = new ArrayList<>();
        for (String[] condition : conditions) {
            String attributeName = condition[0];
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
            columnIndexes.add(columnIndex);
        }
        System.out.println("<DEBUG> Found conditions at indexes: " + columnIndexes);

        //
        StringBuilder result = new StringBuilder().append(header).append("\n");
        String line;
        while ((line = reader.readLine()) != null) {
            String[] rowValues = line.split("\t");
            boolean[] conditionResults = new boolean[columnIndexes.size()];

            for (int i = 0; i < columnIndexes.size(); i++) {
                String[] condition = conditions.get(i);
                String attributeName = condition[0];
                String operator = condition[1];
                String value = condition[2];

                int columnIndex = -1;
                for (int j = 0; j < attributeListFromTable.length; j++) {
                    if (attributeListFromTable[j].equals(attributeName)) {
                        columnIndex = j;
                        break;
                    }
                }
                if (columnIndex == -1) {
                    return "[ERROR] Column " + attributeName + " not found";
                }
                conditionResults[i] = compareValues(rowValues[columnIndex], value, operator);
            }


            boolean finalResult = conditionResults[0];
            for (int i = 0; i < operators.size(); i++) {
                String operator = operators.get(i);
                if (operator.equalsIgnoreCase("and")) {
                    finalResult = finalResult && conditionResults[i + 1];
                } else if (operator.equalsIgnoreCase("or")) {
                    finalResult = finalResult || conditionResults[i + 1];
                }
            }
            if (finalResult) {
                result.append(line).append("\n");
            }
        }
        return result.toString();
    }



    public static boolean compareValues(String columnValue, String value, String operator) {
        try {
            double columnNumber = Double.parseDouble(columnValue);
            double valueNumber = Double.parseDouble(value);

            switch(operator){
                case "==":
                    return columnNumber == valueNumber;
                case "!=":
                    return columnNumber != valueNumber;
                case ">":
                    return columnNumber > valueNumber;
                case "<":
                    return columnNumber < valueNumber;
                case ">=":
                    return columnNumber >= valueNumber;
                case "<=":
                    return columnNumber <= valueNumber;
            }
        } catch (NumberFormatException e) {
            value = value.replaceAll("^'+|'+$", "");
            columnValue = columnValue.trim();
            switch (operator.toLowerCase()){
                case "==":
                    return columnValue.trim().toLowerCase().equals(value.trim().toLowerCase());
                case "!=":
                    return !(columnValue.trim().toLowerCase().contains(value.trim().toLowerCase()));
                case "like":
                    return columnValue.toLowerCase().contains(value.toLowerCase());
            }
        }
        return false;
    }


}
