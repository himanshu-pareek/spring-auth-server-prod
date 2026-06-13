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

public class AuthorizationCodeGrantFlow {

    private static final String authEndpoint = "http://localhost:9000/oauth2/authorize";
    private static final String tokenEndpoint = "http://localhost:9000/oauth2/token";

    private static final String clientId = "oidc-client";
    private static final String clientSecret = "secret";
    private static final String redirectUri = "https://example.net/authorized";

    private static final String responseType = "code";
    private static final String scope = "openid profile";
    private static final String grantType = "authorization_code";

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        var res = createLoginUrl();
        String loginUrl = res[0];
        String codeVerifier = res[1];
        System.out.println("Open the following url in a browser and copy the resulting authorization code\n\n" + loginUrl + "\n\n");
        System.out.print("Enter authorization code: ");
        String authorizationCode = scanner.next();
        String tokenResponse = getToken(authorizationCode, codeVerifier);
        System.out.println("\n----------\n" + tokenResponse);
    }

    private static String[] createLoginUrl() {
        var codes = createCodeChallengeAndVerifier();
        String codeChallenge = codes[1];
        String codeVerifier = codes[0];
        String loginUrl = String.format("%s?response_type=%s&client_id=%s&scope=%s&redirect_uri=%s&code_challenge=%s&code_challenge_method=S256", authEndpoint, responseType, clientId, scope, redirectUri, codeChallenge);
        return new String[]{ loginUrl, codeVerifier };
    }

    private static String getToken(String code, String verifier) {
        HttpRequest request = createGetTokenRequest(code, verifier);
        try {
            HttpResponse<String> response = HttpClient.newHttpClient()
                    .send(request, HttpResponse.BodyHandlers.ofString());
            System.out.println("Response code - " + response.statusCode());
            return response.body();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static HttpRequest createGetTokenRequest(String code, String codeVerifier) {
        Map<String, String> data = Map.of(
                "client_id", clientId,
                "redirect_uri", redirectUri,
                "grant_type", grantType,
                "code", code,
                "code_verifier", codeVerifier
        );
        String formData = data.entrySet().stream()
                .map(e -> e.getKey() + "=" + URLEncoder.encode(e.getValue(), StandardCharsets.UTF_8))
                .collect(Collectors.joining("&"));
        System.out.println(formData);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(tokenEndpoint))
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

    private static String[] createCodeChallengeAndVerifier() {
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[32];
        random.nextBytes(bytes);
        try {
            Base64.Encoder encoder = Base64.getUrlEncoder().withoutPadding();
            String codeVerifier = encoder.encodeToString(bytes);
            MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
            messageDigest.update (codeVerifier.getBytes());
            byte[] digested = messageDigest.digest();
            String codeChallenge = encoder.encodeToString(digested);
            return new String[]{ codeVerifier, codeChallenge };
        } catch (Exception e) {
            throw new RuntimeException (e);
        }
    }

}