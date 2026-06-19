package dev.javarush.youtube.auth_server.client;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Table;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.settings.ClientSettings;
import org.springframework.security.oauth2.server.authorization.settings.TokenSettings;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

import java.time.Instant;
import java.util.Map;
import java.util.stream.Collectors;

@Table(name = "oauth2_registered_client")
public class ClientEntity implements Persistable<String> {
    @Id
    private String id;
    private String clientId;
    private Instant clientIdIssuedAt;
    // The value should be encoded using Spring Security’s PasswordEncoder.
    private String clientSecret;
    private Instant clientSecretExpiresAt;
    private String clientName;
    private String clientAuthenticationMethods;
    private String authorizationGrantTypes;
    private String redirectUris;
    private String postLogoutRedirectUris;
    private String scopes;
    // Example, require PKCE?, require authorization consent?, and others
    private String clientSettings;
    // Example, Token TTL, Access Token Type, Reuse refresh token?
    private String tokenSettings;

    @Transient
    private boolean isNew;

    public ClientEntity(String id, String clientId, Instant clientIdIssuedAt, String clientSecret, Instant clientSecretExpiresAt, String clientName, String clientAuthenticationMethods, String authorizationGrantTypes, String redirectUris, String postLogoutRedirectUris, String scopes, String clientSettings, String tokenSettings) {
        this.id = id;
        this.clientId = clientId;
        this.clientIdIssuedAt = clientIdIssuedAt;
        this.clientSecret = clientSecret;
        this.clientSecretExpiresAt = clientSecretExpiresAt;
        this.clientName = clientName;
        this.clientAuthenticationMethods = clientAuthenticationMethods;
        this.authorizationGrantTypes = authorizationGrantTypes;
        this.redirectUris = redirectUris;
        this.postLogoutRedirectUris = postLogoutRedirectUris;
        this.scopes = scopes;
        this.clientSettings = clientSettings;
        this.tokenSettings = tokenSettings;
    }

    static ClientEntity fromObject(RegisteredClient client, ObjectMapper objectMapper) {
        ClientEntity entity;
        entity = new ClientEntity(
                client.getId(),
                client.getClientId(),
                client.getClientIdIssuedAt(),
                client.getClientSecret(),
                client.getClientSecretExpiresAt(),
                client.getClientName(),
                client.getClientAuthenticationMethods().stream().map(ClientAuthenticationMethod::getValue).collect(Collectors.joining(",")),
                client.getAuthorizationGrantTypes().stream().map(AuthorizationGrantType::getValue).collect(Collectors.joining(",")),
                String.join(",", client.getRedirectUris()),
                String.join(",", client.getPostLogoutRedirectUris()),
                String.join(",", client.getScopes()),
                objectMapper.writeValueAsString(client.getClientSettings().getSettings()),
                objectMapper.writeValueAsString(client.getTokenSettings().getSettings())
        );
        entity.isNew = true;
        return entity;
    }

    RegisteredClient toObject(ObjectMapper objectMapper) {
        RegisteredClient.Builder builder = RegisteredClient.withId(this.id)
                .clientId(this.clientId)
                .clientIdIssuedAt(this.clientIdIssuedAt)
                .clientSecret(this.clientSecret)
                .clientSecretExpiresAt(this.clientSecretExpiresAt)
                .clientName(this.clientName);
        for (String m : this.clientAuthenticationMethods.split(",")) {
            if (!m.isBlank()) {
                builder.clientAuthenticationMethod(new ClientAuthenticationMethod(m.strip()));
            }
        }

        for (String m : this.authorizationGrantTypes.split(",")) {
            if (!m.isBlank()) {
                builder.authorizationGrantType(new AuthorizationGrantType(m.strip()));
            }
        }

        for (String r : this.redirectUris.split(",")) {
            if (!r.isBlank()) {
                builder.redirectUri(r.strip());
            }
        }

        for (String p : this.postLogoutRedirectUris.split(",")) {
            if (!p.isBlank()) {
                builder.postLogoutRedirectUri(p.strip());
            }
        }

        for (String s : this.scopes.split(",")) {
            if (!s.isBlank()) {
                builder.scope(s.strip());
            }
        }

        builder.clientSettings(ClientSettings.withSettings(parseMap(this.clientSettings, objectMapper)).build());
        builder.tokenSettings(TokenSettings.withSettings(parseMap(this.tokenSettings, objectMapper)).build());

        return builder.build();
    }

    private Map<String, Object> parseMap(String data, ObjectMapper objectMapper) {
        try {
            return objectMapper.readValue(data, new TypeReference<>() {});
        } catch (Exception ex) {
            throw new IllegalArgumentException(ex.getMessage(), ex);
        }
    }

    @Override
    public String toString() {
        return "ClientEntity{" +
                "id='" + id + '\'' +
                ", clientId='" + clientId + '\'' +
                ", clientIdIssuedAt=" + clientIdIssuedAt +
                ", clientSecret='" + clientSecret + '\'' +
                ", clientSecretExpiresAt=" + clientSecretExpiresAt +
                ", clientName='" + clientName + '\'' +
                ", clientAuthenticationMethods='" + clientAuthenticationMethods + '\'' +
                ", authorizationGrantTypes='" + authorizationGrantTypes + '\'' +
                ", redirectUris='" + redirectUris + '\'' +
                ", postLogoutRedirectUris='" + postLogoutRedirectUris + '\'' +
                ", scopes='" + scopes + '\'' +
                ", clientSettings='" + clientSettings + '\'' +
                ", tokenSettings='" + tokenSettings + '\'' +
                '}';
    }

    @Override
    public String getId() {
        return this.id;
    }

    @Override
    public boolean isNew() {
        return this.isNew;
    }
}