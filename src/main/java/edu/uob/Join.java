package edu.uob;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

/*
JOIN table_name_1 AND table_name_2 ON attribute_name1 AND attribute_name2
 */
public class Join {
    public static String setJoin(String command, String currentDatabase) {

        // Find keyword;
        String[] word = command.trim().replace(";","").split("\\s+");
        // Check syntax has the right number value;
        if (word.length < 8) {
            return "[ERROR] Invalid Join syntax.";
        }
        // Check word 3 is AND, word 5 is ON, and word 7 is AND
        if (!(word[2].toLowerCase().equals("and") && word[4].toLowerCase().equals("on") && word[6].toLowerCase().equals("and"))) {
            return "[ERROR] Invalid Join syntax. Join must include 'and' and 'on'.";
        }

        // Grab table 1 and table 2, attribute1 and attribute2
        String tableName1 = word[1].toLowerCase().trim().concat(".tab");
        File tabFile1 = new File("databases" + File.separator + currentDatabase + File.separator + tableName1);
        String tableName2 = word[3].toLowerCase().trim().concat(".tab");
        File tabFile2 = new File("databases" + File.separator + currentDatabase + File.separator + tableName2);
        String attributeName1 = word[5].toLowerCase().trim();
        String attributeName2 = word[7].toLowerCase().trim().replace(";","");

        // Check whether table is existed.
        if (!tabFile1.exists()) {
            return "[ERROR] Table " + tableName1 + " does not exist.";
        }
        if (!tabFile2.exists()) {
            return "[ERROR] Table " + tableName2 + " does not exist.";
        }


        int columnIndex1 = -1;
        int columnIndex2 = -1;
        List<String[]> table1Rows = new ArrayList<>();
        List<String[]> table2Rows = new ArrayList<>();
        String[] attributeList1 = new String[0];
        String[] attributeList2 = new String[0];


        // Read table 1
        try (BufferedReader reader = new BufferedReader(new FileReader(tabFile1))) {
            String header1 = reader.readLine(); // Read header
            attributeList1 = header1.split("\t");

            for (int i = 0; i < attributeList1.length; i++) {
                if (attributeList1[i].trim().equals(attributeName1.trim().toLowerCase())) {
                    columnIndex1 = i;
                    break;
                }
            }
            if (columnIndex1 == -1) {
                return "[ERROR] Column " + attributeName1 + " in " + tableName1 + " not found.";
            }

            String line;
            while ((line = reader.readLine()) != null) {
                table1Rows.add(line.split("\t"));
            }
        } catch (IOException e){
            return "[ERROR] Unable to read join file " + tabFile1;
        }

        // Read table 2
        try (BufferedReader reader = new BufferedReader(new FileReader(tabFile2))) {
            String header2 = reader.readLine(); // Read header
            attributeList2 = header2.split("\t");

            for (int i = 0; i < attributeList2.length; i++) {
                if (attributeList2[i].trim().equals(attributeName2.trim().toLowerCase())) {
                    columnIndex2 = i;
                    break;
                }
            }
            if (columnIndex2 == -1) {
                return "[ERROR] Column " + attributeName2 + " in " + tableName2 + " not found.";
            }

            String line;
            while ((line = reader.readLine()) != null) {
                table2Rows.add(line.split("\t"));

            }
        } catch (IOException e){
            return "[ERROR] Unable to read join file " + tabFile2;
        }

        StringBuilder joinResult = new StringBuilder("[OK]\n");
        List<String[]> finalRows = new ArrayList<>();

        List<String> newHeader = new ArrayList<>();
        newHeader.add("id");

        for (int i = 1; i < attributeList1.length; i++) {
            if (i != columnIndex1) {
                newHeader.add(tableName1.replace(".tab", "") + "." + attributeList1[i]);
            }
        }
        for (int i = 0; i < attributeList2.length; i++) {
            if (i != columnIndex2 && !attributeList2[i].equals("id")) {
                newHeader.add(tableName2.replace(".tab", "") + "." + attributeList2[i]);
            }
        }
        joinResult.append(String.join("\t", newHeader)).append("\n");


        for (String[] row1 : table1Rows) {
            for (String[] row2 : table2Rows) {
                if (row1[columnIndex1].equals(row2[columnIndex2])){
                    List<String> mergedRow = new ArrayList<>();
                    for (int i = 1; i < row1.length; i++) {
                        if (i != columnIndex1){
                            mergedRow.add(row1[i]);
                        }
                    }
                    for (int i = 0; i < row2.length; i++) {
                        if (i != columnIndex2 && !attributeList2[i].equals("id")) {
                            mergedRow.add(row2[i]);
                        }
                    }
                    finalRows.add(mergedRow.toArray(new String[0]));
                    //joinResult.append(String.join("\t", mergedRow)).append("\n");
                }
            }
        }

        //Assign new id
        int newId = 1;
        for (String[] row : finalRows) {
            joinResult.append(newId).append("\t").append(String.join("\t", row)).append("\n");
            newId++;
        }

        return joinResult.toString();
    }
}
