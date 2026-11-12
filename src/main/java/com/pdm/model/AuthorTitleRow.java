package com.pdm.model;

public record AuthorTitleRow(
        String authorId,
        String authorName,
        String memberId,
        String memberName,
        int borrowedCount,
        int totalTitles) {
}
