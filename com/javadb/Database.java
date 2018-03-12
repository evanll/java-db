package com.javadb;

import java.io.IOException;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * A collection of related tables.
 */
public class Database {
    private String name;
    private List<Table> tables;
    private StorageEngine storageEngine;

    Database(String name) {
        this.name = name;
        tables = new ArrayList<>();
        storageEngine = new StorageEngine();
    }

    public String getName() {
        return name;
    }

    public List<Table> getTables() {
        return tables;
    }

    public void addTable(Table... t) {
        tables.addAll(Arrays.asList(t));
    }

    /**
     * Loads all database tables from disk.
     */
    public void loadDb() {
        Path dbDir = FileSystems.getDefault().getPath("Databases", name);
        try(DirectoryStream<Path> contents = Files.newDirectoryStream(dbDir, "*.csv")) {
            for(Path path : contents ) {
                Table t = storageEngine.loadTableFile(path.toFile());
                tables.add(t);
            }
        }catch (IOException e) {
            throw new Error("Unable to load database.");
        }
    }

    /**
     * Saves all database tables to disk.
     */
    public void saveDB() {
        for (Table t : tables) {
            Path path = FileSystems.getDefault().getPath("Databases", name, t.getName() + ".csv");
            storageEngine.saveTable(t, path.toFile());
        }

    }
}
