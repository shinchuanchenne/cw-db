package edu.uob;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Paths;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/** This class implements the DB server. */
public class DBServer {

    private static final char END_OF_TRANSMISSION = 4;
    private String storageFolderPath;

    public static void main(String args[]) throws IOException {
        DBServer server = new DBServer();
        server.blockingListenOn(8888);
    }

    /**
    * KEEP this signature otherwise we won't be able to mark your submission correctly.
    */
    public DBServer() {
        storageFolderPath = Paths.get("databases").toAbsolutePath().toString();
        try {
            // Create the database storage folder if it doesn't already exist !
            Files.createDirectories(Paths.get(storageFolderPath));
        } catch(IOException ioe) {
            System.out.println("Can't seem to create database storage folder " + storageFolderPath);
        }
    }

    /**
    * KEEP this signature (i.e. {@code edu.uob.DBServer.handleCommand(String)}) otherwise we won't be
    * able to mark your submission correctly.
    *
    * <p>This method handles all incoming DB commands and carries out the required actions.
    */


    public String handleCommand(String command) {
        // TODO implement your server logic here
        //1.1 Analysing Comment
        //1.1 Standarise lowercase and delete space
        command = command.trim();

        //1.4 Check ends with ;
        if (!command.endsWith(";")) {
            return "[ERROR] Invalid command syntax.";
        }
        //1.3 Generate a switch to set logic
        String[] words = command.split(" ");
        String keyword = words[0].toLowerCase();
        switch (keyword) {
            case "use":
                return useDatabase(command);
            case "create":
                return create(command);
            case "insert":
                // 1.6 INSERT INTO
                return insert(command);
                // 1.7 SELECT
            case "select":
                return select(command);
        }
        return "[ERROR] Unknown command: " + command;
    }

    // 1.2 Writing USE
    // 1.2 Record current database name
    private String currentDatabase;
    private String useDatabase(String command){
        String databaseName = command.substring(4).trim().replace(";","");
        //1.2 Check whether databases/mydatabases is exist?
        File dbFolder = new File("databases" + File.separator + databaseName);
        if (dbFolder.exists()) {
            //1.2 set current database = databaseName
            currentDatabase = databaseName;
            return "[OK]";
        } else {
            return "[ERROR] Database not found";
        }
    }

