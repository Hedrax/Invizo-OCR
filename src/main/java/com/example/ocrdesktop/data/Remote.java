package com.example.ocrdesktop.data;

import com.example.ocrdesktop.AppContext;
import com.example.ocrdesktop.utils.*;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.util.Pair;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;

import java.io.IOException;
import java.net.http.HttpResponse;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.*;

@Slf4j
public class Remote {

    public String createNewReceiptType(ReceiptTypeJSON receiptTypeJSON) {
        try {

            ObjectMapper mapper = ApiClient.getInstance().getObjectMapper();
            Map<String, Object> templateMap = mapper.readValue(
                    receiptTypeJSON.getJsonTemplate().toString(),
                    new TypeReference<Map<String, Object>>() {}
            );

            Map<String, Object> payload = new HashMap<>();
            payload.put("name", receiptTypeJSON.getName());
            payload.put("column2idxMap", receiptTypeJSON.getMap());
            payload.put("template", templateMap);

            ApiResponse<Map<String, Object>> response = ApiClient.post(
                    "/receipt-types",
                    payload,
                    new TypeReference<>() {}
            );

             HttpResponse<String> httpResponse = response.getHttpResponse();
             int statusCode = httpResponse.statusCode();

             if (statusCode == 200 || statusCode == 201) {
                 // Success
                 Map<String, Object> responseBody = response.getBody();
                 if (responseBody != null && responseBody.containsKey("receiptTypeId")) {
                     return (String) responseBody.get("receiptTypeId");
                 }
             } else {
                 handleError(httpResponse);
             }
             return null;

        } catch (Exception e) {
            System.err.println("Failed to create receipt type: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }


    public int modifyReceiptType(ReceiptTypeJSON receiptTypeJSON, String oldName) {
        try {
            ObjectMapper mapper = ApiClient.getInstance().getObjectMapper();
            Map<String, Object> templateMap = mapper.readValue(
                    receiptTypeJSON.getJsonTemplate().toString(),
                    new TypeReference<Map<String, Object>>() {}
            );
            // Prepare payload
            Map<String, Object> payload = new HashMap<>();
            payload.put("name", receiptTypeJSON.getName());
            payload.put("column2idxMap", receiptTypeJSON.getMap());
            payload.put("template", templateMap);

            // Send PUT request
            ApiResponse<Void> responseWrapper = ApiClient.put(
                    "/receipt-types/" + receiptTypeJSON.getId(),
                    payload,
                    new TypeReference<>() {}
            );

            HttpResponse<String> httpResponse = responseWrapper.getHttpResponse();
            int statusCode = httpResponse.statusCode();

            if (statusCode == 200) {
                return 200; // OK
            } else {
                handleError(httpResponse);

                return 400;
            }

        } catch (Exception e) {
            log.error("Failed to modify receipt type: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to modify receipt type.");
        }
    }
    public ObservableList<ReceiptType> getReceiptTypes() {
        try {
            // Send GET request
            ApiResponse<List<Map<String, Object>>> responseWrapper = ApiClient.get(
                    "/receipt-types/json",
                    new TypeReference<>() {}
            );

            HttpResponse<String> httpResponse = responseWrapper.getHttpResponse();
            int statusCode = httpResponse.statusCode();
            List<Map<String, Object>> responseBody = responseWrapper.getBody();

            if (statusCode == 200 && responseBody != null) {
                // Build the ReceiptType objects
                ObservableList<ReceiptType> receiptTypes = FXCollections.observableArrayList();
                for (Map<String, Object> item : responseBody) {
                    String id = (String) item.get("receiptTypeId");
                    Map<String, Object> templateJSON = (Map<String, Object>) item.get("template");
                    Map<String, Integer> column2idxMap = (Map<String, Integer>) item.get("column2idxMap");

                    ReceiptTypeJSON receiptTypeJSON = new ReceiptTypeJSON(id, new JSONObject(templateJSON), new HashMap<>(column2idxMap));
                    try {
                        receiptTypes.add(receiptTypeJSON.getReceiptType());
                        receiptTypeJSON.saveJSONLocally();
                    } catch (Exception e) {
                        log.error("Failed to create receipt type: {}", e.getMessage(), e);
                    }
                }
                return receiptTypes;
            } else {
                handleError(httpResponse);
                throw new RuntimeException("Failed to fetch receipt types: " + httpResponse);
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch receipt types: " + e.getMessage());
        }
    }
    public static Pair<ObservableList<Request>, ObservableList<Receipt>> getRequestsAndReceipts(Timestamp timestamp) {
        ObservableList<Request> requests = FXCollections.observableArrayList();
        ObservableList<Receipt> receipts = FXCollections.observableArrayList();

        try {
            // Convert Timestamp to LocalDateTime string format
            LocalDateTime dateTime = timestamp.toLocalDateTime();
            String dateTimeStr = dateTime.toString();

            // Create a type reference that matches the backend response structure
            TypeReference<List<Map<String, Object>>> typeRef = new TypeReference<>() {};
            ApiResponse<List<Map<String, Object>>> response = ApiClient.get("/request/after?dateTime=" + dateTimeStr, typeRef);

            ObjectMapper mapper = ApiClient.getInstance().getObjectMapper();

            if (response.getHttpResponse().statusCode() == 200 && response.getBody() != null) {
                for (Map<String, Object> requestData : response.getBody()) {
                    // Extract request fields from the response
                    String requestId = ((String) requestData.get("requestId"));
                    String status = (String) requestData.get("status");
                    String uploadedBy = (String) requestData.get("uploadedBy");
                    Timestamp uploadedAt = Timestamp.valueOf(LocalDateTime.parse((String) requestData.get("uploadedAt")));

                    // Create Request object
                    Request request = new Request(
                            requestId,
                            status,
                            uploadedBy,
                            uploadedAt
                    );

                    // Handle receipts
                    ObservableList<Receipt> requestReceipts = FXCollections.observableArrayList();
                    List<Map<String, Object>> receiptsList = mapper.convertValue(
                            requestData.get("receipts"),
                            new TypeReference<List<Map<String, Object>>>() {}
                    );

                    if (receiptsList != null) {
                        for (Map<String, Object> receiptData : receiptsList) {
                            // Extract receipt fields
                            String receiptId = (String) receiptData.get("receiptId");
                            String receiptTypeId = (String) receiptData.get("receiptTypeId");
                            String imageUrl = (String) receiptData.get("imageUrl");
                            String receiptStatus = (String) receiptData.get("status");
                            String approvedBy = (String) receiptData.get("approvedBy");

                            // Convert approvedAt if present
                            Timestamp approvedAt = null;
                            if (receiptData.get("approvedAt") != null) {
                                approvedAt = Timestamp.valueOf(LocalDateTime.parse((String) receiptData.get("approvedAt")));
                            }

                            // Convert ocrData
                            @SuppressWarnings("unchecked")
                            Map<String, Object> ocrDataRaw = (Map<String, Object>) receiptData.get("ocrData");
                            HashMap<Integer, String> ocrData = new HashMap<>();
                            if (ocrDataRaw != null) {
                                ocrDataRaw.forEach((key, value) ->
                                        ocrData.put(Integer.parseInt(key), (String) value)
                                );
                            }

                            //if got something from the remote it won't have a path
                            Receipt receipt = new Receipt(
                                    receiptId,
                                    receiptTypeId,
                                    requestId,
                                    imageUrl,
                                    receiptStatus,
                                    ocrData,
                                    approvedBy,
                                    approvedAt,
                                    null
                            );

                            requestReceipts.add(receipt);
                            receipts.add(receipt);
                        }
                    }

                    request.setData(requestReceipts, null); // ReceiptType can be set later if needed
                    requests.add(request);
                }
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            System.err.println("Failed to fetch requests and receipts: " + e.getMessage());
        }

        return new Pair<>(requests, receipts);
    }
    public int registerNewSuperAdmin(String username, String invitationToken, String email, String password, String confirmPassword) {
        try {
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

            switch (statusCode) {
                case 200:
                    return 200; // OK
                case 409:
                    return 409; // Conflict
                default:
                    handleError(httpResponse);
                    return 400;
            }

        } catch (Exception e) {
            log.error("Failed to register new super admin: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to register new super admin.");
        }
    }
    public boolean authenticate(String email, String password) {
        try {
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

            if (statusCode == 200 && responseBody != null) {
                String accessToken = (String) responseBody.get("accessToken");
                String refreshToken = (String) responseBody.get("refreshToken");

                if (accessToken != null) {
                    AuthorizationInfo tempAuth = new AuthorizationInfo(null, null, accessToken, refreshToken);
                    AppContext.getInstance().setAuthorizationInfo(tempAuth);
                    return true;
                }
            }
            return false;

        } catch (Exception e) {
            log.error("Authentication failed: {}", e.getMessage(), e);
            throw new RuntimeException("Authentication failed");
        }
    }
    public AuthorizationInfo getAuthorizationInfo() {
        try {
            ApiResponse<Map<String, Object>> responseWrapper = ApiClient.get(
                    "/users/me",
                    new TypeReference<>() {}
            );

            HttpResponse<String> httpResponse = responseWrapper.getHttpResponse();
            int statusCode = httpResponse.statusCode();
            Map<String, Object> responseBody = responseWrapper.getBody();

            if (statusCode == 200 && responseBody != null) {
                String userId = (String) responseBody.get("id");
                String username = (String) responseBody.get("username");
                String email = (String) responseBody.get("email");
                String role = (String) responseBody.get("role");
                String orgId = (String) responseBody.get("tenantId");
                String orgName = (String) responseBody.get("tenantName");

                User.Role userRole = User.Role.valueOf(role);
                User user = new User(userId, username, email, userRole);
                Company company = new Company(orgId, orgName);

                AuthorizationInfo currentAuth = AppContext.getInstance().getAuthorizationInfo();
                return new AuthorizationInfo(
                        user,
                        company,
                        currentAuth.getAccessToken(),
                        currentAuth.getRefreshToken()
                );
            } else {
                handleError(httpResponse);
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
            ApiResponse<List<Map<String, Object>>> responseWrapper = ApiClient.get(
                     "/tenants/"+ UUID.fromString(company.companyId) + "/users",
                    new TypeReference<>() {}
            );

            HttpResponse<String> httpResponse = responseWrapper.getHttpResponse();
            int statusCode = httpResponse.statusCode();
            List<Map<String, Object>> responseBody = responseWrapper.getBody();

            if (statusCode == 200 && responseBody != null) {
                List<User> users = new ArrayList<>();
                for (Map<String, Object> userObject : responseBody) {
                    String userId = (String) userObject.get("id");
                    String username = (String) userObject.get("username");
                    String email = (String) userObject.get("email");
                    String role = (String) userObject.get("role");
                    User.Role userRole = User.Role.valueOf(role);
                    users.add(new User(userId, username, email, userRole));
                }
                return users;
            } else {
                handleError(httpResponse);
                throw new RuntimeException("Failed to fetch users.");
            }

        } catch (Exception e) {
            log.error("Failed to fetch users: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to fetch users.");
        }
    }

    public boolean updateUser(User user) {
        try {
            // Prepare payload
            Map<String, Object> payload = new HashMap<>();
            payload.put("id", user.id);
            payload.put("email", user.email);
            payload.put("username", user.userName);
            if (!User.PASSWORD_DEFAULT.equals(user.getPassword())) {
                payload.put("password", user.getPassword());
            }
            payload.put("role", user.role);

            ApiResponse<Void> responseWrapper = ApiClient.put(
                    "/users/" + user.id,
                    payload,
                    new TypeReference<>() {}
            );

            HttpResponse<String> httpResponse = responseWrapper.getHttpResponse();
            int statusCode = httpResponse.statusCode();

            if (statusCode == 200 || statusCode == 204) {
                return true;
            } else {
                handleError(httpResponse);
                return false;
            }

        } catch (Exception e) {
            log.error("Failed to update user: {}", e.getMessage(), e);
            return false;
        }
    }

    public boolean addUser(User user) {
        try {
            // Prepare payload
            Map<String, Object> payload = new HashMap<>();
            payload.put("userId", user.id);
            payload.put("username", user.userName);
            payload.put("email", user.email);
            payload.put("roleType", user.role.toString());
            payload.put("password", user.getPassword());

            ApiResponse<Void> responseWrapper = ApiClient.post(
                    "/users/admin-create",
                    payload,
                    new TypeReference<>() {}
            );

            HttpResponse<String> httpResponse = responseWrapper.getHttpResponse();
            int statusCode = httpResponse.statusCode();

            if (statusCode == 200) {
                return true;
            } else if (statusCode == 409) {
                throw new RuntimeException("User already exists.");
            } else {
                handleError(httpResponse);
                throw new RuntimeException("Failed to add user.");
            }

        } catch (IOException | InterruptedException e) {
            log.error("Failed to add user: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to add user.");
        }
    }
    public void deleteUsers(List<User> deletedUsers) {
        try {
            List<UUID> uuids = new ArrayList<>();
            for (User user : deletedUsers) {

                UUID uuid = UUID.fromString(user.id);
                uuids.add(uuid);
            }

            // Send DELETE request
            ApiResponse<Void> responseWrapper = ApiClient.delete(
                    "/users/bulk-delete",
                    uuids,
                    new TypeReference<>() {
                    }
            );

            HttpResponse<String> httpResponse = responseWrapper.getHttpResponse();
            int statusCode = httpResponse.statusCode();

            if (statusCode != 200 && statusCode != 204) {
                handleError(httpResponse);
                log.error("Failed to delete users - Status: {}, Body: {}", statusCode, httpResponse.body());
            }
        } catch(Exception e) {
                log.error("Failed to delete users: {}", e.getMessage(), e);
                throw new RuntimeException("Failed to delete users");
        }

    }
    public void updateReceipts(ObservableList<Receipt> receipts) {
        try {
            List<Map<String, Object>> receiptData = new ArrayList<>();
            for (Receipt receipt : receipts) {
                Map<String, Object> receiptMap = new HashMap<>();
                receiptMap.put("receiptId", receipt.receiptId);
                receiptMap.put("status", receipt.status.toString());
                receiptMap.put("approvedAt", receipt.approvedAt);
                receiptMap.put("ocrData", receipt.ocrData);
                receiptData.add(receiptMap);
            }

            ApiResponse<Void> responseWrapper = ApiClient.put(
                    "/receipts",
                    receiptData,
                    new TypeReference<>() {}
            );

            HttpResponse<String> httpResponse = responseWrapper.getHttpResponse();
            int statusCode = httpResponse.statusCode();

            if (statusCode != 200 && statusCode != 204) {
                handleError(httpResponse);
                log.error("Failed to update receipts - Status: {}, Body: {}", statusCode, httpResponse.body());
            }
        } catch (Exception e) {
            log.error("Failed to update receipts: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to update receipts.");
        }
    }
    public void deleteReceipts(List<Receipt> receiptsToDelete) {
        if (receiptsToDelete.isEmpty()) {
            return;
        }
        try {
            List<UUID> receiptIds = new ArrayList<>();
            for (Receipt receipt : receiptsToDelete) {
                receiptIds.add(UUID.fromString(receipt.receiptId));
            }

            ApiResponse<Void> responseWrapper = ApiClient.delete(
                    "/receipts/bulk-delete",
                    receiptIds,
                    new TypeReference<>() {}
            );

            HttpResponse<String> httpResponse = responseWrapper.getHttpResponse();
            int statusCode = httpResponse.statusCode();

            if (statusCode != 200 && statusCode != 204) {
                handleError(httpResponse);
                log.error("Failed to delete receipts - Status: {}, Body: {}", statusCode, httpResponse.body());
            }
        } catch (Exception e) {
            log.error("Failed to delete receipts: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to delete receipts.");
        }
    }

    public void updateRequest(Request request) {
        try {
            // Construct the URL with the status as a query parameter
            String url = "/request/" + request.id + "?status=" + request.status.toString();
    
            ApiResponse<Void> responseWrapper = ApiClient.put(
                    url,
                    null,
                    new TypeReference<>() {}
            );
    
            HttpResponse<String> httpResponse = responseWrapper.getHttpResponse();
            int statusCode = httpResponse.statusCode();
    
            if (statusCode != 200 && statusCode != 204) {
                handleError(httpResponse);
                log.error("Failed to update request - Status: {}, Body: {}", statusCode, httpResponse.body());
            }
        } catch (Exception e) {
            log.error("Failed to update request: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to update request.");
        }
    }

    public boolean refreshAuthToken() {
        String refreshToken = AppContext.getInstance().getAuthorizationInfo().getRefreshToken();
        if (refreshToken == null || refreshToken.isEmpty()) {
            log.warn("No refresh token available; can't re-authenticate.");
            return false;
        }

        try {
            Map<String, String> payload = new HashMap<>();
            payload.put("refreshToken", refreshToken);

            ApiResponse<Map<String, Object>> responseWrapper = ApiClient.post(
                    "/auth/refresh",
                    payload,
                    new TypeReference<>() {}
            );

            HttpResponse<String> httpResponse = responseWrapper.getHttpResponse();
            int statusCode = httpResponse.statusCode();

            if (statusCode == 200 && responseWrapper.getBody() != null) {
                Map<String, Object> body = responseWrapper.getBody();
                String newAccessToken = (String) body.get("accessToken");
                String newRefreshToken = (String) body.get("refreshToken");

                if (newAccessToken == null || newRefreshToken == null) {
                    log.error("Refresh endpoint returned null tokens!");
                    return false;
                }

                AuthorizationInfo authInfo = AppContext.getInstance().getAuthorizationInfo();
                authInfo.setAccessToken(newAccessToken);
                authInfo.setRefreshToken(newRefreshToken);

                log.info("Successfully refreshed tokens. AccessToken: {}, RefreshToken: {}", newAccessToken, newRefreshToken);
                return true;
            } else {
                handleError(httpResponse);
                log.error("Failed to refresh token - Status: {}, Body: {}", statusCode, httpResponse.body());
                return false;
            }
        } catch (IOException | InterruptedException e) {
            log.error("Exception while refreshing token: {}", e.getMessage(), e);
            return false;
        }
    }
    public boolean deleteReceiptType(String receiptTypeId) {
        try {
            // Send DELETE request
            ApiResponse<Void> responseWrapper = ApiClient.delete(
                    "/receipt-types/" + receiptTypeId,
                    null,
                    new TypeReference<>() {}
            );

            HttpResponse<String> httpResponse = responseWrapper.getHttpResponse();
            int statusCode = httpResponse.statusCode();

            return statusCode == 200;
        } catch (Exception e) {
            log.error("Failed to delete receipt type: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to delete receipt type.");
        }
    }

    // ------------------------------------------------------
    // PRIVATE HELPER METHODS
    // ------------------------------------------------------

    @Data
    public static class ErrorResponse {
        private String response;
    }

    private void handleError(HttpResponse<String> httpResponse) {
        String responseBody = httpResponse.body();
        int statusCode = httpResponse.statusCode();

        try {
            // Deserialize the error response
            ObjectMapper objectMapper = ApiClient.getInstance().getObjectMapper();
            Map<String, Object> errorMap = objectMapper.readValue(responseBody, new TypeReference<Map<String, Object>>() {});
            Object errorResponse = errorMap.get("response");
            String errorMessage = errorResponse != null ? errorResponse.toString() : "An error occurred (HTTP " + statusCode + ").";

            // Show the error message
            throw new RuntimeException( errorMessage);

        } catch (Exception e) {
            // If deserialization fails, show a generic error message
            throw new RuntimeException( "An error occurred.");
        }
    }
    
    


}
