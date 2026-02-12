package com.demo.testing;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.nio.charset.StandardCharsets;

/**
 * Simple HTTP client utility for sending requests in integration tests.
 */
public class HttpTestClient {

    private HttpTestClient() {
    }

    /**
     * Sends a GET request to the given URL.
     */
    public static Response get(String url) throws IOException {
        HttpURLConnection conn = (HttpURLConnection) URI.create(url).toURL().openConnection();
        try {
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(5000);
            return buildResponse(conn);
        } finally {
            conn.disconnect();
        }
    }

    /**
     * Sends a POST request with a JSON body to the given URL.
     */
    public static Response post(String url, String jsonBody) throws IOException {
        HttpURLConnection conn = (HttpURLConnection) URI.create(url).toURL().openConnection();
        try {
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(5000);

            try (OutputStream os = conn.getOutputStream()) {
                os.write(jsonBody.getBytes(StandardCharsets.UTF_8));
            }
            return buildResponse(conn);
        } finally {
            conn.disconnect();
        }
    }

    private static Response buildResponse(HttpURLConnection conn) throws IOException {
        int status = conn.getResponseCode();
        InputStream stream = (status >= 400) ? conn.getErrorStream() : conn.getInputStream();

        String body = "";
        if (stream != null) {
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(stream, StandardCharsets.UTF_8))) {
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    sb.append(line);
                }
                body = sb.toString();
            }
        }
        return new Response(status, body);
    }

    /**
     * Simple HTTP response container with status code and body.
     */
    public static final class Response {

        private final int statusCode;
        private final String body;

        Response(int statusCode, String body) {
            this.statusCode = statusCode;
            this.body = body;
        }

        public int getStatusCode() {
            return statusCode;
        }

        public String getBody() {
            return body;
        }
    }
}
