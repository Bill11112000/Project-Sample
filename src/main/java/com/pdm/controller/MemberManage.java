package com.pdm.controller;

import com.pdm.dao.MemberDAO;
import com.pdm.model.Lookup;
import com.pdm.model.MemberRow;

import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public class MemberManage extends basecontrol {

    @FXML private TableView<MemberRow> memberTable;
    @FXML private TableColumn<MemberRow, String> colMemberId;
    @FXML private TableColumn<MemberRow, String> colFullName;
    @FXML private TableColumn<MemberRow, String> colTypeName;
    @FXML private TableColumn<MemberRow, String> colEmail;
    @FXML private TableColumn<MemberRow, String> colIdentity;
    @FXML private TableColumn<MemberRow, LocalDate> colExpiry;
    @FXML private TableColumn<MemberRow, String> colAccountStatus;
    @FXML private TableColumn<MemberRow, String> colPhones;

    @FXML private TextField memberIdField;
    @FXML private TextField fullNameField;
    @FXML private TextField emailField;
    @FXML private TextField identityField;
    @FXML private TextField phoneField;
    @FXML private ComboBox<Lookup> categoryBox;
    @FXML private DatePicker expiryPicker;
    @FXML private ComboBox<String> statusBox;

    @FXML private Label countLabel;

    private final MemberDAO memberDAO = new MemberDAO();
    private final ObservableList<MemberRow> members = FXCollections.observableArrayList();
    private final ObservableList<Lookup> categories = FXCollections.observableArrayList();

    @FXML
    private void initialize() {
        colMemberId.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().memberId()));
        colFullName.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().fullName()));
        colTypeName.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().typeName()));
        colEmail.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().email()));
        colIdentity.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().identityCard()));
        colExpiry.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().expiryDate()));
        colAccountStatus.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().accountStatus()));
        colPhones.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().phones()));

        memberTable.setItems(members);
        categoryBox.setItems(categories);
        statusBox.setItems(FXCollections.observableArrayList(
                "Active", "Suspended", "Expired", "Locked"));

        memberTable.getSelectionModel().selectedItemProperty()
                .addListener((obs, oldValue, newValue) -> fillForm(newValue));

        loadCategories();
        loadMembers();
        resetForm();
    }

    private void loadCategories() {
        try {
            categories.setAll(memberDAO.findCategories());
        } catch (SQLException e) {
            showError("Could not load member categories", e);
        }
    }

    private void loadMembers() {
        try {
            List<MemberRow> rows = memberDAO.findAll();
            members.setAll(rows);
            countLabel.setText("Showing " + rows.size() + " members");
        } catch (SQLException e) {
            members.clear();
            countLabel.setText("Could not load members");
            showError("Could not load members", e);
        }
    }

    private void fillForm(MemberRow row) {
        if (row == null) {
            return;
        }
        memberIdField.setText(row.memberId());
        memberIdField.setEditable(false);
        fullNameField.setText(row.fullName());
        emailField.setText(row.email());
        identityField.setText(row.identityCard());
        phoneField.setText(row.phones() == null ? "" : row.phones());
        expiryPicker.setValue(row.expiryDate());
        statusBox.setValue(row.accountStatus());

        for (Lookup item : categories) {
            if (item.id().equals(row.memberTypeId())) {
                categoryBox.setValue(item);
                break;
            }
        }
    }

    private void resetForm() {
        memberTable.getSelectionModel().clearSelection();
        memberIdField.clear();
        memberIdField.setEditable(true);
        fullNameField.clear();
        emailField.clear();
        identityField.clear();
        phoneField.clear();
        expiryPicker.setValue(null);
        categoryBox.setValue(null);
        statusBox.setValue(null);
    }

    private String validateForm() {
        String id = text(memberIdField);
        if (id.isEmpty()) {
            return "Member ID is required.";
        }
        if (id.length() > 20) {
            return "Member ID must be at most 20 characters.";
        }

        String name = text(fullNameField);
        if (name.isEmpty()) {
            return "Full name is required.";
        }
        if (name.length() > 100) {
            return "Full name must be at most 100 characters.";
        }

        String email = text(emailField);
        if (email.isEmpty()) {
            return "Email is required.";
        }
        if (!email.matches("[^@\\s]+@[^@\\s]+\\.[^@\\s]+")) {
            return "Email must look like name@example.com.";
        }
        if (email.length() > 200) {
            return "Email must be at most 200 characters.";
        }

        String identity = text(identityField);
        if (identity.isEmpty()) {
            return "Identity card is required.";
        }
        if (identity.length() < 9 || identity.length() > 20) {
            return "Identity card must be 9 to 20 characters.";
        }

        String phone = text(phoneField);
        if (!phone.isEmpty() && (phone.length() < 8 || phone.length() > 20)) {
            return "Phone must be 8 to 20 characters.";
        }

        if (expiryPicker.getValue() == null) {
            return "Membership expiry date is required.";
        }
        if (categoryBox.getValue() == null) {
            return "Choose a member category.";
        }
        if (statusBox.getValue() == null) {
            return "Choose an account status.";
        }
        return null;
    }

    @FXML
    private void addMember(ActionEvent event) {
        if (memberTable.getSelectionModel().getSelectedItem() != null) {
            showWarning("Click \"Clear form\" before adding a new member.");
            return;
        }

        String error = validateForm();
        if (error != null) {
            showWarning(error);
            return;
        }

        try {
            memberDAO.insert(
                    text(memberIdField),
                    categoryBox.getValue().id(),
                    text(fullNameField),
                    text(emailField),
                    text(identityField),
                    expiryPicker.getValue(),
                    statusBox.getValue(),
                    text(phoneField));

            loadMembers();
            resetForm();
            showInfo("Member added.");
        } catch (SQLException e) {
            showError("Could not add member", e);
        }
    }

    @FXML
    private void updateMember(ActionEvent event) {
        MemberRow selected = memberTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showWarning("Select a member to update.");
            return;
        }

        String error = validateForm();
        if (error != null) {
            showWarning(error);
            return;
        }

        try {
            memberDAO.update(
                    selected.memberId(),
                    categoryBox.getValue().id(),
                    text(fullNameField),
                    text(emailField),
                    text(identityField),
                    expiryPicker.getValue(),
                    statusBox.getValue(),
                    text(phoneField));

            loadMembers();
            showInfo("Member updated.");
        } catch (SQLException e) {
            showError("Could not update member", e);
        }
    }

    @FXML
    private void deleteMember(ActionEvent event) {
        MemberRow selected = memberTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showWarning("Select a member to delete.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Delete member");
        confirm.setHeaderText("Delete " + selected.fullName()
                + " (" + selected.memberId() + ")?");
        confirm.setContentText("This cannot be undone.");
        Optional<ButtonType> answer = confirm.showAndWait();

        if (answer.isEmpty() || answer.get() != ButtonType.OK) {
            return;
        }

        try {
            memberDAO.delete(selected.memberId());
            loadMembers();
            resetForm();
            showInfo("Member deleted.");
        } catch (SQLException e) {
            showError("Could not delete member", e);
        }
    }

    @FXML
    private void refreshMembers(ActionEvent event) {
        loadMembers();
    }

    @FXML
    private void clearForm(ActionEvent event) {
        resetForm();
    }

    private String text(TextField field) {
        return field.getText() == null ? "" : field.getText().trim();
    }
}
