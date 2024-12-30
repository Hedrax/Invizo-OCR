package com.example.ocrdesktop.data;

import com.example.ocrdesktop.utils.ReceiptType;
import com.example.ocrdesktop.utils.ReceiptTypeJSON;

public class Repo {
    Remote remote = new Remote();
    public boolean checkReceiptTypeNameAvailable(String text) {
        //TODO check all name of ReceiptType in the local database and return boolean True: name Available, False: name is reserved by another object
        return true;
    }

    public int createReceiptType(ReceiptTypeJSON receiptTypeJSON) {
        ReceiptType receiptType = receiptTypeJSON.getReceiptType();

        //TODO make the remote request on non IO-Working-Thread
        //posting the new object on the production database
        int response = remote.createNewReceiptType(receiptTypeJSON);

        if (response == 200) {
            //TODO save receipt Type in the local database on response

        }
        return response;

    }

    public int modifyReceiptType(ReceiptTypeJSON receiptTypeJSON, String oldName) {
        ReceiptType receiptType = receiptTypeJSON.getReceiptType();
        //TODO the modification process is a bit of a pain in the ass, as if the name of the receipt or one the column name has changed
        // will require to make modification to all the stored data "Receipts" before deleting the original ReceiptType row
        // at least that's the naive approach
        // therefor, postponed the implementation until we finish the base solution including login
        //posting the new object on the production database

        //remote.modifyReceiptType(receiptTypeJSON);
        return 200;
    }
  /*  // Database connection helper
    private static Connection getDatabaseConnection() throws SQLException {
        String url = "jdbc:sqlite:receipts.db";
        return DriverManager.getConnection(url);
    }

    // Dummy data for ReceiptType
    private static List<ReceiptType> getDummyReceiptTypes() {
        List<ReceiptType> receiptTypes = new ArrayList<>();
        receiptTypes.add(new ReceiptType("1", "Invoice", "An invoice receipt"));
        receiptTypes.add(new ReceiptType("2", "Payment", "A payment receipt"));
        return receiptTypes;
    }

    // Dummy data for ReceiptTypeFields
    private static List<ReceiptTypeFields> getDummyReceiptTypeFields() {
        List<ReceiptTypeFields> receiptTypeFields = new ArrayList<>();
        receiptTypeFields.add(new ReceiptTypeFields("1", "Field1", "String"));
        receiptTypeFields.add(new ReceiptTypeFields("1", "Field2", "Integer"));
        receiptTypeFields.add(new ReceiptTypeFields("2", "Field3", "Date"));
        return receiptTypeFields;
    }

    // Dummy data for Request
    private static List<Request> getDummyRequests() {
        List<Request> requests = new ArrayList<>();
        requests.add(new Request("1", "Pending", "user1", String.valueOf(new Timestamp(System.currentTimeMillis()))));
        requests.add(new Request("2", "Completed", "user2", String.valueOf(new Timestamp(System.currentTimeMillis()))));
        return requests;
    }

    // Dummy data for Receipt
    private static List<Receipt> getDummyReceipts() {
        List<Receipt> receipts = new ArrayList<>();
        receipts.add(new Receipt("1", "1", "1", "1", "image1.png", "pending", "152",String.valueOf(new Timestamp(System.currentTimeMillis()))));
        receipts.add(new Receipt("1", "1", "1", "1", "image1.png", "pending", "152",String.valueOf(new Timestamp(System.currentTimeMillis()))));
        return receipts;
    }

    // Refresh data method
    public static void refreshData() {
        try (Connection localConnection = getDatabaseConnection()) {
            // Get dummy data
            List<ReceiptType> receiptTypes = getDummyReceiptTypes();
            List<ReceiptTypeFields> receiptTypeFields = getDummyReceiptTypeFields();
            List<Request> requests = getDummyRequests();
            List<Receipt> receipts = getDummyReceipts();
            refreshReceiptType(localConnection, receiptTypes);
            refreshUploadRequests(localConnection, requests);
            refreshReceipt(localConnection, receipts);
            createTablesForReceiptTypes(localConnection);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void getAllReceipts() {
        try (Connection localConnection = getDatabaseConnection()) {
            // Retrieve and print all tables and their columns
            DatabaseMetaData metaData = localConnection.getMetaData();

            // Get the list of tables (assuming you're using SQLite or similar databases)
            try (ResultSet tablesResultSet = metaData.getTables(null, null, "%", new String[]{"TABLE"})) {
                while (tablesResultSet.next()) {
                    String tableName = tablesResultSet.getString("TABLE_NAME");
                    System.out.println("Table: " + tableName);

                    // Get the columns for the current table
                    try (ResultSet columnsResultSet = metaData.getColumns(null, null, tableName, "%")) {
                        while (columnsResultSet.next()) {
                            String columnName = columnsResultSet.getString("COLUMN_NAME");
                            String columnType = columnsResultSet.getString("TYPE_NAME");
                            System.out.println("\tColumn: " + columnName + " | Type: " + columnType);
                        }
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

*/

}
