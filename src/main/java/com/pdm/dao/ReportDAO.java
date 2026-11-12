package com.pdm.dao;

import com.pdm.connect.dbconnect;
import com.pdm.model.AuthorTitleRow;
import com.pdm.model.CategoryFineRow;
import com.pdm.model.Lookup;
import com.pdm.model.TopTitleRow;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ReportDAO {

    // Q3: top 10 most borrowed titles in a given year.
    private static final String TOP_TITLES = """
            SELECT b.Book_ID, b.Title, COUNT(*) AS Times_Borrowed
            FROM Loan_Transaction lt
            JOIN Loan_Detail ld ON ld.Loan_ID = lt.Loan_ID
            JOIN Book b         ON b.Book_ID = ld.Book_ID
            WHERE YEAR(lt.Borrow_Date) = ?
            GROUP BY b.Book_ID, b.Title
            ORDER BY Times_Borrowed DESC, b.Title
            LIMIT 10
            """;

    // Q5: total fines outstanding per member category.
    private static final String FINES_BY_CATEGORY = """
            SELECT mc.Member_Type_ID, mc.Type_Name,
                   COUNT(DISTINCT CASE
                       WHEN ld.Payment_Status = 'Unpaid' AND ld.Fine_Amount > 0
                       THEN m.Member_ID END) AS Members_With_Fines,
                   COALESCE(SUM(CASE
                       WHEN ld.Payment_Status = 'Unpaid' THEN ld.Fine_Amount
                       ELSE 0 END), 0) AS Total_Outstanding
            FROM Member_Category mc
            LEFT JOIN Member m          ON m.Member_Type_ID = mc.Member_Type_ID
            LEFT JOIN Loan_Transaction lt ON lt.Member_ID = m.Member_ID
            LEFT JOIN Loan_Detail ld    ON ld.Loan_ID = lt.Loan_ID
            GROUP BY mc.Member_Type_ID, mc.Type_Name
            ORDER BY Total_Outstanding DESC, mc.Type_Name
            """;

    // Q4: members who have borrowed every title written by a given author.
    private static final String BORROWED_ALL_BY_AUTHOR = """
            SELECT a.Author_ID, a.Author_Name, m.Member_ID, m.Full_Name,
                   (SELECT COUNT(DISTINCT ld.Book_ID)
                      FROM Loan_Transaction lt
                      JOIN Loan_Detail ld ON ld.Loan_ID = lt.Loan_ID
                     WHERE lt.Member_ID = m.Member_ID
                       AND ld.Book_ID IN (SELECT ba.Book_ID
                                            FROM Book_Author ba
                                           WHERE ba.Author_ID = a.Author_ID)) AS Borrowed_Count,
                   (SELECT COUNT(*)
                      FROM Book_Author ba
                     WHERE ba.Author_ID = a.Author_ID) AS Total_Titles
            FROM Author a
            CROSS JOIN Member m
            WHERE a.Author_ID = ?
              AND EXISTS (SELECT 1 FROM Book_Author ba WHERE ba.Author_ID = a.Author_ID)
              AND NOT EXISTS (
                    SELECT 1
                    FROM Book_Author ba
                    WHERE ba.Author_ID = a.Author_ID
                      AND NOT EXISTS (
                            SELECT 1
                            FROM Loan_Transaction lt
                            JOIN Loan_Detail ld ON ld.Loan_ID = lt.Loan_ID
                            WHERE lt.Member_ID = m.Member_ID
                              AND ld.Book_ID = ba.Book_ID
                      )
              )
            ORDER BY m.Full_Name
            """;

    public List<TopTitleRow> topTitlesThisYear(int year) throws SQLException {
        List<TopTitleRow> rows = new ArrayList<>();

        try (Connection conn = dbconnect.getConnection();
             PreparedStatement ps = conn.prepareStatement(TOP_TITLES)) {

            ps.setInt(1, year);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    rows.add(new TopTitleRow(
                            rs.getString("Book_ID"),
                            rs.getString("Title"),
                            rs.getInt("Times_Borrowed")));
                }
            }
        }
        return rows;
    }

    public List<CategoryFineRow> outstandingFinesByCategory() throws SQLException {
        List<CategoryFineRow> rows = new ArrayList<>();

        try (Connection conn = dbconnect.getConnection();
             PreparedStatement ps = conn.prepareStatement(FINES_BY_CATEGORY);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                BigDecimal total = rs.getBigDecimal("Total_Outstanding");
                rows.add(new CategoryFineRow(
                        rs.getString("Member_Type_ID"),
                        rs.getString("Type_Name"),
                        rs.getInt("Members_With_Fines"),
                        total == null ? BigDecimal.ZERO : total));
            }
        }
        return rows;
    }

    public List<AuthorTitleRow> membersWhoBorrowedAllByAuthor(String authorId) throws SQLException {
        List<AuthorTitleRow> rows = new ArrayList<>();

        try (Connection conn = dbconnect.getConnection();
             PreparedStatement ps = conn.prepareStatement(BORROWED_ALL_BY_AUTHOR)) {

            ps.setString(1, authorId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    rows.add(new AuthorTitleRow(
                            rs.getString("Author_ID"),
                            rs.getString("Author_Name"),
                            rs.getString("Member_ID"),
                            rs.getString("Full_Name"),
                            rs.getInt("Borrowed_Count"),
                            rs.getInt("Total_Titles")));
                }
            }
        }
        return rows;
    }

    public List<Lookup> findAuthors() throws SQLException {
        List<Lookup> rows = new ArrayList<>();
        String sql = "SELECT Author_ID, Author_Name FROM Author ORDER BY Author_Name";

        try (Connection conn = dbconnect.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                rows.add(new Lookup(rs.getString("Author_ID"), rs.getString("Author_Name")));
            }
        }
        return rows;
    }
}
