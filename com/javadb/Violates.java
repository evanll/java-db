/*
 * Written by Evan Lalopoulos <evan.lalopoulos.2017@my.bristol.ac.uk>
 * Copyright (C) 2018 - All rights reserved.
 * Unauthorized copying of this file is strictly prohibited.
 */

package com.javadb;

import java.util.Map;
import java.util.stream.Stream;

/**
 * An interface for handling column constraints.
 */
public interface Violates {
    boolean isViolated(Stream<Map.Entry<String, Record>> s, String newValue, int colIndex);
}
