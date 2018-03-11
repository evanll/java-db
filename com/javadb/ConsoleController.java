package com.javadb;

/**
 * Controls the ConsoleView.
 */
public class ConsoleController {
    private ConsoleView consoleView;

    ConsoleController() {
        this.consoleView = null;
    }

    public void setConsoleView(ConsoleView consoleView) {
        this.consoleView = consoleView;
    }

    public void printTable(Table t) {
        if (t == null || consoleView == null) {
            return;
        }

        // Compute the width of each column
        int[] colWidth = computeColWidth(t);

        // First row contains the column names
        Column[] columns = t.getColumns();
        String[] colNames = new String[t.columns()];
        for(int i = 0; i<t.columns(); i++) {
            colNames[i] = columns[i].getName();
        }
        consoleView.printRow(colNames, colWidth);

        // Display rows
        t.getRows().forEach(entry -> {
            Record r = entry.getValue();
            consoleView.printRow(r.getValues(), colWidth);
        });

        // Closing table border
        consoleView.printTableBorder(colWidth);
    }

    /**
     * Computes the column width in chars for a table.
     * Goes through all the records in a table and finds
     * the longest value in each column.
     * @param t
     * @return An array containing the width of each column.
     */
    private int[] computeColWidth(Table t) {
        if (t == null) {
            throw new NullPointerException();
        }

        // Calculate the width of each column by finding how many chars is the longest string
        int[] maxWidth = new int[t.columns()];

        // Start by column names
        Column[] columns = t.getColumns();
        for (int i = 0; i < t.columns(); i++) {
            maxWidth[i] = columns[i].getName().length();
        }

        // Go trough fields by column
        t.getRows().forEach(entry -> {
            Record r = entry.getValue();

            for(int i=0; i < t.columns(); i++) {
                if (r.getValue(i).length() > maxWidth[i]) {
                    maxWidth[i] = r.getValue(i).length();
                }
            }
        });

        return maxWidth;
    }

    // Unit testing
    private static void test() {
        Column c0 = new Column("First_Name");
        Column c1 = new Column("Last_Name");
        Column c2 = new Column("County");

        Table t1 = new Table("t1", c0,c1,c2);

        // Add records
        Record r0 = new Record("Angela", "Walker", "Bristol");
        Record r1 = new Record("Tom", "Olson", "London");
        Record r2 = new Record("George - Paul", "Hudson", "Manchester");
        Record r3 = new Record("Hannah", "Powell", "East Riding of Yorkshire");
        t1.insert(r0,r1,r2,r3);

        ConsoleController consoleController = new ConsoleController();
        ConsoleView consoleView= new ConsoleView();
        consoleController.setConsoleView(consoleView);
        consoleController.printTable(t1);
    }

    public static void main(String[] args) {
        test();
    }

}
