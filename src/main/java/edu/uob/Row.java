package edu.uob;
import java.util.ArrayList;
import java.util.List;

public class Row {
    private int id;
    private List<String> values;

    public Row(int id, List<String> values) {
        this.id = id;
        this.values = new ArrayList<>(values);
    }


    public int getId (){
        return id;
    }
    public List<String> getValues (){
        return new ArrayList<>(values);
    }

    public String getValue (int columnIndex){
        if (columnIndex >= 0 && columnIndex < values.size()) {
            return values.get(columnIndex);
        }
        return "[ERROR] Column index out of range.";
    }

    public void setValues (int columnIndex, String newValue){
        if (columnIndex >= 0 && columnIndex < values.size()) {
            values.set(columnIndex, newValue);
        }
    }

}
