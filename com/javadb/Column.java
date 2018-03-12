package com.javadb;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.Set;

/**
 * A column in a table.
 * Currently holds only it's name but later can be used to add Constraints, Data Types.
 */
public class Column {
    private String name;
    private final EnumSet<Constraint> constraints;
    private final boolean isPK;

    Column(String name, Constraint... constraints) {
        this.name = name;
        this.constraints = addConstraints(constraints);
        isPK = this.constraints.contains(Constraint.PRIMARY_KEY);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public EnumSet<Constraint> getConstraints() {
        return constraints;
    }

    /**
     * Constructs a set of constrains.
     * @param constraints
     * @return A constraint EnumSet.
     */
    private EnumSet<Constraint> addConstraints(Constraint... constraints) {
        EnumSet<Constraint> constraintSet = EnumSet.noneOf(Constraint.class);

        for(Constraint constraint : constraints) {
                constraintSet.add(constraint);
        }

        return constraintSet;
    }

    @Override
    public String toString() {
        return name;
    }
}
