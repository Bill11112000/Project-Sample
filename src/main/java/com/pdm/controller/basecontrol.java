package com.pdm.controller;

import java.io.IOException;
import java.sql.SQLException;

import com.pdm.connect.DbErrors;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

/** Shared navigation and message helpers for every screen. */
public class basecontrol {

    private static final String VIEW_PATH = "/com/pdm/view/%s.fxml";

    @FXML
    private BorderPane mainpane;

    @FXML
    public void homeClick(ActionEvent event) {
        openView("HomeView", "Library Management");
    }

    @FXML
    public void bookClick(ActionEvent event) {
        openView("BookView", "Book Catalog Manage");
    }

    @FXML
    public void memClick(ActionEvent event) {
        openView("MemberView", "Member Management");
    }

    @FXML
    public void loanClick(ActionEvent event) {
        openView("LoanView", "Loan Management");
    }

    @FXML
    public void reserClick(ActionEvent event) {
        openView("ReservationView", "Reservation Management");
    }

    @FXML
    public void fineClick(ActionEvent event) {
        openView("FineView", "Fine Management");
    }

    @FXML
    public void reportClick(ActionEvent event) {
        openView("ReportView", "Report");
    }

    @FXML
    public void searchClick(ActionEvent event) {
        try {
            Parent root = loadView("SearchView");
            Stage stage = new Stage();
            stage.initOwner(mainpane.getScene().getWindow());
            stage.setTitle("Search Management");
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            showLoadError(e);
        }
    }

    /** Replaces the content of the current window with the requested view. */
    private void openView(String view, String title) {
        try {
            Parent root = loadView(view);
            Stage stage = (Stage) mainpane.getScene().getWindow();
            stage.setTitle(title);
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            showLoadError(e);
        }
    }

    private Parent loadView(String view) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource(VIEW_PATH.formatted(view)));
        return loader.load();
    }

    // ---- Shared message helpers for every screen ----

    protected void showInfo(String message) {
        showAlert(Alert.AlertType.INFORMATION, "Information", null, message);
    }

    protected void showWarning(String message) {
        showAlert(Alert.AlertType.WARNING, "Check your input", null, message);
    }

    protected void showError(String header, SQLException e) {
        showAlert(Alert.AlertType.ERROR, "Database error", header, DbErrors.friendly(e));
    }

    private void showLoadError(IOException e) {
        showAlert(Alert.AlertType.ERROR, "Navigation error",
                "Could not open the screen",
                e.getMessage() == null ? e.toString() : e.getMessage());
    }

    private void showAlert(Alert.AlertType type, String title, String header, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
