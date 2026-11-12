# PDM Library Management System

A JavaFX desktop application for managing a small library: book catalog, members,
loans, reservations, fines, and reports. All data is read from and written to MySQL
with JDBC and `PreparedStatement`s.

## 1. Requirements

| Item | Version |
| --- | --- |
| JDK | **Java 25** (tested with Eclipse Adoptium 25.0.4) |
| JavaFX | 25 (resolved by Maven) |
| JDBC driver | **MySQL Connector/J 9.1.0** (resolved by Maven) |
| Database | MySQL 8.0 or newer |
| Build tool | Maven 3.9+ |
| Scene Builder | 26 (optional, for editing FXML) |

You do not need to download the JavaFX or JDBC jars manually — `pom.xml` declares them.

## 2. Database setup

1. Import `../Library-Management-System-main/database/database.sql` (the
   `Library-Management-System-main/database` folder next to `libm`) with MySQL
   Workbench, or from that database folder:
   ```bash
   mysql -u root -p < database.sql
   ```
   This creates the `library_management_db` schema (13 tables, 6 triggers, and sample data).
2. Configure the connection in `db.properties` in the **`libm`** folder:
   ```properties
   db.url=jdbc:mysql://localhost:3306/library_management_db
   db.user=root
   db.password=your_password
   ```
   The application reads `db.properties` from the current working directory, so always
   start it from the `libm` folder.
3. Keep your real password out of commits. `db.properties` is a local configuration
   file; replace the value with a placeholder before pushing to a public repository.

## 3. Build and run

1. Make sure `JAVA_HOME` points to JDK 25.
   ```powershell
   $env:JAVA_HOME="C:\Program Files\Eclipse Adoptium\jdk-25.0.4.101-hotspot"
   ```
2. From the `libm` folder, compile once to check the project:
   ```bash
   mvn clean compile
   ```
3. Start the application:
   ```bash
   mvn javafx:run
   ```
4. Optional: run `com.pdm.connect.connectiontest` from your IDE to confirm the
   connection. It prints `Connected to database: library_management_db` on success.

If Maven reports `Unsupported major.minor version 67.0`, it is using an older JDK —
set `JAVA_HOME` to JDK 25 as shown above.

## 4. Screens

Every screen shares the same top bar: **Home**, a **☰** menu for the main sections,
and a **🔎** search button. Search opens in its own window so the current screen stays
open behind it.

| Screen | What it does |
| --- | --- |
| **Home** | Card-based navigation to every screen. |
| **Book Catalog** | Master–detail: browse all titles, select one to see its copies, filter to available copies (Q1). |
| **Members** | Full CRUD: add, update, delete (with confirmation), category and account status. The table lists every member; selecting a row fills the form. |
| **Loans** | Master–detail: all loans and their lines; per-member overdue report (Q2). |
| **Search** | Parameterised book search by title, author, category and copy status; members who borrowed every title by an author (Q4). |
| **Fines** | List all fines and mark an unpaid fine as **Paid** or **Waived**. |
| **Reservations** | Browse all reservations with member, title, date and status. |
| **Reports** | Top 10 most borrowed titles for a year (Q3); outstanding fines per member category with a grand total (Q5). |

## 5. Required queries

| Query | Where to find it | SQL summary |
| --- | --- | --- |
| **Q1** – available copies of a title | Book Catalog → *Available copies (Q1)* | `Book_Copy` filtered by `Book_ID` and `Status = 'Available'` |
| **Q2** – overdue loans of a member | Loans → *Overdue loans (Q2)* | `Loan_Transaction` + `Loan_Detail` where `Return_Date IS NULL` and `Due_Date < CURDATE()` |
| **Q3** – top 10 titles in a year | Reports → *Top titles by year (Q3)* | `GROUP BY` title, `COUNT(*)`, `ORDER BY ... DESC LIMIT 10` |
| **Q4** – members who borrowed every title by an author | Search → *Members who read an author (Q4)* | double `NOT EXISTS` over `Book_Author` and loan history |
| **Q5** – outstanding fines per member category | Reports → *Outstanding fines (Q5)* | `LEFT JOIN` + conditional `SUM` where `Payment_Status = 'Unpaid'` |

The Reports screen also shows the **total outstanding fine amount** as a report aggregate.

## 6. Implementation notes

- Every user-supplied value is bound with `?` in a `PreparedStatement`; no SQL is built
  by concatenating input.
- Connections, statements and result sets use try-with-resources.
- Input is validated before it reaches the database (required fields, email format,
  year range, phone/identity length).
- Database errors are translated into plain-language messages by `DbErrors` instead of
  showing stack traces.
- Deleting a member asks for confirmation first.
- `basecontrol` holds the shared navigation and alert helpers. Each screen controller
  extends it, so adding a screen means adding an FXML view plus a controller with
  `initialize()` and `@FXML` handlers.
- Table rows are immutable Java `record`s in `com.pdm.model`; SQL lives in
  `com.pdm.dao`. There are no placeholder/empty model classes.
- Navigation is centralised in `basecontrol.openView(view, title)`; the search screen
  opens a separate `Stage` owned by the main window.

## 7. Project layout

```
libm/
├── db.properties                     local connection settings (not for sharing)
├── pom.xml                           JavaFX 25 + MySQL Connector/J 9.1.0
├── README.md
└── src/main/
    ├── java/com/pdm/
    │   ├── App.java                  JavaFX entry point
    │   ├── module-info.java          module opens/exports for JavaFX
    │   ├── connect/                  dbconnect, DbErrors, connectiontest
    │   ├── controller/               one controller per screen + basecontrol
    │   ├── dao/                      all SQL (PreparedStatements)
    │   └── model/                    row records used by the tables
    └── resources/com/pdm/view/       FXML layouts and style.css
```

## 8. Git and GitHub contribution guide (team)

- Install Git: <https://git-scm.com/>
- In VS Code, sign in to GitHub, open **Source Control**, and clone the repository.
- Configure your identity once:
  ```bash
  git config --global user.name "Your Name"
  git config --global user.email "your.email@example.com"
  ```
- Always **pull** before coding. Commit and push from the Source Control panel.
- Build output (`target/`) and IDE files are ignored by `.gitignore`; do not commit them.

## 9. Scene Builder (team)

- Download: <https://gluonhq.com/products/scene-builder/>
- `Preview` → `Theme` → `Modena (FX8)`.
- Avoid Gluon components (the blue ones).

## 10. Troubleshooting

| Symptom | Likely cause and fix |
| --- | --- |
| `Access denied for user ...` | Wrong `db.user` / `db.password` in `db.properties`. |
| `Unknown database 'library_management_db'` | The schema was not imported; run `database.sql` first. |
| `Communications link failure` | MySQL is not running, or `db.url` points to the wrong host/port. |
| `Cannot read ... db.properties` | Start the app from the `libm` folder so the file is found. |
| `Unsupported major.minor version 67.0` | Maven is using an older JDK; set `JAVA_HOME` to JDK 25. |
| FXML fails to load a controller | The controller class must be `public`, in `com.pdm.controller`, and opened to `javafx.fxml` in `module-info.java`. |
