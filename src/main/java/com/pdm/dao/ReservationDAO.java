package com.pdm.dao;

import com.pdm.connect.dbconnect;
import com.pdm.model.ReservationRow;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ReservationDAO {

    private static final String FIND_ALL = """
            SELECT r.Reservation_ID, r.Member_ID, m.Full_Name, r.Book_ID, b.Title,
                   r.Reservation_Date, r.Status
            FROM Reservation r
            JOIN Member m ON m.Member_ID = r.Member_ID
            JOIN Book b   ON b.Book_ID   = r.Book_ID
            ORDER BY r.Reservation_Date DESC, r.Reservation_ID
            """;

    public List<ReservationRow> findAll() throws SQLException {
        List<ReservationRow> rows = new ArrayList<>();

        try (Connection conn = dbconnect.getConnection();
             PreparedStatement ps = conn.prepareStatement(FIND_ALL);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Date date = rs.getDate("Reservation_Date");
                rows.add(new ReservationRow(
                        rs.getString("Reservation_ID"),
                        rs.getString("Member_ID"),
                        rs.getString("Full_Name"),
                        rs.getString("Book_ID"),
                        rs.getString("Title"),
                        date == null ? null : date.toLocalDate(),
                        rs.getString("Status")));
            }
        }
        return rows;
    }
}
