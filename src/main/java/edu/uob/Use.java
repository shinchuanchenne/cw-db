package edu.uob;
import java.io.*;

// USE database
public class Use {

    public static String setUse(String command){
        String[] word = command.trim().replace(";","").split("\\s+");

        if (word.length != 2) {
            return "[ERROR] wrong use syntax";
        }
        String databaseName = word[1];
        //1.2 Check whether databases/mydatabases is existed?
        File dbFolder = new File("databases" + File.separator + databaseName);
        if (dbFolder.exists()) {
            //1.2 set current database = databaseName
            return databaseName;
        } else {
            return "[ERROR] Database not found";
        }
    }
}