package com.pdm.controller;

import com.pdm.dao.ReservationDAO;
import com.pdm.model.ReservationRow;

import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

public class ReservationManage extends basecontrol {

    @FXML private TableView<ReservationRow> reservationTable;
    @FXML private TableColumn<ReservationRow, String> colReservationId;
    @FXML private TableColumn<ReservationRow, String> colMemberId;
    @FXML private TableColumn<ReservationRow, String> colMemberName;
    @FXML private TableColumn<ReservationRow, String> colBookId;
    @FXML private TableColumn<ReservationRow, String> colBookTitle;
    @FXML private TableColumn<ReservationRow, LocalDate> colReservationDate;
    @FXML private TableColumn<ReservationRow, String> colStatus;
    @FXML private Label countLabel;

    private final ReservationDAO reservationDAO = new ReservationDAO();
    private final ObservableList<ReservationRow> reservations = FXCollections.observableArrayList();

    @FXML
    private void initialize() {
        colReservationId.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().reservationId()));
        colMemberId.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().memberId()));
        colMemberName.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().memberName()));
        colBookId.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().bookId()));
        colBookTitle.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().title()));
        colReservationDate.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().reservationDate()));
        colStatus.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().status()));

        reservationTable.setItems(reservations);
        loadReservations();
    }

    private void loadReservations() {
        try {
            List<ReservationRow> rows = reservationDAO.findAll();
            reservations.setAll(rows);
            countLabel.setText("Showing " + rows.size() + " reservations");
        } catch (SQLException e) {
            reservations.clear();
            countLabel.setText("Could not load reservations");
            showError("Could not load reservations", e);
        }
    }

    @FXML
    private void refreshReservations(ActionEvent event) {
        loadReservations();
    }
}
