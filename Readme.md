***Database Comparison Tool***


This project is a Spring Boot application that performs data comparison between two tables from a PostgreSQL database or between two CSV files generated from the database tables. The application uses DiffKit to handle the differences between the datasets and outputs a CSV report containing the mismatches.

**Features**

**Compare Database Tables:** Runs SQL queries on two PostgreSQL tables and compares the results.

**Compare CSV Files:** Compares the contents of two CSV files and generates a diff report.

**Mismatches Handling:** Handles cases where rows or attributes are missing on one side or have different values.

**CSV Output:** Writes the comparison results (differences) to a CSV file.


**Technologies Used**

Java 17

Spring Boot

PostgreSQL (JDBC for database connectivity)

DiffKit (for data comparison)

Maven (for project management)

Apache Commons IO (for file operations)

**License**
This project is licensed under the MIT License.

**Author**
EtherealYT
For questions or support, feel free to reach out!