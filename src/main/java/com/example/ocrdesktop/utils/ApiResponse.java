package com.example.ocrdesktop.utils;

import lombok.Getter;

import java.net.http.HttpResponse;

@Getter
public class ApiResponse<T> {
    /**
     * -- GETTER --
     *  Retrieves the deserialized response body.
     *
     * @return Deserialized response of type T.
     */
    private final T body;
    /**
     * -- GETTER --
     *  Retrieves the full HttpResponse object.
     *
     * @return HttpResponse containing status code, headers, and body as String.
     */
    private final HttpResponse<String> httpResponse;

    public ApiResponse(T body, HttpResponse<String> httpResponse) {
        this.body = body;
        this.httpResponse = httpResponse;
    }

}
