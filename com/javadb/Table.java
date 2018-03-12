package com.javadb;

import java.util.*;
import java.util.stream.Stream;

/**
 * A table class that holds columns and records.
 * Includes single and bulk table operations by key.
 */
public class Table {
    private String name;
    // List of unique columns
    private List<Column> columns;
    private Map<String, Record> records;
    private final Column pkCol;

    Table(String name, Column pkCol, Column... columns) {
        this.name = name;
        this.columns = new ArrayList<>();
        records = new LinkedHashMap<>();

        // Set table primary key
        if (!pkCol.isPK()) {
            throw new RuntimeException("Table without primary key.");
        }
        this.pkCol = pkCol;

        // Insert columns
        appendColumns(pkCol);
        appendColumns(columns);
    }

    public String getName() {
        return name;
    }

    /**
     * @return A copy of the table's columns.
     */
    public Column[] getColumns() {
        return columns.toArray(new Column[columns.size()]);
    }

    /**
     * The underlying Map that stores the records cannot be exposed directly.
     * A stream of key/value pairs is prefered over a deep copy, for ease of
     * use and efficiency in large tables.
     * @return A stream of map entries.
     */
    public Stream<Map.Entry<String, Record>> getRows() {
        return records.entrySet().stream();
    }

    /**
     * @return The number of columns.
     */
    public int columns() {
        return columns.size();
    }

    /**
     * @return The number of rows.
     */
    public int rows() {
        return records.size();
    }

    /**
     * @return The index of the column that serves as PK
     */
    private int getPKColIndex() {
        return columns.indexOf(pkCol);
    }

    // Alter Table Operations

