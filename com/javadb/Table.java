package com.javadb;

import java.util.*;
/**
 * A table class that holds columns and records.
 * Columns are stored in an ArrayList.
 * Records are stored in a LinkedHashMap.
 * Includes single and bulk table operations by key.
 */
public class Table {
    private List<Column> columns;
    private Map<Integer, Record> records;


    Table(Column... columns) {
        this.columns = new ArrayList<>();
        this.columns.addAll(Arrays.asList(columns));

        records = new LinkedHashMap<>();
    }

    public int getColumns() {
        return columns.size();
    }

    public int getRows() {
        return records.size();
    }

    // Alter Table Operations

    /**
     * Adds a column at certain index.
     * Each record is replaced by a new record conforming to the new table schema.
     */
    public void addColumn(int colIndex, Column c) {
        columns.add(colIndex, c);

        // Modify table rows
        for (Map.Entry<Integer, Record> pair : records.entrySet()) {
            Record r = pair.getValue();

            List<String> newVals = new ArrayList<>();
            newVals.addAll(Arrays.asList(r.getValues()));
            newVals.add(colIndex, " ");

            // Create a new record and replace the old one in the table
            Record newRecord = new Record(newVals.toArray(new String[newVals.size()]));
            records.replace(pair.getKey(), newRecord);
        }
    }

    /**
     * Add new columns after the last column
     */
    public void appendColumns(Column... columns) {
        for(Column c:columns) {
            addColumn(this.columns.size(), c);
        }
    }

    /**
     * Drops a column from the table.
     * Each record is replaced by a new record conforming to the new table schema.
     */
    public void dropColumn(int colIndex) {
        columns.remove(colIndex);

        // Modify table rows
        for (Map.Entry<Integer, Record> pair : records.entrySet()) {
            Record r = pair.getValue();

            List<String> newVals = new ArrayList<>();
            newVals.addAll(Arrays.asList(r.getValues()));
            newVals.remove(colIndex);

            // Create a new record and replace the old one in the table
            Record newRecord = new Record(newVals.toArray(new String[newVals.size()]));
            records.replace(pair.getKey(), newRecord);
        }
    }

    public void truncate() {
        records.clear();
    }

    // Record Operations

    public boolean insert(Record r) {
        //check if record is compatible
        if ( r.size() != columns.size()) {
            return false;
        }

        records.put(getRows(), r);
        return true;
    }

    public void insert(Record... records) {
        for(Record r : records) {
            insert(r);
        }
    }

    public Record select_record(int key) {
        return records.get(key);
    }

    public Set<Record> select_record(Set<Integer> keys) {

        Set<Record> results = new LinkedHashSet<>();

        for(Integer key : keys) {
            Record r = select_record(key);
            if (r != null) {
                results.add(r);
            }
        }

        return results;
    }

    public boolean update(int key, int colIndex, String newValue) {
        if (colIndex < 0 || colIndex >= getColumns()) {
            throw new IndexOutOfBoundsException();
        }

        Record r = select_record(key);
        if (r == null) {
            return false;
        }

        r.setValue(colIndex, newValue);
        return true;
    }

    public void update(Set<Integer> keys, int colIndex, String newValue) {
        if (colIndex < 0 || colIndex >= getColumns()) {
            throw new IndexOutOfBoundsException();
        }

        for(Integer key : keys) {
            update(key, colIndex, newValue);
        }
    }

    public Record delete(int key) {
        return records.remove(key);
    }

    public Set<Record> delete(Set<Integer> keys) {
        Set<Record> results = new LinkedHashSet<>();

        for(Integer key : keys) {
            Record r = delete(key);
            if (r != null) {
                results.add(r);
            }
        }

        return results;
    }

    // TODO: Temporary. Fix column identation, move to view
    public void printTable() {
        for(Column c : columns) {
            System.out.print(c.toString() + ",");
        }
        System.out.println();
        for (Map.Entry<Integer, Record> pair : records.entrySet()) {
            pair.getValue().printRow();
        }
    }

