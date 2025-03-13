package edu.uob;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.fail;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.time.Duration;

public class CustomDBTests {

    private DBServer server;

    @BeforeEach
    public void setup() {
        server = new DBServer();
    }

    private String sendCommandToServer(String command) {
        return assertTimeoutPreemptively(Duration.ofMillis(1000), () -> {
            return server.handleCommand(command);
        }, "Server took too long to respond (probably stuck in an infinite loop)");
    }

    @Test
    public void testCreateAndInsert() {
        sendCommandToServer("CREATE DATABASE markbook;");
        sendCommandToServer("USE markbook;");
        sendCommandToServer("CREATE TABLE marks (name, mark, pass);");
        sendCommandToServer("INSERT INTO marks VALUES ('Simon', 65, TRUE);");
        sendCommandToServer("INSERT INTO marks VALUES ('Sion', 55, TRUE);");
        sendCommandToServer("INSERT INTO marks VALUES ('Rob', 35, FALSE);");
        sendCommandToServer("INSERT INTO marks VALUES ('Chris', 20, FALSE);");

        String response = sendCommandToServer("SELECT * FROM marks;");
        assertTrue(response.contains("[OK]"), "SELECT * should return [OK]");
        assertTrue(response.contains("Simon"), "Simon should be in the table");
        assertTrue(response.contains("Chris"), "Chris should be in the table");
        sendCommandToServer("DROP DATABASE markbook;");
        assertTrue(response.contains("[OK]"), "SELECT * should return [OK]");
    }

    @Test
    public void testWhereClause() {
        sendCommandToServer("CREATE DATABASE markbook;");
        sendCommandToServer("USE markbook;");
        sendCommandToServer("CREATE TABLE marks (name, mark, pass);");
        sendCommandToServer("INSERT INTO marks VALUES ('Simon', 65, TRUE);");
        sendCommandToServer("INSERT INTO marks VALUES ('Sion', 55, TRUE);");
        sendCommandToServer("INSERT INTO marks VALUES ('Rob', 35, FALSE);");
        sendCommandToServer("INSERT INTO marks VALUES ('Chris', 20, FALSE);");

        String response = sendCommandToServer("SELECT * FROM marks WHERE pass == FALSE;");
        assertTrue(response.contains("[OK]"), "SELECT WHERE should return [OK]");
        assertTrue(response.contains("Rob"), "Rob should be in the result");
        assertTrue(response.contains("Chris"), "Chris should be in the result");
        sendCommandToServer("DROP DATABASE markbook;");
        assertTrue(response.contains("[OK]"), "SELECT * should return [OK]");
    }

    @Test
    public void testJoin() {
        sendCommandToServer("CREATE DATABASE markbook;");
        sendCommandToServer("USE markbook;");
        sendCommandToServer("CREATE TABLE coursework (task, submission);");
        sendCommandToServer("INSERT INTO coursework VALUES ('OXO', 3);");
        sendCommandToServer("INSERT INTO coursework VALUES ('DB', 1);");
        sendCommandToServer("INSERT INTO coursework VALUES ('OXO', 4);");
        sendCommandToServer("INSERT INTO coursework VALUES ('STAG', 2);");

        sendCommandToServer("CREATE TABLE marks (name, mark, pass);");
        sendCommandToServer("INSERT INTO marks VALUES ('Simon', 65, TRUE);");
        sendCommandToServer("INSERT INTO marks VALUES ('Sion', 55, TRUE);");
        sendCommandToServer("INSERT INTO marks VALUES ('Rob', 35, FALSE);");
        sendCommandToServer("INSERT INTO marks VALUES ('Chris', 20, FALSE);");

        String response = sendCommandToServer("JOIN coursework AND marks ON submission AND id;");
        assertTrue(response.contains("[OK]"), "JOIN should return [OK]");
        assertTrue(response.contains("OXO"), "OXO should be in the result");
        assertTrue(response.contains("STAG"), "STAG should be in the result");
        sendCommandToServer("DROP DATABASE markbook;");
        assertTrue(response.contains("[OK]"), "SELECT * should return [OK]");
    }

