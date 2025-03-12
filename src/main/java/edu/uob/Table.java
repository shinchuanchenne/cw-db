package edu.uob;
import java.util.ArrayList;
import java.util.List;

public class Table {
    private String tableName;
    private List<String> columns;
    private List<Row> rows;

    public Table(String tableName, List<String> columns){
        this.tableName = tableName;
        this.columns = new ArrayList<>(columns);
        this.rows = new ArrayList<>();
    }
    public String getTableName() {
        return tableName;
    }

    public List<String> getColumns() {
        return new ArrayList<>(columns);
    }

    public void addRow (List<String> values){
        int newId = rows.isEmpty() ? 1 : rows.get(rows.size()-1).getId() + 1;
        rows.add(new Row(newId, values));
    }

    // 📌 **取得所有 Row**
    public List<Row> getRows() {
        return rows;
    }

    public Row getRow (int id){
        for (Row row : rows) {
            if (row.getId() == id) {
                return row;
            }
        }
        return null;
    }

    public boolean deleteRow (int id){
        return rows.removeIf(row -> row.getId() == id);
    }



    public boolean updateRow (int id, int columnIndex, String newValue){
        Row row = getRow(id);
        if (row != null) {
            row.setValues(columnIndex, newValue);
            return true;
        }
        return false;
    }

    public List<Row> getAllRows(){
        return new ArrayList<>(rows);
    }


    public List<Row> getRowsByCondition(int columnIndex, String value, String operator) {
        List<Row> result = new ArrayList<>();
        for (Row row : rows) {
            if (Select.compareValues(row.getValue(columnIndex), value, operator)) {
                result.add(row);
            }
        }
        return result;
    }


}
