package com.javadb;

public class Main {
    public static void main(String[] args) {
        boolean ea = false;
        assert(ea = true); // Definitely = and not ==
        if (!ea) {
            System.out.println("Attension: No -ea flag given!\n");
        }

        String[] testArgs = {};
        if (ea) {System.out.println("Running tests...");}
        Table.main(testArgs);
        StorageEngine.main(testArgs);
        CSVutils.main(testArgs);
        if (ea) {System.out.println("All tests passed.\n");}
        System.out.println("Table load, save, printing:\n");
        HelloDB.main(testArgs);
    }
}
