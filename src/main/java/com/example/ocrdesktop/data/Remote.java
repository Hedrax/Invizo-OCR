package com.example.ocrdesktop.data;

import com.example.ocrdesktop.AppContext;
import com.example.ocrdesktop.utils.*;
import com.fasterxml.jackson.core.type.TypeReference;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;

import java.io.IOException;
import java.net.http.HttpResponse;
import java.util.*;

@Slf4j
public class Remote {
    static ApiClient apiClient = ApiClient.getInstance();

    public String createNewReceiptType(ReceiptTypeJSON receiptTypeJSON) {
        try {
            // Prepare payload
            Map<String, Object> payload = new HashMap<>();
            payload.put("name", receiptTypeJSON.getName());
            payload.put("template", receiptTypeJSON.getJsonTemplate());

            // Send POST request and receive ApiResponse<String>
            ApiResponse<String> responseWrapper = ApiClient.post(
                    "/receipt-types",
                    payload,
                    new TypeReference<>() {}
            );

            HttpResponse<String> httpResponse = responseWrapper.getHttpResponse();
            int statusCode = httpResponse.statusCode();
            String responseBody = responseWrapper.getBody();

            // Handle response based on status code
            if (statusCode == 201 || statusCode == 200) { // Assuming 201 Created or 200 OK
                return responseBody;
            } else {
                log.error("Failed to create receipt type - Status Code: {}, Body: {}", statusCode, responseBody);
                return null;
            }
        } catch (Exception e) {
            log.error("Failed to create receipt type: {}", e.getMessage(), e);
            return null;
        }
    }

