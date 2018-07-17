/*
 * Written by Evan Lalopoulos <evan.lalopoulos.2017@my.bristol.ac.uk>
 * Copyright (C) 2018 - All rights reserved.
 * Unauthorized copying of this file is strictly prohibited.
 */

package com.javadb;

import java.util.Map;
import java.util.stream.Stream;

/**
 * Defines the available column constraints and implements the Violates interface for each of them.
 */
public enum Constraint implements Violates {
    NOT_NULL {
        @Override
        public boolean isViolated(Stream<Map.Entry<String, Record>> s, String newValue, int colIndex) {
            return newValue.equals("");
        }
    },
    UNIQUE {
        @Override
        public boolean isViolated(Stream<Map.Entry<String, Record>> s, String newValue, int colIndex) {
            return s.anyMatch(entry -> {
                Record r = entry.getValue();
                return r.getValue(colIndex).equals(newValue);
            });
        }
    },
    // PK implies UNIQUE and NOT NULL.
    PRIMARY_KEY {
        @Override
        public boolean isViolated(Stream<Map.Entry<String, Record>> s, String newValue, int colIndex) {
            return NOT_NULL.isViolated(s, newValue, colIndex) || UNIQUE.isViolated(s, newValue, colIndex);
        }
    }
}
