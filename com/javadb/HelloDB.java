/*
 * Written by Evan Lalopoulos <evan.lalopoulos.2017@my.bristol.ac.uk>
 * Copyright (C) 2018 - All rights reserved.
 * Unauthorized copying of this file is strictly prohibited.
 */

package com.javadb;

/**
 * A sample database for testing.
 */
public class HelloDB {
    public static void main(String[] args) {
        Database db = new Database("HelloDB");

        ConsoleController consoleController = new ConsoleController();
        ConsoleView consoleView = new ConsoleView();
        consoleController.setConsoleView(consoleView);

        db.loadDb();
        db.saveDB();

        for(Table t : db.getTables()) {
            consoleController.printTable(t);
        }
    }
}