    public int modifyReceiptType(ReceiptTypeJSON receiptTypeJSON) {
        try {
            // Prepare payload
            Map<String, Object> payload = new HashMap<>();
            payload.put("name", receiptTypeJSON.getName());
            payload.put("template", receiptTypeJSON.getJsonTemplate());

            // Send PUT request and receive ApiResponse<Void>
            ApiResponse<Void> responseWrapper = ApiClient.put(
                    "/receipt-types/" + receiptTypeJSON.getId(),
                    payload,
                    new TypeReference<>() {}
            );

            HttpResponse<String> httpResponse = responseWrapper.getHttpResponse();
            int statusCode = httpResponse.statusCode();

            // Handle response based on status code
            if (statusCode == 200) { // Assuming 200 OK
                return 200;
            } else {
                log.error("Failed to modify receipt type - Status Code: {}, Body: {}", statusCode, httpResponse.body());
                return 400;
            }
        } catch (Exception e) {
            log.error("Failed to modify receipt type: {}", e.getMessage(), e);
            return 400;
        }
    }
    public ObservableList<ReceiptType> getReceiptTypes() {
        try {
            // Send GET request and receive ApiResponse<List<Map<String, Object>>>
            ApiResponse<List<Map<String, Object>>> responseWrapper = ApiClient.get(
                    "/receipt-types",
                    new TypeReference<>() {}
            );

            HttpResponse<String> httpResponse = responseWrapper.getHttpResponse();
            int statusCode = httpResponse.statusCode();
            List<Map<String, Object>> responseBody = responseWrapper.getBody();

            // Handle response based on status code
            if (statusCode == 200) {
                ObservableList<ReceiptType> receiptTypes = FXCollections.observableArrayList();

                for (Map<String, Object> item : responseBody) {
                    String id = (String) item.get("receiptTypeId");
                    String name = (String) item.get("name");
                    Map<String, Object> templateJSON = (Map<String, Object>) item.get("template");
                    Map<String, Integer> column2idxMap = (Map<String, Integer>) item.get("column2idxMap");

                    ReceiptTypeJSON receiptTypeJSON = new ReceiptTypeJSON(id, new JSONObject(templateJSON), new HashMap<>(column2idxMap));
                    receiptTypeJSON.saveJSONLocally();
                    receiptTypes.add(receiptTypeJSON.getReceiptType());
                }

                return receiptTypes;
            } else {
                log.error("Failed to fetch receipt types - Status Code: {}, Body: {}", statusCode, httpResponse.body());
                return FXCollections.observableArrayList();
            }
        } catch (Exception e) {
            log.error("Failed to fetch receipt types: {}", e.getMessage(), e);
            return FXCollections.observableArrayList();
        }
    }
    public static ObservableList<Request> getRequests() {
        ObservableList<Request> Requests = FXCollections.observableArrayList();
        //TODO Rewan need to get all Requests from upload_requests table in  Request format
        return Requests;
    }
    public static ObservableList<Receipt> getReceipts() {
        ObservableList<Receipt> Receipts = FXCollections.observableArrayList();
        //TODO Rewan need to get all Receipts from receipt table in  Receipt format
        // need to convert json to map<string,string>
        return Receipts;
    }
    public int registerNewSuperAdmin(String username, String invitationToken, String email, String password, String confirmPassword) {
        try {
            // Prepare payload
            Map<String, Object> payload = new HashMap<>();
            payload.put("username", username);
            payload.put("invitationToken", invitationToken);
            payload.put("email", email);
            payload.put("password", password);
            payload.put("confirmPassword", confirmPassword);

            ApiResponse<Map<String, Object>> responseWrapper = ApiClient.post(
                    "/auth/register",
                    payload,
                    new TypeReference<>() {}
            );

            HttpResponse<String> httpResponse = responseWrapper.getHttpResponse();
            int statusCode = httpResponse.statusCode();


            // Handle response based on status code
            if (statusCode == 200) {
                return 200; // OK
            } else if (statusCode == 409) {
                return 409; // Conflict (e.g., email already exists)
            } else {
                return 400; // Bad Request or other client-side errors
            }
        } catch (Exception e) {
            log.error("Failed to register new super admin: {}", e.getMessage(), e);
            return 400;
        }
    }
    public boolean authenticate(String email, String password) {
        try {
            // Prepare payload
            Map<String, Object> loginPayload = new HashMap<>();
            loginPayload.put("email", email);
            loginPayload.put("password", password);

            ApiResponse<Map<String, Object>> responseWrapper = ApiClient.post(
                    "/auth/login",
                    loginPayload,
                    new TypeReference<>() {}
            );

            HttpResponse<String> httpResponse = responseWrapper.getHttpResponse();
            int statusCode = httpResponse.statusCode();
            Map<String, Object> responseBody = responseWrapper.getBody();

            // Handle response based on status code
            if (statusCode == 200) {
                String accessToken = (String) responseBody.get("accessToken");
                String refreshToken = (String) responseBody.get("refreshToken");

                if (accessToken != null) {
                    AuthorizationInfo tempAuth = new AuthorizationInfo(null, null, accessToken, refreshToken);
                    AppContext.getInstance().setAuthorizationInfo(tempAuth);
                    return true;
                }
            } else {
                log.error("Authentication failed - Status Code: {}, Body: {}", statusCode, httpResponse.body());
            }

            return false;
        } catch (Exception e) {
            log.error("Authentication failed: {}", e.getMessage(), e);
            return false;
        }
    }
    public AuthorizationInfo getAuthorizationInfo() {
        try {
            // Send GET request and receive ApiResponse<Map<String, Object>>
            ApiResponse<Map<String, Object>> responseWrapper = ApiClient.get(
                    "/users/me",
                    new TypeReference<>() {}
            );

            HttpResponse<String> httpResponse = responseWrapper.getHttpResponse();
            int statusCode = httpResponse.statusCode();
            Map<String, Object> responseBody = responseWrapper.getBody();

            if (statusCode == 200) {
                String userId = (String) responseBody.get("id");
                String username = (String) responseBody.get("username");
                String email = (String) responseBody.get("email");
                String role = (String) responseBody.get("role");

                User.Role userRole = User.Role.valueOf(role);

                String orgId = (String) responseBody.get("companyId");
                String orgName = (String) responseBody.get("tenantName");

                User user = new User(userId, username, email, userRole);
                Company company = new Company(orgId, orgName);

                return new AuthorizationInfo(user, company, AppContext.getInstance().getAuthorizationInfo().getAccessToken(), AppContext.getInstance().getAuthorizationInfo().getRefreshToken());
            } else {
                log.error("Failed to get authorization info - Status Code: {}, Body: {}", statusCode, httpResponse.body());
                AppContext.getInstance().getAuthorizationInfo().clearAuthentication();
                return null;
            }
        } catch (IOException | InterruptedException e) {
            log.error("Failed to get authorization info: {}", e.getMessage(), e);
            AppContext.getInstance().getAuthorizationInfo().clearAuthentication();
            return null;
        }
    }
    public List<User> getAllUsers(Company company) {
        try {
            // Send GET request and receive ApiResponse<List<Map<String, Object>>>
            ApiResponse<List<Map<String, Object>>> responseWrapper = ApiClient.get(
                    "/company/" + company.companyId + "/users",
                    new TypeReference<>() {}
            );

            HttpResponse<String> httpResponse = responseWrapper.getHttpResponse();
            int statusCode = httpResponse.statusCode();
            List<Map<String, Object>> responseBody = responseWrapper.getBody();

            if (statusCode == 200) {
                List<User> users = new ArrayList<>();
                for (Map<String, Object> userObject : responseBody) {
                    String userId = (String) userObject.get("id");
                    String username = (String) userObject.get("username");
                    String email = (String) userObject.get("email");
                    String role = (String) userObject.get("userRole");
                    User.Role userRole = User.Role.valueOf(role);

                    users.add(new User(userId, username, email, userRole));
                }
                return users;
            } else {
                log.error("Failed to fetch users - Status Code: {}, Body: {}", statusCode, httpResponse.body());
                throw new RuntimeException("Failed to fetch users");
            }
        } catch (Exception e) {
            log.error("Failed to fetch users: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to fetch users", e);
        }
    }
    //TODO update check if the user.password == User.PASSWORD_DEFAULT otherwise we're changing the password
    public void updateUser(User user, Company company) {
        try {
            // Prepare payload
            Map<String, Object> payload = new HashMap<>();
            payload.put("id", user.id);
            payload.put("companyId", company.companyId);
            payload.put("tenantName", company.name);
            payload.put("email", user.email);
            payload.put("role", user.role);

            // Send PUT request
            ApiResponse<Void> responseWrapper = ApiClient.put(
                    "/users/" + user.id,
                    payload,
                    new TypeReference<>() {}
            );

            HttpResponse<String> httpResponse = responseWrapper.getHttpResponse();
            int statusCode = httpResponse.statusCode();

            if (statusCode != 200) { // Assuming 200 OK
                log.error("Failed to update user - Status Code: {}, Body: {}", statusCode, httpResponse.body());
            }
        } catch (Exception e) {
            log.error("Failed to update user: {}", e.getMessage(), e);
        }
    }