    //1.3 Writing CREATE
    private String create(String command) {
        //1.3 Standarise words after CREATE
        String[] word = command.toLowerCase().trim().split(" ");

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
                return createTable(command);
            default:
                return "[ERROR] Invalid CREATE command";
        }
    }
    //1.4 Writing Create Database
    private String createDatabase(String command) {
        //1.4
        String[] word = command.toLowerCase().trim().split(" ");
        String databaseName = word[2].trim().replace(";","");

        File dbFolder = new File("databases" + File.separator + databaseName);
        if (!dbFolder.exists()) {
            //1.4 create a database folder
            dbFolder.mkdirs();
            currentDatabase = databaseName; // set to current database
            return "[OK]";
        } else {
            //1.4 return database has exists
            return "[ERROR] Database already exists";
        }
    }
    //1.5 Writing Create Table
    private String createTable(String command) {
        //1.5 Check current Database is set or not.
        if (currentDatabase == null) {
            return "[ERROR] You must define a database first";
        }
        //1.5 Separate (Make sure that Attribute will not lowercase)

        //1.5b <FIX> if there's ( in the command, make sure add space so that tableName( will not convent into a space.
        if (command.contains("(")){
            command = command.replace("(", " (");
        }
        String[] word = command.trim().split(" ");

        //1.5a get table name
        if (word.length == 3) {
            // 1.5a type: create table student;
            String tableName = word[2].trim().replace(";","").concat(".tab");
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

    // 1.6 INSERT INTO
    private String insert(String command) {
        //1.6 Check current Database is set or not.
        if (currentDatabase == null) {
            return "[ERROR] You must define a database first";
        }
        // 1.6 Separate each command. (separate( with space.)
        if (command.contains("(")){
            command = command.replace("(", " ( ");
        }
        if (command.contains(",")){
            command = command.replace(",", " , ");
        }
        String[] word = command.trim().split(" ");
        // 1.6 Check whether syntax is correct
        if (!word[1].toLowerCase().equals("into") || !word[3].toLowerCase().equals("values")) {
            return "[ERROR] Invalid INSERT command";
        }
        // 1.6 Check whether tableName is exist in currentDatabase.
        String tableName = word[2].trim().concat(".tab");
        File tabFile = new File("databases" + File.separator + currentDatabase + File.separator + tableName);
        if (!tabFile.exists()) {
            return "[ERROR] Table not found";
        }
        // 1.6 Find ( and ) after value
        int startIndex = command.indexOf("(");
        int endIndex = command.indexOf(")");
        // 1.6 Catch values
        String values = command.substring(startIndex + 1, endIndex).trim();
        String[] valueList = values.replace("\'", "").split(","); // Make sure ' will delete
        // 1.6 delete other space
        for (int i = 0; i < valueList.length; i++) {
            valueList[i] = valueList[i].trim();
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

    //1.7 SELECT
    private String select(String command) {
        //1.7 Check current Database is set or not.
        if (currentDatabase == null) {
            return "[ERROR] You must define a database first";
        }
        String[] word = command.trim().split(" ");
        System.out.println(Arrays.toString((word)));

        // 1.7 Find table_name ( after keyword FROM)
        int fromIndex = -1;
        int whereIndex = -1;
        for (int i = 0; i < word.length; i++) {
            if (word[i].toLowerCase().equals("from")) {
                fromIndex = i;
                break;
            }
        }
        // 1.7 if has where, locate where
        for (int i = 0; i < word.length; i++) {
            if (word[i].toLowerCase().equals("where")) {
                whereIndex = i;
                break;
            }
        }
        // 1.7

        if (fromIndex == -1 || word[fromIndex + 1] == null) {
            return "[ERROR] Invalid SELECT command";
        }
        String tableName = word[fromIndex + 1].trim().toLowerCase().replace(";","").concat(".tab");
        // 1.7 Check whether table name exist.
        File tabFile = new File("databases" + File.separator + currentDatabase + File.separator + tableName);
        if (!tabFile.exists()) {
            return "[ERROR] Table not found";
        }

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

                    // 1.7.3 Print selected attribute
                    while ((line = reader.readLine()) != null) { //Read every row
                        String[] rowValues = line.split("\t");
                        List<String> selectedValues = new ArrayList<>();
                        for (int index : columnIndexes) {
                            selectedValues.add(rowValues[index]);
                        }
                        result.append(String.join("\t", selectedValues)).append("\n");
                        System.out.println(result);
                    }

                    System.out.println("<DEBUG> " + columnIndexes);

                }
            }
            return result.toString();
        } catch (IOException ioe){
            return "[ERROR] Failed to read table";
        }
    }


    //  === Methods below handle networking aspects of the project - you will not need to change these ! ===

    public void blockingListenOn(int portNumber) throws IOException {
        try (ServerSocket s = new ServerSocket(portNumber)) {
            System.out.println("Server listening on port " + portNumber);
            while (!Thread.interrupted()) {
                try {
                    blockingHandleConnection(s);
                } catch (IOException e) {
                    System.err.println("Server encountered a non-fatal IO error:");
                    e.printStackTrace();
                    System.err.println("Continuing...");
                }
            }
        }
    }

    private void blockingHandleConnection(ServerSocket serverSocket) throws IOException {
        try (Socket s = serverSocket.accept();
        BufferedReader reader = new BufferedReader(new InputStreamReader(s.getInputStream()));
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(s.getOutputStream()))) {

            System.out.println("Connection established: " + serverSocket.getInetAddress());
            while (!Thread.interrupted()) {
                String incomingCommand = reader.readLine();
                System.out.println("Received message: " + incomingCommand);
                String result = handleCommand(incomingCommand);
                writer.write(result);
                writer.write("\n" + END_OF_TRANSMISSION + "\n");
                writer.flush();
            }
        }
    }
}
