package edu.uob;
import java.io.File;
import java.io.IOException;
import java.io.FileWriter;

/*
CREATE DATABASE database_name;
CREATE TABLE table_name;
CREATE TABLE table_name ( attribute1, attribute2, attribute3);
 */

public class Create {

    public static String setCreate(String command, String currentDatabase) {
        //1.3 Standarise words after CREATE
        String[] word = command.toLowerCase().trim().split("\\s+");

        //1.3 Check no parameter.
        if (word.length < 2) {
            return "[ERROR] Invalid CREATE command";
        }

        String secondWord = word[1];
        // 1.3 To see the second word is Database or Table.
        switch (secondWord) {
            case "database":
                return createDatabase(command);
            case "table":
                return createTable(command, currentDatabase);
            default:
                return "[ERROR] Invalid CREATE command";
        }
    }

    //1.4 Writing Create Database
    private static String createDatabase(String command) {
        //1.4
        String[] word = command.toLowerCase().trim().split("\\s+");
        String databaseName = word[2].trim().replace(";","");

        // Check database name is legal.
        String checkdbName = ErrorHandling.plainTextCheck(databaseName, "DatabaseName");
        if (!checkdbName.equals("[OK]")) {
            return checkdbName;
        }

        File dbFolder = new File("databases" + File.separator + databaseName);
        if (!dbFolder.exists()) {
            //1.4 create a database folder
            dbFolder.mkdirs();
            return "[OK]";
        } else {
            //1.4 return database has exists
            return "[ERROR] Database already exists";
        }
    }

    //1.5 Writing Create Table
    private static String createTable(String command, String currentDatabase) {
        //1.5 Check current Database is set or not.
        if (currentDatabase == null) {
            return "[ERROR] You must define a database first";
        }
        //1.5 Separate (Make sure that Attribute will not lowercase)

        //1.5b <FIX> if there's ( in the command, make sure add space so that tableName( will not convent into a space.
        if (command.contains("(")){
            command = command.replace("(", " (");
        }
        String[] word = command.trim().split("\\s+");

        //1.5a get table name
        if (word.length == 3) {

            // 1.5a type: create table student;
            String tableName = word[2].trim().replace(";","");

            // Check table name is legal.
            String checktbName = ErrorHandling.plainTextCheck(tableName, "TableName");
            if (!checktbName.equals("[OK]")) {
                return checktbName;
            }
            tableName = tableName.concat(".tab");

            File tabFile = new File("databases" + File.separator + currentDatabase + File.separator + tableName);

            // 1.5a If .tab file (table) is not exist, create a .tab
            if (!tabFile.exists()) {
                try {
                    tabFile.createNewFile();
                } catch (IOException ioe) {
                    return "[ERROR] Failed to create table";
                }
                //1.5a create id in the first row

                try(FileWriter writer = new FileWriter(tabFile)){
                    writer.write("id\n");
                    writer.flush();
                } catch (IOException ioe) {
                    return "[ERROR] Failed to write table id";
                }
                return "[OK]";
            } else {
                return "[ERROR] Table already exists";
            }

        } else if (word.length > 3) {
            // 1.5b type: create table student(name, age, email);
            // 1.5b Identify table name and attribute name
            String tableName = word[2].trim().concat(".tab");
            // 1.5b Find location of ( and )
            int startIndex = command.indexOf("(");
            int endIndex = command.indexOf(")");
            // 1.5b Catch attributes
            String attributes = command.substring(startIndex + 1, endIndex).trim();
            String[] attributeList = attributes.split(",");
            // 1.5 [BUGFIX] make sure that attribute won't create space.
            for (int i = 0; i < attributeList.length; i++) {
                attributeList[i] = attributeList[i].trim();
                // Check attribute name is validated.
                String checkAttrName = ErrorHandling.plainTextCheck(attributeList[i], "AttributeName");
                if (!checkAttrName.equals("[OK]")) {
                    return checkAttrName;
                }

                if (attributeList[i].equalsIgnoreCase("id")) {
                    return "[ERROR] You can not create an id attribute";
                }

                // Check has same attribute.
                for (int j = 0; j < i; j++){
                    if (attributeList[j].toLowerCase().equals(attributeList[i].toLowerCase())) {
                        return "[ERROR] Duplicate attribute";
                    }
                }

            }

            //1.5b Check whether table is existed?
            File tabFile = new File("databases" + File.separator + currentDatabase + File.separator + tableName);
            // 1.5b If .tab file (table) is not exist, create a .tab
            if (!tabFile.exists()) {
                try {
                    tabFile.createNewFile();
                } catch (IOException ioe) {
                    return "[ERROR] Failed to create table";
                }
                //1.5b create id in the first row, then create attributes
                try(FileWriter writer = new FileWriter(tabFile)){
                    String header = "id\t" + String.join("\t", attributeList) + "\n";
                    writer.write(header);
                    writer.flush();
                } catch (IOException ioe) {
                    return "[ERROR] Failed to write table id";
                }
                return "[OK]";
            } else {
                return "[ERROR] Table already exists";
            }

        } else {
            return "[ERROR] Invalid CREATE command";
        }
    }

}