package com.pdm.dao;

import com.pdm.connect.dbconnect;
import com.pdm.model.LoanDetailRow;
import com.pdm.model.LoanRow;
import com.pdm.model.OverdueRow;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class LoanDAO {

    private static final String FIND_ALL = """
            SELECT lt.Loan_ID, lt.Member_ID, m.Full_Name, lt.Staff_ID,
                   s.Full_Name AS Staff_Name, lt.Borrow_Date, lt.Due_Date,
                   lt.Total_Copies_Borrowed
            FROM Loan_Transaction lt
            JOIN Member m ON m.Member_ID = lt.Member_ID
            JOIN Staff s  ON s.Staff_ID  = lt.Staff_ID
            ORDER BY lt.Borrow_Date DESC, lt.Loan_ID
            """;

    private static final String FIND_DETAILS = """
            SELECT ld.Loan_ID, ld.Detail_ID, ld.Book_ID, b.Title, ld.Copy_ID,
                   ld.Return_Date, ld.Renewal_Count, ld.Payment_Status, ld.Fine_Amount
            FROM Loan_Detail ld
            JOIN Book b ON b.Book_ID = ld.Book_ID
            WHERE ld.Loan_ID = ?
            ORDER BY ld.Detail_ID
            """;

    // Q2: which loans of a specific member are overdue today?
    private static final String OVERDUE_BY_MEMBER = """
            SELECT lt.Loan_ID, lt.Member_ID, m.Full_Name, lt.Due_Date,
                   ld.Detail_ID, ld.Book_ID, b.Title, ld.Copy_ID,
                   DATEDIFF(CURDATE(), lt.Due_Date) AS Days_Overdue
            FROM Loan_Transaction lt
            JOIN Member m      ON m.Member_ID = lt.Member_ID
            JOIN Loan_Detail ld ON ld.Loan_ID = lt.Loan_ID
            JOIN Book b        ON b.Book_ID = ld.Book_ID
            WHERE lt.Member_ID = ?
              AND ld.Return_Date IS NULL
              AND lt.Due_Date < CURDATE()
            ORDER BY lt.Due_Date, ld.Detail_ID
            """;

    public List<LoanRow> findAll() throws SQLException {
        List<LoanRow> rows = new ArrayList<>();

        try (Connection conn = dbconnect.getConnection();
             PreparedStatement ps = conn.prepareStatement(FIND_ALL);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Date borrow = rs.getDate("Borrow_Date");
                Date due = rs.getDate("Due_Date");
                rows.add(new LoanRow(
                        rs.getString("Loan_ID"),
                        rs.getString("Member_ID"),
                        rs.getString("Full_Name"),
                        rs.getString("Staff_ID"),
                        rs.getString("Staff_Name"),
                        borrow == null ? null : borrow.toLocalDate(),
                        due == null ? null : due.toLocalDate(),
                        rs.getInt("Total_Copies_Borrowed")));
            }
        }
        return rows;
    }

    public List<LoanDetailRow> findDetails(String loanId) throws SQLException {
        List<LoanDetailRow> rows = new ArrayList<>();

        try (Connection conn = dbconnect.getConnection();
             PreparedStatement ps = conn.prepareStatement(FIND_DETAILS)) {

            ps.setString(1, loanId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Date returned = rs.getDate("Return_Date");
                    BigDecimal fine = rs.getBigDecimal("Fine_Amount");
                    rows.add(new LoanDetailRow(
                            rs.getString("Loan_ID"),
                            rs.getString("Detail_ID"),
                            rs.getString("Book_ID"),
                            rs.getString("Title"),
                            rs.getString("Copy_ID"),
                            returned == null ? null : returned.toLocalDate(),
                            rs.getInt("Renewal_Count"),
                            rs.getString("Payment_Status"),
                            fine));
                }
            }
        }
        return rows;
    }

    public List<OverdueRow> findOverdueByMember(String memberId) throws SQLException {
        List<OverdueRow> rows = new ArrayList<>();

        try (Connection conn = dbconnect.getConnection();
             PreparedStatement ps = conn.prepareStatement(OVERDUE_BY_MEMBER)) {

            ps.setString(1, memberId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Date due = rs.getDate("Due_Date");
                    rows.add(new OverdueRow(
                            rs.getString("Loan_ID"),
                            rs.getString("Member_ID"),
                            rs.getString("Full_Name"),
                            due == null ? null : due.toLocalDate(),
                            rs.getString("Detail_ID"),
                            rs.getString("Book_ID"),
                            rs.getString("Title"),
                            rs.getString("Copy_ID"),
                            rs.getInt("Days_Overdue")));
                }
            }
        }
        return rows;
    }
}
