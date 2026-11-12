package com.pdm.dao;

import com.pdm.connect.dbconnect;
import com.pdm.model.CopyRow;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class BookCopyDAO {

    private static final String BY_BOOK = """
            SELECT Book_ID, Copy_ID, Shelf_Location, Status, Condition_Notes
            FROM Book_Copy
            WHERE Book_ID = ?
            ORDER BY Copy_ID
            """;

    private static final String AVAILABLE_BY_BOOK = """
            SELECT Book_ID, Copy_ID, Shelf_Location, Status, Condition_Notes
            FROM Book_Copy
            WHERE Book_ID = ?
              AND Status = 'Available'
            ORDER BY Copy_ID
            """;

    public List<CopyRow> findByBook(String bookId) throws SQLException {
        return query(BY_BOOK, bookId);
    }

    // Q1: which copies of a specific title are currently available?
    public List<CopyRow> findAvailable(String bookId) throws SQLException {
        return query(AVAILABLE_BY_BOOK, bookId);
    }

    private List<CopyRow> query(String sql, String bookId) throws SQLException {
        List<CopyRow> rows = new ArrayList<>();

        try (Connection conn = dbconnect.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, bookId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    rows.add(new CopyRow(
                            rs.getString("Book_ID"),
                            rs.getString("Copy_ID"),
                            rs.getString("Shelf_Location"),
                            rs.getString("Status"),
                            rs.getString("Condition_Notes")));
                }
            }
        }
        return rows;
    }
}
