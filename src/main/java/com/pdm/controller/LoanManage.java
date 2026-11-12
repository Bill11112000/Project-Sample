package com.pdm.controller;

import com.pdm.dao.LoanDAO;
import com.pdm.dao.MemberDAO;
import com.pdm.model.LoanDetailRow;
import com.pdm.model.LoanRow;
import com.pdm.model.Lookup;
import com.pdm.model.OverdueRow;

import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

public class LoanManage extends basecontrol {

    @FXML private TableView<LoanRow> loanTable;
    @FXML private TableColumn<LoanRow, String> colLoanId;
    @FXML private TableColumn<LoanRow, String> colMemberName;
    @FXML private TableColumn<LoanRow, String> colStaffName;
    @FXML private TableColumn<LoanRow, LocalDate> colBorrowDate;
    @FXML private TableColumn<LoanRow, LocalDate> colDueDate;
    @FXML private TableColumn<LoanRow, Integer> colTotalCopies;

    @FXML private TableView<LoanDetailRow> detailTable;
    @FXML private TableColumn<LoanDetailRow, String> colDetailId;
    @FXML private TableColumn<LoanDetailRow, String> colBookId;
    @FXML private TableColumn<LoanDetailRow, String> colBookTitle;
    @FXML private TableColumn<LoanDetailRow, String> colCopyId;
    @FXML private TableColumn<LoanDetailRow, LocalDate> colReturnDate;
    @FXML private TableColumn<LoanDetailRow, Integer> colRenewals;
    @FXML private TableColumn<LoanDetailRow, String> colPaymentStatus;
    @FXML private TableColumn<LoanDetailRow, BigDecimal> colFineAmount;

    @FXML private ComboBox<Lookup> memberBox;
    @FXML private TableView<OverdueRow> overdueTable;
    @FXML private TableColumn<OverdueRow, String> colOverdueLoan;
    @FXML private TableColumn<OverdueRow, LocalDate> colOverdueDue;
    @FXML private TableColumn<OverdueRow, String> colOverdueDetail;
    @FXML private TableColumn<OverdueRow, String> colOverdueBook;
    @FXML private TableColumn<OverdueRow, String> colOverdueCopy;
    @FXML private TableColumn<OverdueRow, Integer> colDaysOverdue;

    @FXML private Label loanCountLabel;
    @FXML private Label detailCountLabel;
    @FXML private Label overdueCountLabel;

    private final LoanDAO loanDAO = new LoanDAO();
    private final MemberDAO memberDAO = new MemberDAO();

    private final ObservableList<LoanRow> loans = FXCollections.observableArrayList();
    private final ObservableList<LoanDetailRow> details = FXCollections.observableArrayList();
    private final ObservableList<OverdueRow> overdue = FXCollections.observableArrayList();
    private final ObservableList<Lookup> members = FXCollections.observableArrayList();

    @FXML
    private void initialize() {
        colLoanId.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().loanId()));
        colMemberName.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().memberName()));
        colStaffName.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().staffName()));
        colBorrowDate.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().borrowDate()));
        colDueDate.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().dueDate()));
        colTotalCopies.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().totalCopies()));

        colDetailId.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().detailId()));
        colBookId.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().bookId()));
        colBookTitle.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().title()));
        colCopyId.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().copyId()));
        colReturnDate.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().returnDate()));
        colRenewals.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().renewalCount()));
        colPaymentStatus.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().paymentStatus()));
        colFineAmount.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().fineAmount()));

        colOverdueLoan.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().loanId()));
        colOverdueDue.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().dueDate()));
        colOverdueDetail.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().detailId()));
        colOverdueBook.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().title()));
        colOverdueCopy.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().copyId()));
        colDaysOverdue.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().daysOverdue()));

        loanTable.setItems(loans);
        detailTable.setItems(details);
        overdueTable.setItems(overdue);
        memberBox.setItems(members);

        loanTable.getSelectionModel().selectedItemProperty()
                .addListener((obs, oldValue, newValue) -> onLoanSelected(newValue));

        loadMembers();
        loadLoans();
        detailCountLabel.setText("Select a loan to see its lines");
    }

    private void loadMembers() {
        try {
            members.setAll(memberDAO.findMembers());
        } catch (SQLException e) {
            showError("Could not load members", e);
        }
    }

    private void loadLoans() {
        try {
            List<LoanRow> rows = loanDAO.findAll();
            loans.setAll(rows);
            loanCountLabel.setText("Showing " + rows.size() + " loans");
        } catch (SQLException e) {
            loans.clear();
            loanCountLabel.setText("Could not load loans");
            showError("Could not load loans", e);
        }
    }

    private void onLoanSelected(LoanRow selected) {
        if (selected == null) {
            details.clear();
            detailCountLabel.setText("Select a loan to see its lines");
            return;
        }

        try {
            List<LoanDetailRow> rows = loanDAO.findDetails(selected.loanId());
            details.setAll(rows);
            detailCountLabel.setText("Showing " + rows.size() + " loan lines");
        } catch (SQLException e) {
            details.clear();
            detailCountLabel.setText("Could not load loan lines");
            showError("Could not load loan lines", e);
        }
    }

    @FXML
    private void refreshLoans(ActionEvent event) {
        LoanRow selected = loanTable.getSelectionModel().getSelectedItem();
        String selectedId = selected == null ? null : selected.loanId();

        loadLoans();

        if (selectedId != null) {
            for (LoanRow row : loans) {
                if (row.loanId().equals(selectedId)) {
                    loanTable.getSelectionModel().select(row);
                    break;
                }
            }
        }
    }

    @FXML
    private void loadOverdue(ActionEvent event) {
        Lookup member = memberBox.getValue();
        if (member == null) {
            showWarning("Choose a member first.");
            return;
        }

        try {
            List<OverdueRow> rows = loanDAO.findOverdueByMember(member.id());
            overdue.setAll(rows);
            overdueCountLabel.setText(rows.isEmpty()
                    ? "No overdue loans for " + member.name()
                    : "Overdue rows: " + rows.size());
        } catch (SQLException e) {
            overdue.clear();
            overdueCountLabel.setText("Could not load overdue loans");
            showError("Could not load overdue loans", e);
        }
    }
}
