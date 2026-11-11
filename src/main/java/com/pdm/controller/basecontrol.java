package com.pdm.controller;

import java.io.IOException;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

//it is use for menu switch or return home
public class basecontrol {

    @FXML
    private BorderPane mainpane;

    @FXML
    public void homeClick(ActionEvent event) {
        try {

            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/pdm/view/HomeView.fxml"));
            Parent root = fxmlLoader.load();

            Stage stage = (Stage) mainpane.getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setTitle("Library Management");
            stage.setScene(scene);
            stage.show();
            System.out.println("home ok");
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @FXML
    public void bookClick(ActionEvent event) {
        try {

            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/pdm/view/BookView.fxml"));
            Parent root = fxmlLoader.load();

            Stage stage = (Stage) mainpane.getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setTitle("Book Catalog Manage");
            stage.setScene(scene);
            stage.show();
            System.out.println("book ok");
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @FXML
    public void memClick(ActionEvent event) {
        try {

            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/pdm/view/MemberView.fxml"));
            Parent root = fxmlLoader.load();

            Stage stage = (Stage) mainpane.getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setTitle("Member Management");
            stage.setScene(scene);
            stage.show();
            System.out.println("member ok");
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @FXML
    public void loanClick(ActionEvent event) {
        try {

            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/pdm/view/LoanView.fxml"));
            Parent root = fxmlLoader.load();

            Stage stage = (Stage) mainpane.getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setTitle("Loan Management");
            stage.setScene(scene);
            stage.show();
            System.out.println("loans ok");
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @FXML
    public void reserClick(ActionEvent event) {
        try {

            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/pdm/view/ReservationView.fxml"));
            Parent root = fxmlLoader.load();

            Stage stage = (Stage) mainpane.getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setTitle("Reservation Management");
            stage.setScene(scene);
            stage.show();
            System.out.println("reservation ok");
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @FXML
    public void fineClick(ActionEvent event) {
        try {

            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/pdm/view/FineView.fxml"));
            Parent root = fxmlLoader.load();

            Stage stage = (Stage) mainpane.getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setTitle("Fine Management");
            stage.setScene(scene);
            stage.show();
            System.out.println("fines ok");
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @FXML
    public void reportClick(ActionEvent event) {
        try {

            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/pdm/view/ReportView.fxml"));
            Parent root = fxmlLoader.load();

            Stage stage = (Stage) mainpane.getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setTitle("Report");
            stage.setScene(scene);
            stage.show();
            System.out.println("reports ok");
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @FXML
    public void searchClick(ActionEvent event) {
        try {

            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/pdm/view/SearchView.fxml"));
            Parent root = fxmlLoader.load();

            // Stage stage = (Stage) mainpane.getScene().getWindow();
            Stage stage = new Stage();
            Scene scene = new Scene(root);
            stage.setTitle("Search Management");
            stage.setScene(scene);
            stage.show();
            System.out.println("search ok");
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
