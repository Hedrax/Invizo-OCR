package com.example.ocrdesktop.utils;

import com.example.ocrdesktop.AppContext;
import com.example.ocrdesktop.data.Remote;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;

public class ApiClient {

    private static final ApiClient INSTANCE = new ApiClient();
    private static final String BASE_URL = "http://localhost:8080"; // Adjust as needed
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
        while (retryCount.get() <= MAX_RETRIES) {
            HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + endpoint))
                    .timeout(Duration.ofSeconds(10));

            // Set HTTP method and body
            switch (method.toUpperCase()) {
                case "GET":
                    requestBuilder.GET();
                    break;
                case "POST":
                    String postJson = serialize(requestBody);
                    requestBuilder.POST(HttpRequest.BodyPublishers.ofString(postJson));
                    requestBuilder.header("Content-Type", "application/json");
                    break;
                case "PUT":
                    String putJson = serialize(requestBody);
                    requestBuilder.PUT(HttpRequest.BodyPublishers.ofString(putJson));
                    requestBuilder.header("Content-Type", "application/json");
                    break;
                case "DELETE":
                    if (requestBody != null) {
                        String deleteJson = serialize(requestBody);
                        requestBuilder.method("DELETE", HttpRequest.BodyPublishers.ofString(deleteJson));
                        requestBuilder.header("Content-Type", "application/json");
                    } else {
                        requestBuilder.DELETE();
                    }
                    break;
                default:
                    throw new UnsupportedOperationException("HTTP method not supported: " + method);
            }

            // Add Authorization header if needed
            if (!endpoint.equals("/auth/login") && !endpoint.equals("/auth/register")) {
                String authToken = AppContext.getInstance().getAuthorizationInfo().getAccessToken();
                if (authToken != null && !authToken.isEmpty()) {
                    requestBuilder.header("Authorization", "Bearer " + authToken);
                }
            }

            HttpRequest request = requestBuilder.build();

            try {
                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
                int statusCode = response.statusCode();
                T body = null;

                if (typeReference.getType().equals(Void.class)) {
                    return new ApiResponse<>(null, response);
                }
                body = deserialize(response.body(), typeReference);
                return new ApiResponse<>(body, response);


            } catch (IOException | InterruptedException e) {
                // Handle network-related exceptions
                if (retryCount.incrementAndGet() > MAX_RETRIES) {
                    throw e;
                }
                System.err.println("Network error occurred, retrying... (" + retryCount.get() + ")");
                Thread.sleep(RETRY_DELAY.toMillis());
            }
        }
        throw new RuntimeException("Exceeded maximum retry attempts.");
    }
    private boolean isSuccessful(int statusCode) {
        return statusCode >= 200 && statusCode < 300;
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


    private boolean handleUnauthorized() {
        try {

            boolean success = new Remote().refreshAuthToken();
            return success;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
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
