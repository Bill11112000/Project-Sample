package com.pdm.model;

import java.math.BigDecimal;

public record CategoryFineRow(
        String memberTypeId,
        String typeName,
        int membersWithFines,
        BigDecimal totalOutstanding) {
}
