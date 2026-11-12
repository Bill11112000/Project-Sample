package com.pdm.model;

import java.time.LocalDate;

public record MemberRow(
        String memberId,
        String memberTypeId,
        String typeName,
        String fullName,
        String email,
        String identityCard,
        LocalDate expiryDate,
        String accountStatus,
        String phones) {
}
