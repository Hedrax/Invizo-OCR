package com.example.ocrdesktop.utils;

import com.example.ocrdesktop.AppContext;
import com.example.ocrdesktop.control.NavigationManager;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import javafx.application.Platform;
import javafx.scene.control.Alert;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class ApiClient {

    private static final ApiClient INSTANCE = new ApiClient();
    private static final String BASE_URL = "http://invizo-app-env.eba-3rc9msxb.us-east-2.elasticbeanstalk.com"; // Adjust as needed
    private static final int MAX_RETRIES = 3;
    private static final Duration RETRY_DELAY = Duration.ofSeconds(2);

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    private ApiClient() {
        this.httpClient = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_2)
                .connectTimeout(Duration.ofSeconds(10))
                .build();

        this.objectMapper = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    public static ApiClient getInstance() {
        return INSTANCE;
    }


    public <T> ApiResponse<T> sendRequestSync(
            String method,
            String endpoint,
            Object requestBody,
            TypeReference<T> typeReference
    ) throws IOException, InterruptedException {
        AtomicInteger retryCount = new AtomicInteger(0);
        boolean tokenRefreshed = false;

        while (retryCount.get() <= MAX_RETRIES) {
            HttpRequest request = buildRequest(method, endpoint, requestBody);

            try {
                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
                int statusCode = response.statusCode();

                // Handle unauthorized responses
                if (statusCode == 401) {
                    if (!tokenRefreshed) {
                        if (handleTokenRefresh()) {
                            tokenRefreshed = true;
                            retryCount.decrementAndGet(); // Reset retry count after successful refresh
                            continue; // Retry with new token
                        } else {
                            handleLogout();
                            throw new RuntimeException("Session expired. Please login again.");
                        }
                    } else {
                        handleLogout();
                        throw new RuntimeException("Authorization failed. Insufficient permissions.");
                    }
                }

                // Handle forbidden responses (role-based access)
                if (statusCode == 403) {
                    Platform.runLater(() -> showErrorAlert("Permission Denied",
                            "You don't have permission to perform this action."));
                    throw new RuntimeException("Insufficient permissions");
                }

                // Handle successful responses
                T body = null;
                if (statusCode != 204 && !response.body().isEmpty()) {
                    body = deserialize(response.body(), typeReference);
                }
                return new ApiResponse<>(body, response);

            } catch (IOException | InterruptedException e) {
                if (retryCount.incrementAndGet() > MAX_RETRIES) {
                    handleLogout();
                    throw e;
                }
                System.err.println("Network error occurred, retrying... (" + retryCount.get() + ")");
                Thread.sleep(RETRY_DELAY.toMillis());
            }
        }
        handleLogout();
        throw new RuntimeException("Exceeded maximum retry attempts.");
    }

    private synchronized boolean handleTokenRefresh() {
        try {
            AuthorizationInfo authInfo = AppContext.getInstance().getAuthorizationInfo();
            if (authInfo == null || authInfo.getRefreshToken() == null) {
                return false;
            }

            // Call refresh endpoint
            Map<String, String> payload = new HashMap<>();
            payload.put("refreshToken", authInfo.getRefreshToken());

            ApiResponse<Map<String, Object>> response = ApiClient.post(
                    "/auth/refresh",
                    payload,
                    new TypeReference<>() {}
            );

            if (response.getHttpResponse().statusCode() == 200) {
                Map<String, Object> body = response.getBody();
                String newAccessToken = (String) body.get("accessToken");
                String newRefreshToken = (String) body.get("refreshToken");

                // Update auth context
                authInfo.setAccessToken(newAccessToken);
                authInfo.setRefreshToken(newRefreshToken);
                AppContext.getInstance().setAuthorizationInfo(authInfo);

                return true;
            }
        } catch (Exception e) {
            System.err.println("Token refresh failed: " + e.getMessage());
        }
        return false;
    }

    private void handleLogout() {
        Platform.runLater(() -> {
            try {
                NavigationManager.getInstance().logout();
                showErrorAlert("Session Expired", "Your session has expired. Please login again.");

            } catch (IOException e) {
                throw new RuntimeException(e);
            }

        });
    }

    private HttpRequest buildRequest(String method, String endpoint, Object requestBody) {
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + endpoint))
                .timeout(Duration.ofSeconds(10));

        // Add authorization header if needed
        if (!endpoint.equals("/auth/login") && !endpoint.equals("/auth/register")) {
            String token = AppContext.getInstance().getAuthorizationInfo().getAccessToken();
            if (token != null) {
                builder.header("Authorization", "Bearer " + token);
            }
        }

        // Set request method and body
        switch (method.toUpperCase()) {
            case "GET":
                builder.GET();
                break;
            case "POST": {
                String json = serialize(requestBody);
                builder.POST(HttpRequest.BodyPublishers.ofString(json))
                        .header("Content-Type", "application/json");
                break;
            }
            case "PUT": {
                String json = serialize(requestBody);
                builder.PUT(HttpRequest.BodyPublishers.ofString(json))
                        .header("Content-Type", "application/json");
                break;
            }
            case "DELETE": {
                if (requestBody != null) {
                    String json = serialize(requestBody);
                    builder.method("DELETE", HttpRequest.BodyPublishers.ofString(json))
                            .header("Content-Type", "application/json");
                } else {
                    builder.DELETE();
                }
                break;
            }
            default:
                throw new IllegalArgumentException("Unsupported HTTP method: " + method);
        }

        return builder.build();
    }

    private void showErrorAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private <T> T deserialize(String json, TypeReference<T> typeReference) {
        try {
            return objectMapper.readValue(json, typeReference);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to deserialize JSON response", e);
        }
    }

    private String serialize(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            System.err.println("Failed to serialize request body: " + e.getMessage());
            throw new RuntimeException("Failed to serialize request body", e);
        }
    }



    // Convenience methods for HTTP verbs returning ApiResponse

    public static <T> ApiResponse<T> get(String endpoint, TypeReference<T> typeReference) throws IOException, InterruptedException {
        return getInstance().sendRequestSync("GET", endpoint, null, typeReference);
    }

    public static <T> ApiResponse<T> post(String endpoint, Object requestBody, TypeReference<T> typeReference) throws IOException, InterruptedException {
        return getInstance().sendRequestSync("POST", endpoint, requestBody, typeReference);
    }

    public static <T> ApiResponse<T> put(String endpoint, Object requestBody, TypeReference<T> typeReference) throws IOException, InterruptedException {
        return getInstance().sendRequestSync("PUT", endpoint, requestBody, typeReference);
    }

    public static <T> ApiResponse<T> delete(String endpoint, TypeReference<T> typeReference) throws IOException, InterruptedException {
        return getInstance().sendRequestSync("DELETE", endpoint, null, typeReference);
    }

    public static <T> ApiResponse<T> delete(String endpoint, Object requestBody, TypeReference<T> typeReference) throws IOException, InterruptedException {
        return getInstance().sendRequestSync("DELETE", endpoint, requestBody, typeReference);
    }

    public ObjectMapper getObjectMapper() {
        return objectMapper;
    }
}
