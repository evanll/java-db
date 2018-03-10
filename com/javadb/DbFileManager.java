package com.javadb;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DbFileManager {



    public List<Record> read_table() {
        List<Record> records = new ArrayList<>();

        try(BufferedReader reader = new BufferedReader(new FileReader("test.csv"))) {
            String values[];
            String line;
            while ((line = reader.readLine()) != null) {
                values = CSV.parseCSVline(line);
                Record r = new Record(values);
                records.add(r);
            }
        } catch (IOException e) {
            throw new Error("Unable to read file.");
        }

        return records;
    }


    public void write(String tableName, ArrayList<String> columns, List<Record> records) {
        try(BufferedWriter tableFile = new BufferedWriter(new FileWriter(tableName + ".csv"))) {
            tableFile.write(CSV.generateCSVRecord(columns));

            for (Record r : records) {
                tableFile.write(CSV.generateCSVRecord(Arrays.asList(r.getValues())));
            }
        } catch (IOException e) {
            throw new Error("Unable to create file.");
        }
    }

    public static void main(String[] args) {
        DbFileManager fileManager = new DbFileManager();

//        ArrayList<String> columns = new ArrayList<>();
//        columns.add("first_name");
//        columns.add("last_name");
//        columns.add("location");
//
//        Record r0 = new Record("Angela", "Walker", "Bristol");
//        Record r1 = new Record("Tom", "Olson", "\",London");
//        Record r2 = new Record("Paul", "Hudson", "Manchester");
//        Record r3 = new Record("Hannah", "Powell", "Essex");
//
//        ArrayList<Record> records = new ArrayList<>();
//        records.add(r0);
//        records.add(r1);
//        records.add(r2);
//        records.add(r3);
//
//        fileManager.write("test", columns, records);


       // List<Record> records_parsed = fileManager.read_table();

//        for (Record r : records_parsed) {
//            System.out.println(r.toString());
//        }
    }
}
