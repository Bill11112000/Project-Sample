package com.pdm.controller;

import com.pdm.dao.ReportDAO;
import com.pdm.model.CategoryFineRow;
import com.pdm.model.TopTitleRow;

import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.Year;
import java.util.List;

public class ReportManage extends basecontrol {

    @FXML private TextField yearField;
    @FXML private TableView<TopTitleRow> topTable;
    @FXML private TableColumn<TopTitleRow, String> colTopBookId;
    @FXML private TableColumn<TopTitleRow, String> colTopTitle;
    @FXML private TableColumn<TopTitleRow, Integer> colTopCount;
    @FXML private Label topCountLabel;

    @FXML private TableView<CategoryFineRow> finesTable;
    @FXML private TableColumn<CategoryFineRow, String> colCategoryId;
    @FXML private TableColumn<CategoryFineRow, String> colCategoryName;
    @FXML private TableColumn<CategoryFineRow, Integer> colMembersWithFines;
    @FXML private TableColumn<CategoryFineRow, BigDecimal> colTotalOutstanding;
    @FXML private Label finesCountLabel;
    @FXML private Label grandTotalLabel;

    private final ReportDAO reportDAO = new ReportDAO();

    private final ObservableList<TopTitleRow> topRows = FXCollections.observableArrayList();
    private final ObservableList<CategoryFineRow> fineRows = FXCollections.observableArrayList();

    @FXML
    private void initialize() {
        colTopBookId.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().bookId()));
        colTopTitle.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().title()));
        colTopCount.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().timesBorrowed()));

        colCategoryId.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().memberTypeId()));
        colCategoryName.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().typeName()));
        colMembersWithFines.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().membersWithFines()));
        colTotalOutstanding.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().totalOutstanding()));

        topTable.setItems(topRows);
        finesTable.setItems(fineRows);

        yearField.setText(String.valueOf(Year.now().getValue()));

        runTopTitles(null);
        runFines(null);
    }

    @FXML
    private void runTopTitles(ActionEvent event) {
        Integer year = parseYear();
        if (year == null) {
            return;
        }

        try {
            List<TopTitleRow> rows = reportDAO.topTitlesThisYear(year);
            topRows.setAll(rows);
            topCountLabel.setText("Top " + rows.size() + " titles for " + year);
        } catch (SQLException e) {
            topRows.clear();
            topCountLabel.setText("Report failed");
            showError("Could not run the top titles report", e);
        }
    }

    @FXML
    private void runFines(ActionEvent event) {
        try {
            List<CategoryFineRow> rows = reportDAO.outstandingFinesByCategory();
            fineRows.setAll(rows);

            BigDecimal total = BigDecimal.ZERO;
            for (CategoryFineRow row : rows) {
                if (row.totalOutstanding() != null) {
                    total = total.add(row.totalOutstanding());
                }
            }

            finesCountLabel.setText("Showing " + rows.size() + " member categories");
            grandTotalLabel.setText("Total outstanding fines: " + total);
        } catch (SQLException e) {
            fineRows.clear();
            finesCountLabel.setText("Report failed");
            grandTotalLabel.setText("Total outstanding fines: -");
            showError("Could not run the fines report", e);
        }
    }

    private Integer parseYear() {
        String text = yearField.getText() == null ? "" : yearField.getText().trim();
        try {
            int year = Integer.parseInt(text);
            if (year < 1900 || year > 2100) {
                showWarning("Year must be between 1900 and 2100.");
                return null;
            }
            return year;
        } catch (NumberFormatException e) {
            showWarning("Year must be a whole number, for example " + Year.now().getValue() + ".");
            return null;
        }
    }
}