    @Test
    public void testUpdateAndDelete() {
        sendCommandToServer("CREATE DATABASE markbook;");
        sendCommandToServer("USE markbook;");
        sendCommandToServer("CREATE TABLE marks (name, mark, pass);");
        sendCommandToServer("INSERT INTO marks VALUES ('Simon', 65, TRUE);");
        sendCommandToServer("INSERT INTO marks VALUES ('Sion', 55, TRUE);");
        sendCommandToServer("INSERT INTO marks VALUES ('Rob', 35, FALSE);");
        sendCommandToServer("INSERT INTO marks VALUES ('Chris', 20, FALSE);");

        sendCommandToServer("UPDATE marks SET mark = 38 WHERE name == 'Chris';");
        String response = sendCommandToServer("SELECT * FROM marks WHERE name == 'Chris';");
        assertTrue(response.contains("38"), "Chris's mark should be updated to 38");

        sendCommandToServer("DELETE FROM marks WHERE name == 'Sion';");
        response = sendCommandToServer("SELECT * FROM marks;");
        assertFalse(response.contains("Sion"), "Sion should be deleted from the table");
        sendCommandToServer("DROP DATABASE markbook;");
        assertTrue(response.contains("[OK]"), "SELECT * should return [OK]");
    }

    @Test
    public void testAlterTable() {
        sendCommandToServer("CREATE DATABASE markbook;");
        sendCommandToServer("USE markbook;");
        sendCommandToServer("CREATE TABLE marks (name, mark, pass);");
        sendCommandToServer("INSERT INTO marks VALUES ('Simon', 65, TRUE);");

        sendCommandToServer("ALTER TABLE marks ADD age;");
        String response = sendCommandToServer("SELECT * FROM marks;");
        assertTrue(response.contains("age"), "Age column should be added");

        sendCommandToServer("ALTER TABLE marks DROP pass;");
        response = sendCommandToServer("SELECT * FROM marks;");
        assertFalse(response.contains("pass"), "Pass column should be removed");
        sendCommandToServer("DROP DATABASE markbook;");
        assertTrue(response.contains("[OK]"), "SELECT * should return [OK]");
    }

    @Test
    public void testDropTableAndDatabase() {
        sendCommandToServer("CREATE DATABASE markbook;");
        sendCommandToServer("USE markbook;");
        sendCommandToServer("CREATE TABLE marks (name, mark, pass);");
        sendCommandToServer("INSERT INTO marks VALUES ('Simon', 65, TRUE);");

        sendCommandToServer("DROP TABLE marks;");
        String response = sendCommandToServer("SELECT * FROM marks;");
        assertTrue(response.contains("[ERROR]"), "SELECT * FROM marks should return an error after table is dropped");

        sendCommandToServer("DROP DATABASE markbook;");
        response = sendCommandToServer("USE markbook;");
        assertTrue(response.contains("[ERROR]"), "USE markbook should return an error after database is dropped");
    }

    @Test
    public void testInvalidCommands() {
        sendCommandToServer("CREATE DATABASE markbook;");
        sendCommandToServer("USE markbook;");
        sendCommandToServer("CREATE TABLE marks (name, mark, pass);");

        String response = sendCommandToServer("SELECT height FROM marks WHERE name == 'Chris';");
        assertTrue(response.contains("[ERROR]"), "Querying non-existent column should return an error");

        response = sendCommandToServer("SELECT * FROM library;");
        assertTrue(response.contains("[ERROR]"), "Querying non-existent table should return an error");

        response = sendCommandToServer("SELECT * FROM marks");
        assertTrue(response.contains("[ERROR]"), "Missing semicolon should return an error");
    }


    @Test
    public void testWithSpace() {
        sendCommandToServer("CREATE       DATABASE          testwithSpace;");
        sendCommandToServer("USE    testwithSpace          ;");
        sendCommandToServer("CREATE      TABLE     marks   (      name,    mark       , pass       );");
        sendCommandToServer("INSERT INTO marks VALUES ('    Simon',       65     , TRUE     )        ;");
        sendCommandToServer("INSERT INTO     marks VALUES ('Sion',         55      , TRUE);");
        sendCommandToServer("INSERT INTO        marks VALUES ('Rob', 35,           FALSE);");
        sendCommandToServer("INSERT      INTO marks      VALUES       (         'Chris', 20, FALSE    )    ;");

        String response = sendCommandToServer("SELECT * FROM marks;");
        assertTrue(response.contains("[OK]"), "SELECT * should return [OK]");
        assertTrue(response.contains("Simon"), "Simon should be in the table");
        assertTrue(response.contains("Chris"), "Chris should be in the table");
        sendCommandToServer("DROP DATABASE testwithSpace;");
        assertTrue(response.contains("[OK]"), "SELECT * should return [OK]");
    }

}
