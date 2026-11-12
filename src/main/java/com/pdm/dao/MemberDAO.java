package com.pdm.dao;

import com.pdm.connect.dbconnect;
import com.pdm.model.Lookup;
import com.pdm.model.MemberRow;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class MemberDAO {

    private static final String FIND_ALL = """
            SELECT m.Member_ID, m.Member_Type_ID, mc.Type_Name, m.Full_Name, m.Email,
                   m.Identity_Card, m.Membership_Expiry_Date, m.Account_Status,
                   (SELECT GROUP_CONCAT(mp.Phone SEPARATOR ', ')
                      FROM Member_Phone mp
                     WHERE mp.Member_ID = m.Member_ID) AS Phones
            FROM Member m
            JOIN Member_Category mc ON mc.Member_Type_ID = m.Member_Type_ID
            ORDER BY m.Member_ID
            """;

    private static final String INSERT = """
            INSERT INTO Member
                (Member_ID, Member_Type_ID, Full_Name, Email, Identity_Card,
                 Membership_Expiry_Date, Account_Status)
            VALUES (?, ?, ?, ?, ?, ?, ?)
            """;

    private static final String UPDATE = """
            UPDATE Member
               SET Member_Type_ID = ?, Full_Name = ?, Email = ?, Identity_Card = ?,
                   Membership_Expiry_Date = ?, Account_Status = ?
             WHERE Member_ID = ?
            """;

    private static final String DELETE = "DELETE FROM Member WHERE Member_ID = ?";
    private static final String DELETE_PHONES = "DELETE FROM Member_Phone WHERE Member_ID = ?";
    private static final String INSERT_PHONE =
            "INSERT INTO Member_Phone (Member_ID, Phone) VALUES (?, ?)";

    public List<MemberRow> findAll() throws SQLException {
        List<MemberRow> rows = new ArrayList<>();

        try (Connection conn = dbconnect.getConnection();
             PreparedStatement ps = conn.prepareStatement(FIND_ALL);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Date expiry = rs.getDate("Membership_Expiry_Date");
                rows.add(new MemberRow(
                        rs.getString("Member_ID"),
                        rs.getString("Member_Type_ID"),
                        rs.getString("Type_Name"),
                        rs.getString("Full_Name"),
                        rs.getString("Email"),
                        rs.getString("Identity_Card"),
                        expiry == null ? null : expiry.toLocalDate(),
                        rs.getString("Account_Status"),
                        rs.getString("Phones")));
            }
        }
        return rows;
    }

    public List<Lookup> findCategories() throws SQLException {
        List<Lookup> rows = new ArrayList<>();
        String sql = "SELECT Member_Type_ID, Type_Name FROM Member_Category ORDER BY Type_Name";

        try (Connection conn = dbconnect.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                rows.add(new Lookup(rs.getString("Member_Type_ID"), rs.getString("Type_Name")));
            }
        }
        return rows;
    }

    public List<Lookup> findMembers() throws SQLException {
        List<Lookup> rows = new ArrayList<>();
        String sql = "SELECT Member_ID, Full_Name FROM Member ORDER BY Full_Name";

        try (Connection conn = dbconnect.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                rows.add(new Lookup(rs.getString("Member_ID"), rs.getString("Full_Name")));
            }
        }
        return rows;
    }

    public void insert(String memberId, String memberTypeId, String fullName, String email,
                       String identityCard, LocalDate expiry, String accountStatus, String phone)
            throws SQLException {

        try (Connection conn = dbconnect.getConnection()) {
            conn.setAutoCommit(false);
            try {
                try (PreparedStatement ps = conn.prepareStatement(INSERT)) {
                    ps.setString(1, memberId);
                    ps.setString(2, memberTypeId);
                    ps.setString(3, fullName);
                    ps.setString(4, email);
                    ps.setString(5, identityCard);
                    ps.setDate(6, Date.valueOf(expiry));
                    ps.setString(7, accountStatus);
                    ps.executeUpdate();
                }
                replacePhones(conn, memberId, phone);
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        }
    }

    public void update(String memberId, String memberTypeId, String fullName, String email,
                       String identityCard, LocalDate expiry, String accountStatus, String phone)
            throws SQLException {

        try (Connection conn = dbconnect.getConnection()) {
            conn.setAutoCommit(false);
            try {
                try (PreparedStatement ps = conn.prepareStatement(UPDATE)) {
                    ps.setString(1, memberTypeId);
                    ps.setString(2, fullName);
                    ps.setString(3, email);
                    ps.setString(4, identityCard);
                    ps.setDate(5, Date.valueOf(expiry));
                    ps.setString(6, accountStatus);
                    ps.setString(7, memberId);
                    ps.executeUpdate();
                }
                replacePhones(conn, memberId, phone);
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        }
    }

    public void delete(String memberId) throws SQLException {
        try (Connection conn = dbconnect.getConnection();
             PreparedStatement ps = conn.prepareStatement(DELETE)) {
            ps.setString(1, memberId);
            ps.executeUpdate();
        }
    }

    private void replacePhones(Connection conn, String memberId, String phone) throws SQLException {
        try (PreparedStatement del = conn.prepareStatement(DELETE_PHONES)) {
            del.setString(1, memberId);
            del.executeUpdate();
        }

        if (phone != null && !phone.isBlank()) {
            try (PreparedStatement ins = conn.prepareStatement(INSERT_PHONE)) {
                ins.setString(1, memberId);
                ins.setString(2, phone.trim());
                ins.executeUpdate();
            }
        }
    }
}
