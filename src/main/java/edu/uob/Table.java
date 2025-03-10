package edu.uob;
import java.util.ArrayList;
import java.util.List;

public class Table {
    private String tableName;
    private List<String> columnNames;
    private int id = 1;

    public Table(String tableName, List<String> columnNames) {
        this.tableName = tableName;
        this.columnNames = new ArrayList<>(columnNames);
    }

}
