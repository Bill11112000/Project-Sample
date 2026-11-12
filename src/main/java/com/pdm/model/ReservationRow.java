package com.pdm.model;

import java.time.LocalDate;

public record ReservationRow(
        String reservationId,
        String memberId,
        String memberName,
        String bookId,
        String title,
        LocalDate reservationDate,
        String status) {
}
