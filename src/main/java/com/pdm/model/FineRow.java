package com.pdm.model;

import java.math.BigDecimal;
import java.time.LocalDate;

public record FineRow(
        String loanId,
        String detailId,
        String memberId,
        String memberName,
        String bookId,
        String title,
        LocalDate dueDate,
        LocalDate returnDate,
        BigDecimal fineAmount,
        String paymentStatus) {
}