    public void addUser(User user) {
        try {
            System.out.println(user);
            // Prepare payload
            Map<String, Object> payload = new HashMap<>();
            payload.put("email", user.email);
            payload.put("role", user.role);

            // Send POST request
            ApiResponse<Void> responseWrapper = ApiClient.post(
                    "/users/admin-create",
                    payload,
                    new TypeReference<>() {}
            );

            HttpResponse<String> httpResponse = responseWrapper.getHttpResponse();
            int statusCode = httpResponse.statusCode();

            if (statusCode != 201 && statusCode != 200) { // Assuming 201 Created or 200 OK
                log.error("Failed to add user - Status Code: {}, Body: {}", statusCode, httpResponse.body());
            }
        } catch (IOException | InterruptedException e) {
            log.error("Failed to add user: {}", e.getMessage(), e);
        }
    }
    public void deleteUsers(List<User> deletedUsers) {
        try {
            // Extract user IDs as UUIDs
            List<UUID> uuids = new ArrayList<>();
            for (User user : deletedUsers) {
                uuids.add(UUID.fromString(user.id));
            }

            // Prepare payload
            Map<String, Object> payload = new HashMap<>();
            payload.put("userIds", uuids);

            // Send DELETE request
            ApiResponse<Void> responseWrapper = ApiClient.delete(
                    "/users/bulk-delete",
                    payload,
                    new TypeReference<>() {}
            );

            HttpResponse<String> httpResponse = responseWrapper.getHttpResponse();
            int statusCode = httpResponse.statusCode();

            if (statusCode != 200 && statusCode != 204) { // Assuming 200 OK or 204 No Content
                log.error("Failed to delete users - Status Code: {}, Body: {}", statusCode, httpResponse.body());
            }
        } catch (Exception e) {
            log.error("Failed to delete users: {}", e.getMessage(), e);
        }
    }
    public void updateReceipts(ObservableList<Receipt> receipts) {
        try {
            // Prepare payload
            List<Map<String, Object>> receiptData = new ArrayList<>();
            for (Receipt receipt : receipts) {
                Map<String, Object> receiptMap = new HashMap<>();
                receiptMap.put("receiptId", receipt.receiptId);
                receiptMap.put("status", receipt.status);
                receiptMap.put("approvedBy", receipt.approvedByUserId);
                receiptMap.put("approvedAt", receipt.approvedAt);
                receiptMap.put("ocrData", receipt.ocrData);
                receiptData.add(receiptMap);
            }

            // Send PUT request
            ApiResponse<Void> responseWrapper = ApiClient.put(
                    "/receipts/update",
                    receiptData,
                    new TypeReference<>() {}
            );

            HttpResponse<String> httpResponse = responseWrapper.getHttpResponse();
            int statusCode = httpResponse.statusCode();

            if (statusCode != 200 && statusCode != 204) { // Assuming 200 OK or 204 No Content
                log.error("Failed to update receipts - Status Code: {}, Body: {}", statusCode, httpResponse.body());
            }
        } catch (Exception e) {
            log.error("Failed to update receipts: {}", e.getMessage(), e);
        }
    }
    public void deleteReceipts(List<Receipt> receiptsToDelete) {
        try {
            // Extract receipt IDs as UUIDs
            List<UUID> receiptIds = new ArrayList<>();
            for (Receipt receipt : receiptsToDelete) {
                receiptIds.add(UUID.fromString(receipt.receiptId));
            }

            // Prepare payload
            Map<String, Object> payload = new HashMap<>();
            payload.put("receiptIds", receiptIds);

            // Send DELETE request
            ApiResponse<Void> responseWrapper = ApiClient.delete(
                    "/bulk-delete",
                    payload,
                    new TypeReference<>() {}
            );

            HttpResponse<String> httpResponse = responseWrapper.getHttpResponse();
            int statusCode = httpResponse.statusCode();

            if (statusCode != 200 && statusCode != 204) { // Assuming 200 OK or 204 No Content
                log.error("Failed to delete receipts - Status Code: {}, Body: {}", statusCode, httpResponse.body());
            }
        } catch (Exception e) {
            log.error("Failed to delete receipts: {}", e.getMessage(), e);
        }
    }

