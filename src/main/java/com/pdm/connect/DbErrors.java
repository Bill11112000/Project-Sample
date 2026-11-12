package com.pdm.connect;

import java.sql.SQLException;

/** Turns JDBC/MySQL errors into messages a user can understand. */
public final class DbErrors {

    private DbErrors() {
    }

    public static String friendly(SQLException e) {
        SQLException root = e;
        while (root.getNextException() != null) {
            root = root.getNextException();
        }

        int code = root.getErrorCode();
        String state = root.getSQLState();

        return switch (code) {
            case 1045 -> "Access denied. Check db.user and db.password in db.properties.";
            case 1049 -> "The database does not exist. Import database.sql first.";
            case 1062 -> "That value already exists. IDs, ISBNs, emails and identity cards must be unique.";
            case 1451 -> "This record is still used by other records, so it cannot be deleted.";
            case 1452 -> "A referenced record does not exist. Choose a valid category, publisher or member.";
            case 1406 -> "One of the values is too long for its column.";
            case 1264 -> "One of the numbers is outside the allowed range.";
            case 1048 -> "A required field is missing.";
            default -> {
                if ("45000".equals(state)) {
                    yield root.getMessage();
                }
                if ("23000".equals(state)) {
                    yield "The change breaks a database rule: " + root.getMessage();
                }
                if (code == 0) {
                    yield "Cannot reach the database. Check db.properties and that MySQL is running.\n"
                            + root.getMessage();
                }
                yield "Database error: " + root.getMessage();
            }
        };
    }
}
