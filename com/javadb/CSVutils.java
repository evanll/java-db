/*
 * Written by Evan Lalopoulos <evan.lalopoulos.2017@my.bristol.ac.uk>
 * Copyright (C) 2018 - All rights reserved.
 * Unauthorized copying of this file is strictly prohibited.
 */

package com.javadb;

import java.util.ArrayList;
import java.util.List;

/**
 * A utility class that handles CSV formatting and parsing.
 * Although it works for the most common CSV formats, it does not follow
 * a specific CSV standard. It's build for the specific requirements
 * of the java-db project.
 */
public class CSVutils {
    private static final char DELIMITER_CHAR = ',';
    private static final char QUOTES_CHAR = '"';
    // Todo: \n \r\n
    private static final char NEWLINE_CHAR = '\n';

    /**
     * Parses a CSV line and returns an array of values.
     * @param CSVline A raw CSV line to be split into values.
     * @return A String[] that contains the extracted values.
     */
    public static String[] parseCSVline(String CSVline) {
        List<String> values = new ArrayList<>();
        StringBuilder sb = new StringBuilder();

        char[] x = CSVline.toCharArray();
        char c;
        boolean inQuotes = false;
        for(int i = 0; i < x.length; i++) {
            c = x[i];
            if (!inQuotes) {
                if (c == DELIMITER_CHAR) {
                    values.add(sb.toString());
                    sb.setLength(0);
                } else if (c == QUOTES_CHAR) {
                    inQuotes = true;
                } else if (c != NEWLINE_CHAR){
                    sb.append(c);
                }
            } else {
                if (c == QUOTES_CHAR) {
                    if (isQuotesNext(x,i)) { // Is it an escape quote eg. "val""ue"
                        // Consume it and append it
                        i++;
                        sb.append(QUOTES_CHAR);
                    } else { // Or a matching closing quote "value"
                        inQuotes = false;
                    }
                } else {
                    sb.append(c);
                }
            }
        }
        // Adds the last value to results array
        values.add(sb.toString());

        return values.toArray(new String[values.size()]);
    }

    /**
     * Formats an array of values to CSV.
     * Values that contain commas are wrapped in double quotes.
     * Ex: green, red becomes ...,"green, red",...
     * Values that contain double quotes are wrapped in double quotes
     * and the double quotes are escaped using a preceding double quote.
     * Ex: He said "Hi" becomes ...,"He said ""Hi""",...
     * @param values An array of values to be converted to CSV record.
     * @return A string containing a CSV record.
     */
    public static String generateCSVRecord(String[] values) {
        StringBuilder sb = new StringBuilder();

        for(int i = 0; i < values.length; i++) {
            String value = values[i];

            if (containsSpecialChar(value)) {
                sb.append(handleSpecialChar(value));
            } else {
                sb.append(value);
            }

            // Add delimeter if not the final value
            if (i != values.length -1) {
                sb.append(DELIMITER_CHAR);
            }
        }

        sb.append(NEWLINE_CHAR);

        return sb.toString();
    }

    // Helper Functions

    private static String handleSpecialChar(String value) {
        StringBuilder sb = new StringBuilder();
        char x[] = value.toCharArray();

        for(char c : x) {
            if (c == QUOTES_CHAR) {
                // Escape quote with another preceding quote
                sb.append(QUOTES_CHAR);
            }
            sb.append(c);
        }

        // Wrap it on escape characters
        sb.insert(0, QUOTES_CHAR);
        sb.append(QUOTES_CHAR);

        return sb.toString();
    }

    private static boolean containsSpecialChar(String s) {
        return s != null &&
                        (s.indexOf(DELIMITER_CHAR) != -1 ||
                        s.indexOf(QUOTES_CHAR) != -1 ||
                        s.indexOf(NEWLINE_CHAR) != -1);
    }

    private static boolean isQuotesNext(char[] x, int index) {
        return (x != null && (index+1 < x.length) && x[index + 1] == QUOTES_CHAR);
    }

    // Unit Testing

    private static void test_parser() {
        // Basic test
        String csvLineA = "abc,ZYX,123,!@#$%^&*()_+<>/'{}\n";
        String[] parsedA = parseCSVline(csvLineA);
        assert(parsedA[0].equals("abc"));
        assert (parsedA[1].equals("ZYX"));
        assert (parsedA[2].equals("123"));
        assert (parsedA[3].equals("!@#$%^&*()_+<>/'{}"));

        // Comma inside value
        String csvLineB = "abc,\"kl,m\",qwe\n";
        String[] parsedB = parseCSVline(csvLineB);
        assert(parsedB[0].equals("abc"));
        assert (parsedB[1].equals("kl,m"));
        assert (parsedB[2].equals("qwe"));

        // Quotes inside value
        // Quotes and comma inside value
        String csvLineC = "abc,\"\"\"Hi\"\"\",\"\"\"Do it, if you\"\"\"\n";
        String[] parsedC = parseCSVline(csvLineC);
        assert(parsedC[0].equals("abc"));
        assert (parsedC[1].equals("\"Hi\""));
        assert (parsedC[2].equals("\"Do it, if you\""));
    }

    private static void test_csv_generator() {
        String val0 = "abc";
        String val1 = "zy,x";
        String val2 = "kl \"m\"";
        String val3 = "Dear,\n How";

        String[] values = new String[4];
        values[0] = val0;
        values[1] = val1;
        values[2] = val2;
        values[3] = val3;

        String CSVrecord = generateCSVRecord(values);
        String expectedOutput = "abc,\"zy,x\",\"kl \"\"m\"\"\",\"Dear,\n How\"\n";

        assert (CSVrecord.equals(expectedOutput));
    }

    private static void test_csv_generator_and_parser() {
        String val0 = "Amy";
        String val1 = "O'hare";
        String val2 = "Biologist, diver";
        String val3 = "My favorites quote is \"Carpe Diem\"";
        String val4 = "I like pizza, hamburger and tortilla.";
        String val5 = "Not much to say\n I love cats!";

        String[] values = new String[6];
        values[0] = val0;
        values[1] = val1;
        values[2] = val2;
        values[3] = val3;
        values[4] = val4;
        values[5] = val5;

        // Test CSV generator
        String CSVrecord = generateCSVRecord(values);
        String expectedOutput = "Amy," + "O'hare," + "\"Biologist, diver\"," +
                        "\"My favorites quote is \"\"Carpe Diem\"\"\"," +
                        "\"I like pizza, hamburger and tortilla.\"," +
                        "\"Not much to say\n I love cats!\"" + "\n";

        assert(CSVrecord.equals(expectedOutput));

        // Test with CSV parser
        String[] parsedValues = parseCSVline(CSVrecord);

        assert(val0.equals(parsedValues[0]));
        assert(val1.equals(parsedValues[1]));
        assert(val2.equals(parsedValues[2]));
        assert(val3.equals(parsedValues[3]));
        assert(val4.equals(parsedValues[4]));
        assert(val5.equals(parsedValues[5]));
    }

    public static void main(String[] args) {
        test_parser();
        test_csv_generator();
        test_csv_generator_and_parser();
    }
}
