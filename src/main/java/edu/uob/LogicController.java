package edu.uob;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class LogicController {

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

            if (LogicController.compareValues(columnValue, valueName, comparisonOperator)){
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
                conditionResults[i] = LogicController.compareValues(rowValues[columnIndex], value, operator);
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


    public static String formatOperatorsAddSpace(String command) {
        command = command.replace(">=", " >= ")
                .replace("<=", " <= ")
                .replace("!=", " != ")
                .replace(">", " > ")
                .replace("<", " < ")
                .replace("==", " == ")
                .replace("(", " ( ")
                .replace(",", " , ");

        if (command.toUpperCase().contains(" WHERE ")) {
            int whereIndex = command.toUpperCase().indexOf(" WHERE ") + 7;
            String beforeWhere = command.substring(0, whereIndex);
            String afterWhere = command.substring(whereIndex);

            afterWhere = afterWhere.replace("LIKE", " LIKE ")
                    .replace("like", " LIKE ")
                    .replace("AND", " AND ")
                    .replace("and", " AND ")
                    .replace("OR", " OR ")
                    .replace("or", " OR ");
            command = beforeWhere + afterWhere;
        }
        return command;
    }

}
