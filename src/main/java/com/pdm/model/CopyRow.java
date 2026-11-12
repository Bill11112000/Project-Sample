package com.pdm.model;

public record CopyRow(
        String bookId,
        String copyId,
        String shelfLocation,
        String status,
        String conditionNotes) {
}
