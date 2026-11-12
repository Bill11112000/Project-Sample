package com.pdm.dao;

import com.pdm.connect.dbconnect;
import com.pdm.model.FineRow;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class FineDAO {

    private static final String FIND_ALL = """
            SELECT ld.Loan_ID, ld.Detail_ID, m.Member_ID, m.Full_Name,
                   ld.Book_ID, b.Title, lt.Due_Date, ld.Return_Date,
                   ld.Fine_Amount, ld.Payment_Status
            FROM Loan_Detail ld
            JOIN Loan_Transaction lt ON lt.Loan_ID = ld.Loan_ID
            JOIN Member m            ON m.Member_ID = lt.Member_ID
            JOIN Book b              ON b.Book_ID = ld.Book_ID
            WHERE ld.Fine_Amount > 0
            ORDER BY ld.Payment_Status, lt.Due_Date, ld.Loan_ID
            """;

    private static final String UPDATE_STATUS = """
            UPDATE Loan_Detail
               SET Payment_Status = ?
             WHERE Loan_ID = ? AND Detail_ID = ?
            """;

    public List<FineRow> findAll() throws SQLException {
        List<FineRow> rows = new ArrayList<>();

        try (Connection conn = dbconnect.getConnection();
             PreparedStatement ps = conn.prepareStatement(FIND_ALL);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Date due = rs.getDate("Due_Date");
                Date returned = rs.getDate("Return_Date");
                BigDecimal fine = rs.getBigDecimal("Fine_Amount");
                rows.add(new FineRow(
                        rs.getString("Loan_ID"),
                        rs.getString("Detail_ID"),
                        rs.getString("Member_ID"),
                        rs.getString("Full_Name"),
                        rs.getString("Book_ID"),
                        rs.getString("Title"),
                        due == null ? null : due.toLocalDate(),
                        returned == null ? null : returned.toLocalDate(),
                        fine,
                        rs.getString("Payment_Status")));
            }
        }
        return rows;
    }

    public int updatePaymentStatus(String loanId, String detailId, String newStatus)
            throws SQLException {

        try (Connection conn = dbconnect.getConnection();
             PreparedStatement ps = conn.prepareStatement(UPDATE_STATUS)) {

            ps.setString(1, newStatus);
            ps.setString(2, loanId);
            ps.setString(3, detailId);
            return ps.executeUpdate();
        }
    }
}
