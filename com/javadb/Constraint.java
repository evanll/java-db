package com.javadb;

import java.util.Map;
import java.util.stream.Stream;

/**
 * Defines the available column constraints and implements the Violate interface for each of them.
 */
public enum Constraint implements Violates {
    NOT_NULL {
        @Override
        public boolean isViolated(Stream<Map.Entry<Integer, Record>> s, Record newRecord, int colIndex) {
            return newRecord.getValue(colIndex).equals("");
        }
    },
    UNIQUE {
        @Override
        public boolean isViolated(Stream<Map.Entry<Integer, Record>> s, Record newRecord, int colIndex) {
            return s.anyMatch(entry -> {
                Record r = entry.getValue();
                return r.getValue(colIndex).equals(newRecord.getValue(colIndex));
            });
        }
    },
    // PK implies UNIQUE and NOT NULL.
    PRIMARY_KEY {
        @Override
        public boolean isViolated(Stream<Map.Entry<Integer, Record>> s, Record newRecord, int colIndex) {
            return NOT_NULL.isViolated(s, newRecord, colIndex) || UNIQUE.isViolated(s, newRecord, colIndex);
        }
    }
}