    // Unit Testing

    public static void test_create_table() {
        Column c0 = new Column("First_Name");
        Column c1 = new Column("Last_Name");
        Column c2 = new Column("County");

        Table t1 = new Table(c0, c1, c2);
        assert(t1.getColumns() == 3);

        // Add records
        Record r0 = new Record("Angela", "Walker", "Bristol");
        Record r1 = new Record("Tom", "Olson", "London");
        Record r2 = new Record("Paul", "Hudson", "Manchester");
        Record r3 = new Record("Hannah", "Powell", "Essex");
        t1.insert(r0,r1,r2,r3);
        assert(t1.getRows() == 4);

        // Truncate Table
        t1.truncate();
        assert(t1.getRows() == 0);
    }

    public static void test_insertion() {
        Column c0 = new Column("First_Name");
        Column c1 = new Column("Last_Name");
        Column c2 = new Column("County");

        Table t1 = new Table(c0,c1,c2);
        assert(t1.getColumns() == 3);

        // Add records
        Record r0 = new Record("Angela", "Walker", "Bristol");
        Record r1 = new Record("Tom", "Olson", "London");
        Record r2 = new Record("Paul", "Hudson", "Manchester");
        Record r3 = new Record("Hannah", "Powell", "Essex");
        t1.insert(r0,r1,r2,r3);
        assert(t1.getRows() == 4);

        //Incompatible Record Insertion

        // Record with less columns than table
        t1.insert(new Record("Helen", "Knight"));
        assert(t1.getRows() == 4);

        // Record with more columns than table
        t1.insert(new Record("Albert", "Grey", "London", "Lawyer"));
        assert(t1.getRows() == 4);
    }

    public static void test_selection() {
        Column c0 = new Column("First_Name");
        Column c1 = new Column("Last_Name");
        Column c2 = new Column("County");

        Table t1 = new Table(c0,c1,c2);
        assert(t1.getColumns() == 3);

        Record r0 = new Record("Angela", "Walker", "Bristol");
        Record r1 = new Record("Tom", "Olson", "London");
        Record r2 = new Record("Paul", "Hudson", "Manchester");
        Record r3 = new Record("Hannah", "Powell", "Essex");

        t1.insert(r0,r1,r2,r3);
        assert(t1.getRows() == 4);

        // Select Record
        assert(t1.select_record(0) == r0);
        assert(t1.select_record(1) == r1);

        // Select Multiple Records
        Set<Integer> keys = new HashSet<>();
        keys.add(2);
        keys.add(3);
        Set<Record> selection = t1.select_record(keys);
        assert(selection.size() == 2);
        assert(selection.contains(r2) && selection.contains(r3));

        // Select Multiple Records, invalid keys passed
        keys.clear();
        keys.add(-2);
        keys.add(3);
        Set<Record> selection2 = t1.select_record(keys);

        assert(selection2.size() == 1);
        assert(selection2.contains(r3));

        // Check that table is not modified
        assert(t1.getRows() == 4);
    }

    public static void test_update() {
        Column c0 = new Column("First_Name");
        Column c1 = new Column("Last_Name");
        Column c2 = new Column("County");

        // Single Row Update
        Table t1 = new Table(c0,c1,c2);
        assert(t1.getColumns() == 3);

        // Add records
        Record r0 = new Record("Angela", "Walker", "Bristol");
        Record r1 = new Record("Tom", "Olson", "London");
        Record r2 = new Record("Paul", "Hudson", "Manchester");
        Record r3 = new Record("Hannah", "Powell", "Essex");
        t1.insert(r0,r1,r2,r3);
        assert(t1.getRows() == 4);

        assert(t1.update(1,2, "York"));
        assert(t1.select_record(1).getValue(2).equals("York"));

        // Bulk Update
        Table t2 = new Table(c0,c1,c2);
        assert(t2.getColumns() == 3);

        // Add records
        Record r1_0 = new Record("Angela", "Walker", "Bristol");
        Record r1_1 = new Record("Tom", "Olson", "London");
        Record r1_2 = new Record("Paul", "Hudson", "Manchester");
        Record r1_3 = new Record("Hannah", "Powell", "Essex");
        t2.insert(r1_0, r1_1, r1_2, r1_3);

        Set<Integer> keys = new HashSet<>();
        keys.add(1);
        keys.add(3);
        t2.update(keys, 2, "Hampshire");

        assert(r1_1.getValue(2).equals("Hampshire") && r1_3.getValue(2).equals("Hampshire"));
    }

