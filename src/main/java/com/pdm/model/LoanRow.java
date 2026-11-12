package com.pdm.model;

import java.time.LocalDate;

public record LoanRow(
        String loanId,
        String memberId,
        String memberName,
        String staffId,
        String staffName,
        LocalDate borrowDate,
        LocalDate dueDate,
        int totalCopies) {
}
