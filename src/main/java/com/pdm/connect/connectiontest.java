package com.pdm.connect;

import java.sql.Connection;
import java.sql.SQLException;

public class connectiontest {

    public static void main(String[] args) {
        try (Connection conn = dbconnect.getConnection()) {
            System.out.println("Connected to database: " + conn.getCatalog());
        } catch (SQLException e) {
            System.err.println(DbErrors.friendly(e));
        }
    }
}
