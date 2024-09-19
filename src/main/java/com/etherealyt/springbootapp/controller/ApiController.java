package com.etherealyt.springbootapp.controller;

import antlr.StringUtils;
import org.diffkit.diff.conf.DKLauncher;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.*;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@RestController
public class ApiController {

    @Value("${sample.src.query}")
    String srcQuery;

    @Value("${sample.dest.query}")
    String destQuery;

    @GetMapping("/ping")
    public String healthCheck() {
        return "pong";
    }

    @GetMapping("/compare")
    public String compareTables() throws Exception {
        compare();
        return "Comparison Successful!";
    }

    @GetMapping("/compare-csv")
    public String compareTablesOfflineCSV() throws Exception {
        compareForCsv();
        return "Comparison for CSV Successful!";
    }

    public synchronized void compare() throws Exception {
        String url = "jdbc:postgresql://localhost:5432/postgres";
        String user = "dbauser";
        String password = "changeit";

        try (Connection conn = DBConnectionUtility.getConnection(url, user, password)) {
            if (conn != null) {
                System.out.println("Connected to the PostgreSQL database successfully!");
                fireQuery(srcQuery, destQuery, conn, "C:\\Users\\Deepak\\IdeaProjects\\database-compare-tool\\Dumpdir\\", "dd");
            } else {
                System.out.println("Failed to make a connection!");
            }
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
            throw new Exception("Database connection failed: " + e.getMessage(), e);
        }
    }

    public synchronized void compareForCsv() throws Exception {
        Connection conn = null;
        fireQuery(srcQuery, destQuery, conn, "C:\\Users\\Deepak\\IdeaProjects\\database-compare-tool\\Dumpdir\\", "dd");
    }

    private void fireQuery(String srcQuery, String destQuery, Connection conn, String dumpDir, String type) throws Exception {
        try {
            // Write to CSV files
            if(conn!=null) {
                writeToFile(srcQuery, conn, "src_" + type, dumpDir);
                writeToFile(destQuery, conn, "tgt_" + type, dumpDir);
            }
            // Perform the comparison
            fireComparison(dumpDir, type);
        } catch (Exception e) {
            e.printStackTrace();
            throw new Exception("Query execution failed: " + e.getMessage(), e);
        }
    }

    public static void writeToFile(String selectQuery, Connection connection, String type, String dumpDir)
            throws SQLException, IOException {
        try (PreparedStatement pstmt = connection.prepareStatement(selectQuery);
             ResultSet rs = pstmt.executeQuery()) {
            writeResultSetToCSV(rs, dumpDir + type + ".csv");
        }
    }

    public static void writeResultSetToCSV(ResultSet rs, String output) throws SQLException, IOException {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(output))) {
            ResultSetMetaData rsm = rs.getMetaData();
            int columnCount = rsm.getColumnCount();

            // Write the CSV header
            for (int i = 1; i <= columnCount; i++) {
                bw.write(rsm.getColumnName(i));
                if (i < columnCount) {
                    bw.write(",");
                }
            }
            bw.newLine();

            // Write the data rows
            while (rs.next()) {
                for (int i = 1; i <= columnCount; i++) {
                    bw.write(rs.getString(i) != null ? rs.getString(i) : "");
                    if (i < columnCount) {
                        bw.write(",");
                    }
                }
                bw.newLine();
            }
            bw.flush();
        }
    }

    public static void fireComparison(String dumpDir, String type) throws IOException {
        String diffFilePath = dumpDir + "final.sink_" + type + ".diff";
        String outputFilePath = dumpDir + "output.csv";
        File diffFile = new File(diffFilePath);

        // Create or delete the diff file
        if (diffFile.exists()) {
            if (!diffFile.delete()) {
                throw new IOException("Failed to delete the existing diff file.");
            }
        } else {
            if (!diffFile.createNewFile()) {
                throw new IOException("Failed to create a new diff file.");
            }
            diffFile.delete();  // Clean up after creation
        }

        DKLauncher.main(new String[]{"-planfiles", dumpDir + "config_" + type + ".xml"});
        Map<String, Map<String, String[]>> differences = parseDiffFile(diffFilePath);
        writeDifferencesToCSV(differences, outputFilePath);
    }

    public static Map<String, Map<String, String[]>> parseDiffFile(String filePath) throws IOException {
        Map<String, Map<String, String[]>> differences = new HashMap<>();
        Pattern keyPattern = Pattern.compile("@\\{(.*?)\\}");  // Pattern to match the key (e.g., name=Frank Miller)
        Pattern leftPattern = Pattern.compile("<(.*)");        // Pattern to match values on the left
        Pattern rightPattern = Pattern.compile(">(.*)");       // Pattern to match values on the right
        Pattern missingPattern = Pattern.compile("!\\{(.*?)\\}");  // Pattern to match missing rows (e.g., !{name=Ruby Carter})

        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            String currentKey = null;
            String currentAttribute = null;
            String leftValue = null;

            while ((line = reader.readLine()) != null) {
                Matcher keyMatcher = keyPattern.matcher(line);
                Matcher leftMatcher = leftPattern.matcher(line);
                Matcher rightMatcher = rightPattern.matcher(line);
                Matcher missingMatcher = missingPattern.matcher(line);

                // Extract the key and other attributes inside "@{...}"
                if (keyMatcher.find()) {
                    currentKey = keyMatcher.group(1).trim();
                    differences.putIfAbsent(currentKey, new HashMap<>());
                }
                // Handle missing rows (indicated by !{...})
                else if (missingMatcher.find()) {
                    currentKey = missingMatcher.group(1).trim();
                    differences.putIfAbsent(currentKey, new HashMap<>());
                    differences.get(currentKey).put("Missing", new String[]{"Left", "Missing in Right"});
                }
                // Extract attribute name (e.g., email, id, department)
                else if (!line.startsWith("<") && !line.startsWith(">")) {
                    currentAttribute = line.trim();
                }
                // Extract left value
                else if (leftMatcher.find()) {
                    leftValue = leftMatcher.group(1).trim();
                }
                // Extract right value
                else if (rightMatcher.find()) {
                    if (currentKey != null && currentAttribute != null) {
                        String rightValue = rightMatcher.group(1).trim();
                        differences.get(currentKey).put(currentAttribute, new String[]{leftValue, rightValue});
                    }
                    // Reset leftValue for the next comparison
                    leftValue = null;
                }
            }
        }

        return differences;
    }

    public static void writeDifferencesToCSV(Map<String, Map<String, String[]>> differences, String filePath) {
        try (FileWriter csvWriter = new FileWriter(filePath)) {
            // Write CSV header
            csvWriter.append("Key,Attribute,Left Value,Right Value\n");

            // Write data rows
            for (Map.Entry<String, Map<String, String[]>> entry : differences.entrySet()) {
                String key = entry.getKey();

                for (Map.Entry<String, String[]> attrEntry : entry.getValue().entrySet()) {
                    String attribute = attrEntry.getKey();
                    String[] values = attrEntry.getValue();
                    String leftValue = values[0] != null ? values[0] : "";
                    String rightValue = values[1] != null ? values[1] : "";

                    csvWriter.append(key)
                            .append(',')
                            .append(attribute)
                            .append(',')
                            .append(leftValue)
                            .append(',')
                            .append(rightValue)
                            .append('\n');
                }
            }

            System.out.println("CSV file created successfully at: " + filePath);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
