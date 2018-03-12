package com.javadb;

import java.io.*;
import java.nio.file.FileSystems;
import java.nio.file.Path;

/**
 * Responsible for storing and loading tables from disk.
 */
public class StorageEngine {

    /**
     * Loads a table from a CSV file.
     * @param file Table file to be loaded
     * @return A Table object
     */
    public Table loadTableFile(File file) {
        Table t = null;

        try(BufferedReader br = new BufferedReader(new FileReader(file))) {
            // Handle first line as header, containing column name
            String line = br.readLine();
            String[] header;
            if (line != null) {
                header = CSVutils.parseCSVline(line);

                Column[] columns = new Column[header.length];
                for (int i = 0; i < header.length; i++) {
                    // Use first column as PK
                    if (i == 0) {
                        columns[i] = new Column(header[i], Constraint.PRIMARY_KEY);
                    } else {
                        columns[i] = new Column(header[i]);
                    }
                }

                // Create a new table
                String tableName = file.getName();
                tableName = tableName.substring(0, tableName.lastIndexOf("."));

                t = new Table(tableName, columns[0], columns);

                // Rest lines will be handled as table records
                String values[];
                while ((line = br.readLine()) != null) {
                    values = CSVutils.parseCSVline(line);
                    Record r = new Record(values);
                    t.insert(r);
                }
            }
        } catch (FileNotFoundException e) {
            throw new Error("Table file not found.");
        } catch (IOException e) {
            throw new Error("Unable to load talbe.");
        }

        return t;
    }

    /**
     * Saves a table to disk in CSV format.
     * @param t Table to be saved as CSV file.
     */
    public void saveTable(Table t, File file) {
        try(BufferedWriter bw = new BufferedWriter(new FileWriter(file,true))) {
            // Write columns
            Column[] columns = t.getColumns();
            String[] columnNames = new String[t.columns()];
            for(int i = 0; i< t.columns(); i++) {
                columnNames[i] = columns[i].getName();
            }
            bw.write(CSVutils.generateCSVRecord(columnNames));

            // Write table records
            t.getRows().forEach(entry -> {
                Record r = entry.getValue();
                try {
                    bw.write(CSVutils.generateCSVRecord(r.getValues()));
                } catch (Exception e) {
                    throw new Error("Unable to create table file.");
                }
            } );
        } catch (IOException e) {
            throw new Error("Unable to create table file.");
        }
    }

    // Unit testing

    private static void test() {
        StorageEngine storageEngine = new StorageEngine();

        Column c0 = new Column("First_Name", Constraint.PRIMARY_KEY);
        Column c1 = new Column("Last_Name");
        Column c2 = new Column("County");

        // Single Row Update
        Table t1 = new Table("t1", c0,c1,c2);
        assert(t1.columns() == 3);

        // Add records
        Record r0 = new Record("Angela", "Walker", "Bristol");
        Record r1 = new Record("Tom", "Olson", "London");
        Record r2 = new Record("Paul", "Hudson", "Manchester");
        Record r3 = new Record("Hannah", "Powell", "Essex");
        t1.insert(r0,r1,r2,r3);

        // Save table
        Path path = FileSystems.getDefault().getPath("Databases","SaveLoadTest", t1.getName() + ".csv");
        storageEngine.saveTable(t1,path.toFile());

        // Load table
        Table t1_loaded = storageEngine.loadTableFile(path.toFile());
    }

    public static void main(String[] args) {
        test();
    }
}