    public static void test_deletion() {
        Column c0 = new Column("First_Name");
        Column c1 = new Column("Last_Name");
        Column c2 = new Column("County");

        Table t1 = new Table(c0,c1,c2);
        assert (t1.getColumns() == 3);

        Record r0 = new Record("Angela", "Walker", "Bristol");
        Record r1 = new Record("Tom", "Olson", "London");
        Record r2 = new Record("Paul", "Hudson", "Manchester");
        Record r3 = new Record("Hannah", "Powell", "Essex");
        t1.insert(r0, r1, r2, r3);
        assert (t1.getRows() == 4);

        // Delete single record
        assert(t1.delete(1) == r1);
        assert(t1.getRows() == 3);

        // Delete Multiple Records
        Table t2 = new Table(c0,c1,c2);
        t2.insert(r0,r1,r2,r3);
        assert(t2.getRows() == 4);

        Set<Integer> keys = new HashSet<>();
        keys.add(1);
        keys.add(2);

        Set<Record> deleted = t2.delete(keys);
        assert(deleted.size() == 2);
        assert(deleted.contains(r1) && deleted.contains(r2));
        assert(t2.getRows() == 2);

        // Delete Multiple Records with invalid keys
        keys.clear();
        deleted.clear();

        Table t3 = new Table(c0,c1,c2);
        t3.insert(r0,r1,r2,r3);

        keys.add(-2);
        keys.add(2);

        deleted = t3.delete(keys);
        assert(deleted.size() == 1);
        assert(deleted.contains(r2));
        assert(t3.getRows() == 3);
    }

    public static void test_alter_table_append_columns() {
        Column c0 = new Column("First_Name");
        Column c1 = new Column("Last_Name");
        Column c2 = new Column("County");

        Table t1 = new Table(c0, c1, c2);
        assert(t1.getColumns() == 3);

        Column c3 = new Column("Col_3");
        Column c4 = new Column("Col_4");
        t1.appendColumns(c3,c4);
        assert(t1.getColumns() == 5);
    }

    public static void test_alter_table_remove_columns() {
        Column c0 = new Column("First_Name");
        Column c1 = new Column("Last_Name");
        Column c2 = new Column("County");

        Table t1 = new Table(c0, c1, c2);
        assert(t1.getColumns() == 3);

        Record r0 = new Record("Angela", "Walker", "Bristol");
        Record r1 = new Record("Tom", "Olson", "London");
        Record r2 = new Record("Paul", "Hudson", "Manchester");
        Record r3 = new Record("Hannah", "Powell", "Essex");
        t1.insert(r0,r1,r2,r3);
        assert(t1.getRows() == 4);

        // Remove middle column
        t1.dropColumn(1);
        assert(t1.getColumns() == 2);
        assert(t1.getRows() == 4);

        // Check that records are altered as well
        for(int i=0; i<4; i++) {
            Record r = t1.select_record(i);
            assert(r.size() == 2);
        }

        // Also check a random sample
        assert(t1.select_record(0).getValue(1).equals("Bristol"));
    }

    public static void main(String[] args) {
        test_create_table();
        test_insertion();
        test_update();
        test_selection();
        test_deletion();
        test_alter_table_append_columns();
        test_alter_table_remove_columns();
    }
}
