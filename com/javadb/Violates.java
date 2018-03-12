package com.javadb;

import java.util.Map;
import java.util.stream.Stream;

/**
 * An interface for handling column constraints.
 */
public interface Violates {
    boolean isViolated(Stream<Map.Entry<String, Record>> s, String newValue, int colIndex);
}
