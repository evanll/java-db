package com.javadb;

import java.util.Map;
import java.util.stream.Stream;

/**
 * An interface that helps handle column constraints.
 */
public interface Violates {
    boolean isViolated(Stream<Map.Entry<Integer, Record>> s, Record newRecord, int colIndex);
}