    public void updateRequest(Request request) {
        try {
            // Prepare payload
            Map<String, Object> payload = new HashMap<>();
            payload.put("status", request.status.toString());

            // Send PUT request
            ApiResponse<Void> responseWrapper = ApiClient.put(
                    "/request/" + request.id,
                    payload,
                    new TypeReference<>() {}
            );

            HttpResponse<String> httpResponse = responseWrapper.getHttpResponse();
            int statusCode = httpResponse.statusCode();


            if (statusCode != 200 && statusCode != 204) { // Assuming 200 OK or 204 No Content
                log.error("Failed to update request - Status Code: {}, Body: {}", statusCode, httpResponse.body());
            }
        } catch (Exception e) {
            log.error("Failed to update request: {}", e.getMessage(), e);
        }
    }

    public boolean refreshAuthToken() {
        // 1) Retrieve the current refresh token from wherever you store it
        String refreshToken = AppContext.getInstance().getAuthorizationInfo().getRefreshToken();
        if (refreshToken == null || refreshToken.isEmpty()) {
            log.warn("No refresh token available; can't re-authenticate.");
            return false;
        }

        // 2) Attempt to call the refresh endpoint
        try {
            // Construct JSON payload
            Map<String, String> payload = new HashMap<>();
            payload.put("refreshToken", refreshToken);

            // Make a POST request to /auth/refresh with your existing 'sendRequestSync' or similar
            ApiResponse<Map> responseWrapper = ApiClient.post(
                    "/auth/refresh",
                    payload,
                    new TypeReference<>() {}
            );

            HttpResponse<String> httpResponse = responseWrapper.getHttpResponse();
            int statusCode = httpResponse.statusCode();

            if (statusCode == 200) {
                // 3) Parse new tokens
                Map body = responseWrapper.getBody();
                String newAccessToken = (String) body.get("accessToken");
                String newRefreshToken = (String) body.get("refreshToken");

                if (newAccessToken == null || newRefreshToken == null) {
                    log.error("Refresh endpoint returned null tokens!");
                    return false;
                }

                // 4) Store new tokens in your context
                AuthorizationInfo authInfo = AppContext.getInstance().getAuthorizationInfo();
                authInfo.setAccessToken(newAccessToken);
                authInfo.setRefreshToken(newRefreshToken);

                log.info("Successfully refreshed tokens. AccessToken: {}, RefreshToken: {}", newAccessToken, newRefreshToken);
                return true;
            } else {
                log.error("Failed to refresh token. Status: {}, Body: {}", statusCode, httpResponse.body());
                return false;
            }
        } catch (IOException | InterruptedException e) {
            log.error("Exception while refreshing token: {}", e.getMessage(), e);
            return false;
        }
    }
}
