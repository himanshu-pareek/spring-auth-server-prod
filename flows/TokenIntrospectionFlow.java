import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpClient;

import java.nio.charset.StandardCharsets;

import java.util.stream.Collectors;
import java.util.Scanner;
import java.util.Base64;
import java.util.Map;
import java.security.MessageDigest;
import java.security.SecureRandom;

public class TokenIntrospectionFlow {

    private static final String introspectionEndpoint = "http://localhost:9000/oauth2/introspect";

    private static final String clientId = "client1";
    private static final String clientSecret = "supersecret";

    public static void main(String[] args) {
        if (args.length < 1) {
            System.err.println("Please provide token to introspect");
            System.exit(1);
        }
        String token = args[0];
        String introspectionResponse = introspectToken(token);
        System.out.println("\n----------\n" + introspectionResponse);
    }

    private static String introspectToken(String token) {
        HttpRequest request = createIntrospectTokenRequest(token);
        try {
            HttpResponse<String> response = HttpClient.newHttpClient()
                    .send(request, HttpResponse.BodyHandlers.ofString());
            System.out.println("Response code - " + response.statusCode());
            return response.body();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static HttpRequest createIntrospectTokenRequest(String token) {
        Map<String, String> data = Map.of(
                "token", token
        );
        String formData = data.entrySet().stream()
                .map(e -> e.getKey() + "=" + URLEncoder.encode(e.getValue(), StandardCharsets.UTF_8))
                .collect(Collectors.joining("&"));
        System.out.println(formData);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(introspectionEndpoint))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .header("Authorization", getClientAuthHeaderValue())
                .POST(HttpRequest.BodyPublishers.ofString(formData))
                .build();
        return request;
    }

    private static String getClientAuthHeaderValue() {
        String clientIdAndSecret = clientId + ":" + clientSecret;
        Base64.Encoder encoder = Base64.getEncoder();
        return "Basic " + encoder.encodeToString(clientIdAndSecret.getBytes());
    }
}