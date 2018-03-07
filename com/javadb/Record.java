package com.javadb;

/**
 * A record in the database.
 * The values of a record are stored in a simple array.
 * The number of fields in a Record is defined at construction time, and cannot be changed.
 * Make a copy of the Record, when new fields are required.
 */
public class Record {
    private String[] values;

    Record(String... values) {
        this.values = values;
    }

    /**
     * @return A copy of the record's values array.
     */
    public String[] getValues() {
        String[] valuesCopy = new String[values.length];
        System.arraycopy(values, 0, valuesCopy, 0, values.length);

        return valuesCopy;
    }

    public String getValue(int n) {
        if (n < 0 || n >= values.length) {
            throw new ArrayIndexOutOfBoundsException();
        }

        return values[n];
    }

    public void setValue(int n, String s) {
        if (n < 0 || n >= values.length) {
            throw new ArrayIndexOutOfBoundsException();
        }

        values[n] = s;
    }

    public int size() {
        return values.length;
    }

    public void printRow() {
        for(String s : values) {
            System.out.print(s + ",");
        }
        System.out.println();
    }
}
