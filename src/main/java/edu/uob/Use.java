package edu.uob;
import java.io.*;


public class Use {

    public static String setUse(String command){
        String databaseName = command.substring(4).trim().replace(";","");

        //1.2 Check whether databases/mydatabases is exist?
        File dbFolder = new File("databases" + File.separator + databaseName);
        if (dbFolder.exists()) {
            //1.2 set current database = databaseName
            return databaseName;
        } else {
            return "[ERROR] Database not found";
        }
    }
}