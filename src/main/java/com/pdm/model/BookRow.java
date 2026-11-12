package com.pdm.model;

public record BookRow(
        String bookId,
        String title,
        String isbn,
        int publicationYear,
        String category,
        String publisher,
        String authors,
        int totalCopies,
        int availableCopies) {
}
