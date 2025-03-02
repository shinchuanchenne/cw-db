package edu.uob;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Paths;
import java.nio.file.Files;

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
