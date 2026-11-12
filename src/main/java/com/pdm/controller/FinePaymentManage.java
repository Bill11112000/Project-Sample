package com.pdm.controller;

import com.pdm.dao.FineDAO;
import com.pdm.model.FineRow;

import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public class FinePaymentManage extends basecontrol {

    @FXML private TableView<FineRow> fineTable;
    @FXML private TableColumn<FineRow, String> colLoanId;
    @FXML private TableColumn<FineRow, String> colDetailId;
    @FXML private TableColumn<FineRow, String> colMemberId;
    @FXML private TableColumn<FineRow, String> colMemberName;
    @FXML private TableColumn<FineRow, String> colBookTitle;
    @FXML private TableColumn<FineRow, LocalDate> colDueDate;
    @FXML private TableColumn<FineRow, LocalDate> colReturnDate;
    @FXML private TableColumn<FineRow, BigDecimal> colFineAmount;
    @FXML private TableColumn<FineRow, String> colPaymentStatus;
    @FXML private Label countLabel;

    private final FineDAO fineDAO = new FineDAO();
    private final ObservableList<FineRow> fines = FXCollections.observableArrayList();

    @FXML
    private void initialize() {
        colLoanId.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().loanId()));
        colDetailId.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().detailId()));
        colMemberId.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().memberId()));
        colMemberName.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().memberName()));
        colBookTitle.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().title()));
        colDueDate.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().dueDate()));
        colReturnDate.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().returnDate()));
        colFineAmount.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().fineAmount()));
        colPaymentStatus.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().paymentStatus()));

        fineTable.setItems(fines);
        loadFines();
    }

    private void loadFines() {
        try {
            List<FineRow> rows = fineDAO.findAll();
            fines.setAll(rows);
            countLabel.setText("Showing " + rows.size() + " fines");
        } catch (SQLException e) {
            fines.clear();
            countLabel.setText("Could not load fines");
            showError("Could not load fines", e);
        }
    }

    @FXML
    private void refreshFines(ActionEvent event) {
        loadFines();
    }

    @FXML
    private void markPaid(ActionEvent event) {
        updateSelected("Paid");
    }

    @FXML
    private void waiveFine(ActionEvent event) {
        updateSelected("Waived");
    }

    private void updateSelected(String newStatus) {
        FineRow selected = fineTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showWarning("Select a fine first.");
            return;
        }
        if (!"Unpaid".equals(selected.paymentStatus())) {
            showWarning("Only an Unpaid fine can be changed. This one is "
                    + selected.paymentStatus() + ".");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Update fine");
        confirm.setHeaderText("Mark this fine as " + newStatus + "?");
        confirm.setContentText(selected.memberName() + " - " + selected.title()
                + " - " + selected.fineAmount());
        Optional<ButtonType> answer = confirm.showAndWait();

        if (answer.isEmpty() || answer.get() != ButtonType.OK) {
            return;
        }

        try {
            int updated = fineDAO.updatePaymentStatus(
                    selected.loanId(), selected.detailId(), newStatus);

            if (updated == 0) {
                showWarning("The fine no longer exists.");
            } else {
                showInfo("Fine marked as " + newStatus + ".");
            }
            loadFines();
        } catch (SQLException e) {
            showError("Could not update the fine", e);
        }
    }
}
