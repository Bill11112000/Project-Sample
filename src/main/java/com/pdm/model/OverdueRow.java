package com.pdm.model;

import java.time.LocalDate;

public record OverdueRow(
        String loanId,
        String memberId,
        String memberName,
        LocalDate dueDate,
        String detailId,
        String bookId,
        String title,
        String copyId,
        int daysOverdue) {
}
