package com.pdm.dao;

import com.pdm.connect.dbconnect;
import com.pdm.model.BookRow;
import com.pdm.model.Lookup;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class BookTitleDAO {

    private static final String BASE_SELECT = """
            SELECT b.Book_ID,
                   b.Title,
                   b.ISBN,
                   b.Publication_Year,
                   c.Category_Name,
                   p.Publisher_Name,
                   (SELECT GROUP_CONCAT(a.Author_Name ORDER BY ba.Author_Order SEPARATOR ', ')
                      FROM Book_Author ba
                      JOIN Author a ON a.Author_ID = ba.Author_ID
                     WHERE ba.Book_ID = b.Book_ID) AS Authors,
                   (SELECT COUNT(*)
                      FROM Book_Copy bc
                     WHERE bc.Book_ID = b.Book_ID) AS Total_Copies,
                   (SELECT COUNT(*)
                      FROM Book_Copy bc
                     WHERE bc.Book_ID = b.Book_ID
                       AND bc.Status = 'Available') AS Available_Copies
            FROM Book b
            JOIN Category c  ON c.Category_ID  = b.Category_ID
            JOIN Publisher p ON p.Publisher_ID = b.Publisher_ID
            WHERE 1 = 1
            """;

    public List<BookRow> findAll() throws SQLException {
        return query(BASE_SELECT + " ORDER BY b.Book_ID", List.of());
    }

    /**
     * Parameterised book search. Blank criteria are ignored.
     * Every user value is bound with ?, never concatenated into the SQL.
     */
    public List<BookRow> search(String title, String author, String category, String status)
            throws SQLException {

        StringBuilder sql = new StringBuilder(BASE_SELECT);
        List<String> params = new ArrayList<>();

        if (title != null && !title.isBlank()) {
            sql.append(" AND b.Title LIKE ?");
            params.add("%" + title.trim() + "%");
        }
        if (author != null && !author.isBlank()) {
            sql.append("""
                     AND EXISTS (SELECT 1
                                   FROM Book_Author ba
                                   JOIN Author a ON a.Author_ID = ba.Author_ID
                                  WHERE ba.Book_ID = b.Book_ID
                                    AND a.Author_Name LIKE ?)
                    """);
            params.add("%" + author.trim() + "%");
        }
        if (category != null && !category.isBlank()) {
            sql.append(" AND c.Category_Name = ?");
            params.add(category);
        }
        if (status != null && !status.isBlank()) {
            sql.append("""
                     AND EXISTS (SELECT 1
                                   FROM Book_Copy bc
                                  WHERE bc.Book_ID = b.Book_ID
                                    AND bc.Status = ?)
                    """);
            params.add(status);
        }

        sql.append(" ORDER BY b.Title");
        return query(sql.toString(), params);
    }

    public List<Lookup> findCategories() throws SQLException {
        List<Lookup> rows = new ArrayList<>();
        String sql = "SELECT Category_ID, Category_Name FROM Category ORDER BY Category_Name";

        try (Connection conn = dbconnect.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                rows.add(new Lookup(rs.getString("Category_ID"), rs.getString("Category_Name")));
            }
        }
        return rows;
    }

    private List<BookRow> query(String sql, List<String> params) throws SQLException {
        List<BookRow> rows = new ArrayList<>();

        try (Connection conn = dbconnect.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            for (int i = 0; i < params.size(); i++) {
                ps.setString(i + 1, params.get(i));
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    rows.add(map(rs));
                }
            }
        }
        return rows;
    }

    private BookRow map(ResultSet rs) throws SQLException {
        return new BookRow(
                rs.getString("Book_ID"),
                rs.getString("Title"),
                rs.getString("ISBN"),
                rs.getInt("Publication_Year"),
                rs.getString("Category_Name"),
                rs.getString("Publisher_Name"),
                rs.getString("Authors"),
                rs.getInt("Total_Copies"),
                rs.getInt("Available_Copies"));
    }
}