    /**
     * Adds a column at certain index.
     * Each record is replaced by a new record conforming to the new table schema.
     */
    public boolean addColumn(int colIndex, Column c) {
        /* Check if the column to add is marked as Primary Key
         * Rejects any other columns that are marked as primary key, other than this
         * set in the table constructor.
         */

        // Allow only unique columns to be added
        for (Column column : columns) {
            if (c.equals(column)) {
                return false;
            }
        }


        if (c.getConstraints().contains(Constraint.PRIMARY_KEY) && c != pkCol) {
            return false;
        }

        // Add column to table
        columns.add(c);

        // Replace table rows to conform to the new table schema
        for (Map.Entry<String, Record> pair : records.entrySet()) {
            Record r = pair.getValue();

            List<String> newVals = new ArrayList<>();
            newVals.addAll(Arrays.asList(r.getValues()));
            newVals.add(colIndex, " ");

            // Create a new record and replace the old one in the table
            Record newRecord = new Record(newVals.toArray(new String[newVals.size()]));
            records.replace(pair.getKey(), newRecord);
        }

        return true;
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
    public boolean dropColumn(int colIndex) {
        // Stops dropping a column that serves as PK
        if (columns.get(colIndex) == pkCol) {
            return false;
        }

        // Remove column
        columns.remove(colIndex);

        // Modify table rows
        for (Map.Entry<String, Record> pair : records.entrySet()) {
            Record r = pair.getValue();

            List<String> newVals = new ArrayList<>();
            newVals.addAll(Arrays.asList(r.getValues()));
            newVals.remove(colIndex);

            // Create a new record and replace the old one in the table
            Record newRecord = new Record(newVals.toArray(new String[newVals.size()]));
            records.replace(pair.getKey(), newRecord);
        }

        return true;
    }

    public void truncate() {
        records.clear();
    }

    // Record Operations

    public boolean insert(Record r) {
        //check if record is compatible
        if (isViolation(r)) {
            return false;
        }

        records.put(r.getValue(getPKColIndex()), r);
        return true;
    }

    public void insert(Record... records) {
        for(Record r : records) {
            insert(r);
        }
    }

    public Record select_record(String key) {
        return records.get(key);
    }

    public Set<Record> select_record(Set<String> keys) {

        Set<Record> results = new LinkedHashSet<>();

        for(String key : keys) {
            Record r = select_record(key);
            if (r != null) {
                results.add(r);
            }
        }

        return results;
    }

    public boolean update(String key, int colIndex, String newValue) {
        if (colIndex < 0 || colIndex >= columns()) {
            throw new IndexOutOfBoundsException();
        }

        Record r = select_record(key);
        if (r == null) {
            return false;
        }

        // Check for constraint violations
        EnumSet<Constraint> constraintSet = columns.get(colIndex).getConstraints();

        for(Constraint constraint : constraintSet) {
            if (constraint.isViolated(records.entrySet().stream(), newValue, colIndex)) {
                return false;
            }
        }

        // If everything is ok modify record
        r.setValue(colIndex, newValue);
        return true;
    }

    public void update(Set<String> keys, int colIndex, String newValue) {
        if (colIndex < 0 || colIndex >= columns()) {
            throw new IndexOutOfBoundsException();
        }

        for(String key : keys) {
            update(key, colIndex, newValue);
        }
    }

    public Record delete(String key) {
        return records.remove(key);
    }

    public Set<Record> delete(Set<String> keys) {
        Set<Record> results = new LinkedHashSet<>();

        for(String key : keys) {
            Record r = delete(key);
            if (r != null) {
                results.add(r);
            }
        }

        return results;
    }

    /**
     * Checks if a record violates any column constraints.
     * @param r
     * @return True if any violations are detected.
     */
    private boolean isViolation(Record r) {
        // Checks if record has the same number of fields as columns
        if ( r.size() != columns.size()) {
            return true;
        }

        // Loops through columns and checks if any constraints are violated
        boolean violation = false;
        int i =0;
        while (i < columns.size()) {
            EnumSet<Constraint> constraintSet= columns.get(i).getConstraints();

            for (Constraint constraint : constraintSet) {
                violation = constraint.isViolated(records.entrySet().stream(), r.getValue(i), i);
                if (violation) {
                    return true;
                }
            }

            i++;
        }

        return false;
    }

    // Unit Testing

    public static void test_create_table() {
        Column c0 = new Column("First_Name", Constraint.PRIMARY_KEY);
        Column c1 = new Column("Last_Name");
        Column c2 = new Column("County");

        Table t1 = new Table("t1", c0, c1, c2);
        assert(t1.columns() == 3);

        // Add records
        Record r0 = new Record("Angela", "Walker", "Bristol");
        Record r1 = new Record("Tom", "Olson", "London");
        Record r2 = new Record("Paul", "Hudson", "Manchester");
        Record r3 = new Record("Hannah", "Powell", "Essex");
        t1.insert(r0,r1,r2,r3);
        assert(t1.rows() == 4);

        // Truncate Table
        t1.truncate();
        assert(t1.rows() == 0);
    }

    public static void test_insertion() {
        Column c0 = new Column("First_Name", Constraint.PRIMARY_KEY);
        Column c1 = new Column("Last_Name");
        Column c2 = new Column("County");

        Table t1 = new Table("t1", c0,c1,c2);
        assert(t1.columns() == 3);

        // Add records
        Record r0 = new Record("Angela", "Walker", "Bristol");
        Record r1 = new Record("Tom", "Olson", "London");
        Record r2 = new Record("Paul", "Hudson", "Manchester");
        Record r3 = new Record("Hannah", "Powell", "Essex");
        t1.insert(r0,r1,r2,r3);
        assert(t1.rows() == 4);

        //Incompatible Record Insertion

        // Record with less columns than table
        t1.insert(new Record("Helen", "Knight"));
        assert(t1.rows() == 4);

        // Record with more columns than table
        t1.insert(new Record("Albert", "Grey", "London", "Lawyer"));
        assert(t1.rows() == 4);
    }

    public static void test_selection() {
        Column c0 = new Column("First_Name", Constraint.PRIMARY_KEY);
        Column c1 = new Column("Last_Name");
        Column c2 = new Column("County");

        Table t1 = new Table("t1", c0,c1,c2);
        assert(t1.columns() == 3);

        Record r0 = new Record("Angela", "Walker", "Bristol");
        Record r1 = new Record("Tom", "Olson", "London");
        Record r2 = new Record("Paul", "Hudson", "Manchester");
        Record r3 = new Record("Hannah", "Powell", "Essex");

        t1.insert(r0,r1,r2,r3);
        assert(t1.rows() == 4);

        // Select Record
        assert(t1.select_record("Angela") == r0);
        assert(t1.select_record("Tom") == r1);

        // Select Multiple Records
        Set<String> keys = new HashSet<>();
        keys.add("Paul");
        keys.add("Hannah");
        Set<Record> selection = t1.select_record(keys);
        assert(selection.size() == 2);
        assert(selection.contains(r2) && selection.contains(r3));

        // Select Multiple Records, invalid keys passed
        keys.clear();
        keys.add("XXX");
        keys.add("Hannah");
        Set<Record> selection2 = t1.select_record(keys);

        assert(selection2.size() == 1);
        assert(selection2.contains(r3));

        // Check that table is not modified
        assert(t1.rows() == 4);
    }

    public static void test_update() {
        Column c0 = new Column("First_Name", Constraint.PRIMARY_KEY);
        Column c1 = new Column("Last_Name");
        Column c2 = new Column("County");

        // Single Row Update
        Table t1 = new Table("t1",c0,c1,c2);
        assert(t1.columns() == 3);

        // Add records
        Record r0 = new Record("Angela", "Walker", "Bristol");
        Record r1 = new Record("Tom", "Olson", "London");
        Record r2 = new Record("Paul", "Hudson", "Manchester");
        Record r3 = new Record("Hannah", "Powell", "Essex");
        t1.insert(r0,r1,r2,r3);
        assert(t1.rows() == 4);

        assert(t1.update("Tom",2, "York"));
        assert(t1.select_record("Tom").getValue(2).equals("York"));

        // Bulk Update
        Table t2 = new Table("t2",c0,c1,c2);
        assert(t2.columns() == 3);

        // Add records
        Record r1_0 = new Record("Angela", "Walker", "Bristol");
        Record r1_1 = new Record("Tom", "Olson", "London");
        Record r1_2 = new Record("Paul", "Hudson", "Manchester");
        Record r1_3 = new Record("Hannah", "Powell", "Essex");
        t2.insert(r1_0, r1_1, r1_2, r1_3);

        Set<String> keys = new HashSet<>();
        keys.add("Tom");
        keys.add("Hannah");
        t2.update(keys, 2, "Hampshire");

        assert(r1_1.getValue(2).equals("Hampshire") && r1_3.getValue(2).equals("Hampshire"));
    }

    public static void test_deletion() {
        Column c0 = new Column("First_Name", Constraint.PRIMARY_KEY);
        Column c1 = new Column("Last_Name");
        Column c2 = new Column("County");

        Table t1 = new Table("t1",c0,c1,c2);
        assert (t1.columns() == 3);

        Record r0 = new Record("Angela", "Walker", "Bristol");
        Record r1 = new Record("Tom", "Olson", "London");
        Record r2 = new Record("Paul", "Hudson", "Manchester");
        Record r3 = new Record("Hannah", "Powell", "Essex");
        t1.insert(r0, r1, r2, r3);
        assert (t1.rows() == 4);

        // Delete single record
        assert(t1.delete("Tom") == r1);
        assert(t1.rows() == 3);

        // Delete Multiple Records
        Table t2 = new Table("t2",c0,c1,c2);
        t2.insert(r0,r1,r2,r3);
        assert(t2.rows() == 4);

        Set<String> keys = new HashSet<>();
        keys.add("Tom");
        keys.add("Paul");

        Set<Record> deleted = t2.delete(keys);
        assert(deleted.size() == 2);
        assert(deleted.contains(r1) && deleted.contains(r2));
        assert(t2.rows() == 2);

        // Delete Multiple Records with invalid keys
        keys.clear();
        deleted.clear();

        Table t3 = new Table("t3",c0,c1,c2);
        t3.insert(r0,r1,r2,r3);

        keys.add("AAA");
        keys.add("Paul");

        deleted = t3.delete(keys);
        assert(deleted.size() == 1);
        assert(deleted.contains(r2));
        assert(t3.rows() == 3);
    }

    public static void test_alter_table_append_columns() {
        Column c0 = new Column("First_Name", Constraint.PRIMARY_KEY);
        Column c1 = new Column("Last_Name");
        Column c2 = new Column("County");

        Table t1 = new Table("t1", c0, c1, c2);
        assert(t1.columns() == 3);

        Column c3 = new Column("Col_3");
        Column c4 = new Column("Col_4");
        t1.appendColumns(c3,c4);
        assert(t1.columns() == 5);
    }

    public static void test_alter_table_remove_columns() {
        Column c0 = new Column("First_Name", Constraint.PRIMARY_KEY);
        Column c1 = new Column("Last_Name");
        Column c2 = new Column("County");

        Table t1 = new Table("t1", c0, c1, c2);
        assert(t1.columns() == 3);

        Record r0 = new Record("Angela", "Walker", "Bristol");
        Record r1 = new Record("Tom", "Olson", "London");
        Record r2 = new Record("Paul", "Hudson", "Manchester");
        Record r3 = new Record("Hannah", "Powell", "Essex");
        t1.insert(r0,r1,r2,r3);
        assert(t1.rows() == 4);

        // Remove middle column
        t1.dropColumn(1);
        assert(t1.columns() == 2);
        assert(t1.rows() == 4);

        // Check that records are altered as well
        t1.getRows().forEach(entry -> {
            Record r = entry.getValue();
            assert (r.size() == 2);
        });

        // Also check a random sample
        assert(t1.select_record("Angela").getValue(1).equals("Bristol"));

        // Try to drop PK col
        t1.dropColumn(0);
        assert(t1.columns() == 2);
    }

    public static void test_constraints_primary_key() {
        Column c0 = new Column("Id", Constraint.PRIMARY_KEY);
        Column c1 = new Column("First_Name", Constraint.NOT_NULL);
        Column c2 = new Column("Last_Name", Constraint.NOT_NULL, Constraint.UNIQUE);
        Column c3 = new Column("County", Constraint.NOT_NULL);

        Table t1 = new Table("t1",c0,c1,c2,c3);
        assert(t1.columns() == 4);

        // Add records
        Record r0 = new Record("0","Angela", "Walker", "Bristol");
        Record r1 = new Record("1","Tom", "Olson", "London");
        Record r2 = new Record("2","Paul", "Hudson", "Manchester");
        Record r3 = new Record("3","Hannah", "Powell", "Essex");
        t1.insert(r0,r1,r2,r3);
        assert(t1.rows() == 4);

        // Insert

        // id Primary key constraint, not unique PK
        assert (t1.insert(new Record("3","Helen", "White", "London")) == false);
        assert(t1.rows() == 4);
        // id Primary key constraint, null PK
        assert (t1.insert(new Record("","Helen", "White", "London")) == false);
        assert(t1.rows() == 4);
        // id Primary key constraint
        assert (t1.insert(new Record("4","Helen", "White", "London")));
        assert(t1.rows() == 5);

        // First name constraint, null
        assert (t1.insert(new Record("5","", "Madisson", "London")) == false);
        assert(t1.rows() == 5);
        // First name constraint, not null
        assert (t1.insert(new Record("5","Jane", "Madisson", "London")));
        assert(t1.rows() == 6);

        // Last name constraint, not unique
        assert (t1.insert(new Record("6","John", "Madisson", "London")) == false);
        assert(t1.rows() == 6);
        // Last name constraint, null
        assert (t1.insert(new Record("6","John", "", "London")) == false);
        assert(t1.rows() == 6);

        // Update

        // Update Last Name to Not Unique value
        assert(t1.update("1",2, "Walker") == false);

        // Update Last Name to Null value
        assert(t1.update("1",2, "") == false);

        // Update County with Not Null value
        assert(t1.update("1",3, "Bristol"));
    }

    public static void main(String[] args) {
        test_create_table();
        test_insertion();
        test_update();
        test_selection();
        test_deletion();
        test_alter_table_append_columns();
        test_alter_table_remove_columns();
        test_constraints_primary_key();
    }
}
