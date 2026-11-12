package com.pdm.model;

import java.math.BigDecimal;
import java.time.LocalDate;

public record LoanDetailRow(
        String loanId,
        String detailId,
        String bookId,
        String title,
        String copyId,
        LocalDate returnDate,
        int renewalCount,
        String paymentStatus,
        BigDecimal fineAmount) {
}
