package com.javadb;

/**
 * Handles textual output in terminal.
 * Currently prints a table with the columns lined up.
 */
public class ConsoleView {
    private static final char HOR_BORDER = '-';
    private static final char COL_DIVIDER = '|';
    private static final char CORNER_MARK = '+';

    // Print table

    /**
     * Prints a single row from a table.
     * Formatted so that all columns are lined up, when displaying a table.
     * @param values An array containg the values of the record to be displayed
     * @param colWidth An array containing the width of each column
     */
    public void printRow(String[] values, int[] colWidth) {
        if (values == null || colWidth == null) {
            return;
        }

        // Prints upper horizontal border
        System.out.println(buildHorBorder(colWidth));

        for(int i =0; i < values.length; i++) {
            System.out.print(
                    String.format(COL_DIVIDER + "%-" + colWidth[i] + "s", values[i])
            );
        }
        System.out.print(COL_DIVIDER);
        System.out.println();
    }

    /**
     * Builds the table's horizontal border.
     * @param colWidths An array with the width of each column.
     * @return A string containing the horizontal border.
     */
    private String buildHorBorder(int[] colWidths) {
        if (colWidths == null) {
            throw new Error("Border building failed.");
        }

        StringBuilder sb = new StringBuilder();

        for(int cw : colWidths) {
            sb.append(CORNER_MARK);
            for (int i = 0; i < cw; i++) {
                sb.append(HOR_BORDER);
            }
        }
        sb.append(CORNER_MARK);

        return sb.toString();
    }

    public void printTableBorder(int[] colWidth) {
        if (colWidth == null) {
            return;
        }

        System.out.println(buildHorBorder(colWidth));
    }
}
