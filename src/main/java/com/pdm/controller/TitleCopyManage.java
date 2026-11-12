package com.pdm.controller;

import com.pdm.dao.BookCopyDAO;
import com.pdm.dao.BookTitleDAO;
import com.pdm.model.BookRow;
import com.pdm.model.CopyRow;

import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

import java.sql.SQLException;
import java.util.List;

public class TitleCopyManage extends basecontrol {

    @FXML private TableView<BookRow> bookTable;
    @FXML private TableColumn<BookRow, String> colBookId;
    @FXML private TableColumn<BookRow, String> colTitle;
    @FXML private TableColumn<BookRow, String> colIsbn;
    @FXML private TableColumn<BookRow, Integer> colYear;
    @FXML private TableColumn<BookRow, String> colCategory;
    @FXML private TableColumn<BookRow, String> colPublisher;
    @FXML private TableColumn<BookRow, String> colAuthors;
    @FXML private TableColumn<BookRow, Integer> colCopies;
    @FXML private TableColumn<BookRow, Integer> colAvailable;

    @FXML private TableView<CopyRow> copyTable;
    @FXML private TableColumn<CopyRow, String> colCopyId;
    @FXML private TableColumn<CopyRow, String> colShelf;
    @FXML private TableColumn<CopyRow, String> colStatus;
    @FXML private TableColumn<CopyRow, String> colCondition;

    @FXML private Label bookCountLabel;
    @FXML private Label copyCountLabel;
    @FXML private Button availableButton;

    private final BookTitleDAO bookDAO = new BookTitleDAO();
    private final BookCopyDAO copyDAO = new BookCopyDAO();

    private final ObservableList<BookRow> books = FXCollections.observableArrayList();
    private final ObservableList<CopyRow> copies = FXCollections.observableArrayList();

    @FXML
    private void initialize() {
        colBookId.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().bookId()));
        colTitle.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().title()));
        colIsbn.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().isbn()));
        colYear.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().publicationYear()));
        colCategory.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().category()));
        colPublisher.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().publisher()));
        colAuthors.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().authors()));
        colCopies.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().totalCopies()));
        colAvailable.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().availableCopies()));

        colCopyId.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().copyId()));
        colShelf.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().shelfLocation()));
        colStatus.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().status()));
        colCondition.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().conditionNotes()));

        bookTable.setItems(books);
        copyTable.setItems(copies);

        bookTable.getSelectionModel().selectedItemProperty()
                .addListener((obs, oldValue, newValue) -> onBookSelected(newValue));

        availableButton.setDisable(true);
        loadBooks();
    }

    private void loadBooks() {
        try {
            List<BookRow> rows = bookDAO.findAll();
            books.setAll(rows);
            bookCountLabel.setText("Showing " + rows.size() + " books");
        } catch (SQLException e) {
            books.clear();
            bookCountLabel.setText("Could not load books");
            showError("Could not load the book catalog", e);
        }
    }

    private void onBookSelected(BookRow selected) {
        availableButton.setDisable(selected == null);

        if (selected == null) {
            copies.clear();
            copyCountLabel.setText("Select a book to see its copies");
            return;
        }

        loadCopies(selected.bookId(), false);
    }

    private void loadCopies(String bookId, boolean onlyAvailable) {
        try {
            List<CopyRow> rows = onlyAvailable
                    ? copyDAO.findAvailable(bookId)
                    : copyDAO.findByBook(bookId);

            copies.setAll(rows);
            copyCountLabel.setText(onlyAvailable
                    ? "Available copies: " + rows.size()
                    : "Showing " + rows.size() + " copies");
        } catch (SQLException e) {
            copies.clear();
            copyCountLabel.setText("Could not load copies");
            showError("Could not load copies", e);
        }
    }

    @FXML
    private void refreshBooks(ActionEvent event) {
        BookRow selected = bookTable.getSelectionModel().getSelectedItem();
        String selectedId = selected == null ? null : selected.bookId();

        loadBooks();

        if (selectedId != null) {
            for (BookRow row : books) {
                if (row.bookId().equals(selectedId)) {
                    bookTable.getSelectionModel().select(row);
                    break;
                }
            }
        }
    }

    @FXML
    private void showAvailableCopies(ActionEvent event) {
        BookRow selected = bookTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showWarning("Select a book first.");
            return;
        }
        loadCopies(selected.bookId(), true);
    }
}
