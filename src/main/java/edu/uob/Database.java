package edu.uob;
import java.io.*;
import java.util.*;

public class Database {
    private String name;
    private Map<String, Table> tables; // 用 Map 存儲表格

    // 🏗️ **構造函式**
    public Database(String name) {
        this.name = name;
        this.tables = new HashMap<>();
    }

    // 📋 **取得資料庫名稱**
    public String getName() {
        return name;
    }

    // 📌 **1️⃣ 新增 `Table`**
    public void addTable(String tableName, List<String> columns) {
        if (!tables.containsKey(tableName)) {
            tables.put(tableName, new Table(tableName, columns));
        }
    }

    // 📌 **2️⃣ 刪除 `Table`**
    public void dropTable(String tableName) {
        tables.remove(tableName);
    }

    // 📌 **3️⃣ 取得 `Table`**
    public Table getTable(String tableName) {
        return tables.get(tableName);
    }

    // 📌 **4️⃣ 從檔案載入 `Table`**
    public void loadFromFile() {
        File dbFolder = new File("databases" + File.separator + name);
        if (!dbFolder.exists()) {
            dbFolder.mkdir(); // 如果資料夾不存在，創建一個
        }

        File[] tableFiles = dbFolder.listFiles();
        if (tableFiles != null) {
            for (File file : tableFiles) {
                if (file.getName().endsWith(".tab")) {
                    try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                        String header = reader.readLine();
                        List<String> columns = Arrays.asList(header.split("\t"));
                        Table table = new Table(file.getName().replace(".tab", ""), columns);

                        String line;
                        while ((line = reader.readLine()) != null) {
                            List<String> values = Arrays.asList(line.split("\t"));
                            table.addRow(values);
                        }
                        tables.put(table.getTableName(), table);
                    } catch (IOException e) {
                        System.out.println("[ERROR] Failed to load table: " + file.getName());
                    }
                }
            }
        }
    }

    // 📌 **5️⃣ 將 `Table` 儲存回 `.tab` 檔案**
    public void saveToFile() {
        File dbFolder = new File("databases" + File.separator + name);
        if (!dbFolder.exists()) {
            dbFolder.mkdir();
        }

        for (Table table : tables.values()) {
            File file = new File(dbFolder, table.getTableName() + ".tab");
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
                writer.write(String.join("\t", table.getColumns()) + "\n");
                for (Row row : table.getRows()) {
                    writer.write(row.getId() + "\t" + String.join("\t", row.getValues()) + "\n");
                }
            } catch (IOException e) {
                System.out.println("[ERROR] Failed to save table: " + table.getTableName());
            }
        }
    }
}
