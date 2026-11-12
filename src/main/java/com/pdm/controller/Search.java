package com.pdm.controller;

import com.pdm.dao.BookTitleDAO;
import com.pdm.dao.ReportDAO;
import com.pdm.model.AuthorTitleRow;
import com.pdm.model.BookRow;
import com.pdm.model.Lookup;

import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;

import java.sql.SQLException;
import java.util.List;

public class Search extends basecontrol {

    @FXML private TextField titleField;
    @FXML private TextField authorField;
    @FXML private ComboBox<Lookup> categoryBox;
    @FXML private ComboBox<String> statusBox;
    @FXML private TableView<BookRow> resultTable;
    @FXML private TableColumn<BookRow, String> colBookId;
    @FXML private TableColumn<BookRow, String> colTitle;
    @FXML private TableColumn<BookRow, String> colIsbn;
    @FXML private TableColumn<BookRow, Integer> colYear;
    @FXML private TableColumn<BookRow, String> colCategory;
    @FXML private TableColumn<BookRow, String> colPublisher;
    @FXML private TableColumn<BookRow, String> colAuthors;
    @FXML private TableColumn<BookRow, Integer> colCopies;
    @FXML private TableColumn<BookRow, Integer> colAvailable;
    @FXML private Label resultCountLabel;

    @FXML private ComboBox<Lookup> q4AuthorBox;
    @FXML private TableView<AuthorTitleRow> q4Table;
    @FXML private TableColumn<AuthorTitleRow, String> colQ4Author;
    @FXML private TableColumn<AuthorTitleRow, String> colQ4MemberId;
    @FXML private TableColumn<AuthorTitleRow, String> colQ4MemberName;
    @FXML private TableColumn<AuthorTitleRow, Integer> colQ4Borrowed;
    @FXML private TableColumn<AuthorTitleRow, Integer> colQ4Total;
    @FXML private Label q4CountLabel;

    private final BookTitleDAO bookDAO = new BookTitleDAO();
    private final ReportDAO reportDAO = new ReportDAO();

    private final ObservableList<BookRow> results = FXCollections.observableArrayList();
    private final ObservableList<AuthorTitleRow> q4Rows = FXCollections.observableArrayList();
    private final ObservableList<Lookup> categories = FXCollections.observableArrayList();
    private final ObservableList<Lookup> authors = FXCollections.observableArrayList();

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

        colQ4Author.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().authorName()));
        colQ4MemberId.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().memberId()));
        colQ4MemberName.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().memberName()));
        colQ4Borrowed.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().borrowedCount()));
        colQ4Total.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().totalTitles()));

        resultTable.setItems(results);
        q4Table.setItems(q4Rows);
        categoryBox.setItems(categories);
        q4AuthorBox.setItems(authors);

        statusBox.setItems(FXCollections.observableArrayList(
                "Any", "Available", "Borrowed", "Reserved", "Damaged", "Lost"));
        statusBox.setValue("Any");

        loadCategories();
        loadAuthors();
        runSearch(null);
    }

    private void loadCategories() {
        try {
            categories.setAll(bookDAO.findCategories());
            categories.add(0, new Lookup("", "Any category"));
            categoryBox.setValue(categories.get(0));
        } catch (SQLException e) {
            showError("Could not load categories", e);
        }
    }

    private void loadAuthors() {
        try {
            authors.setAll(reportDAO.findAuthors());
        } catch (SQLException e) {
            showError("Could not load authors", e);
        }
    }

    @FXML
    private void runSearch(ActionEvent event) {
        Lookup category = categoryBox.getValue();
        String categoryName = (category == null || category.id().isEmpty()) ? null : category.name();

        String status = statusBox.getValue();
        if ("Any".equals(status)) {
            status = null;
        }

        try {
            List<BookRow> rows = bookDAO.search(
                    titleField.getText(),
                    authorField.getText(),
                    categoryName,
                    status);

            results.setAll(rows);
            resultCountLabel.setText("Showing " + rows.size() + " matching books");
        } catch (SQLException e) {
            results.clear();
            resultCountLabel.setText("Search failed");
            showError("Could not search books", e);
        }
    }

    @FXML
    private void clearSearch(ActionEvent event) {
        titleField.clear();
        authorField.clear();
        categoryBox.setValue(categories.isEmpty() ? null : categories.get(0));
        statusBox.setValue("Any");
        runSearch(null);
    }

    @FXML
    private void runQ4(ActionEvent event) {
        Lookup author = q4AuthorBox.getValue();
        if (author == null) {
            showWarning("Choose an author first.");
            return;
        }

        try {
            List<AuthorTitleRow> rows = reportDAO.membersWhoBorrowedAllByAuthor(author.id());
            q4Rows.setAll(rows);
            q4CountLabel.setText(rows.isEmpty()
                    ? "No member has borrowed every title by " + author.name()
                    : "Members who borrowed every title: " + rows.size());
        } catch (SQLException e) {
            q4Rows.clear();
            q4CountLabel.setText("Query failed");
            showError("Could not run Q4", e);
        }
    }
}
