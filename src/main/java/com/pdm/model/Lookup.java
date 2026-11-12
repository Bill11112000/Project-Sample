package com.pdm.model;

/** Generic id/name item for ComboBoxes. */
public record Lookup(String id, String name) {

    @Override
    public String toString() {
        return name;
    }
}
