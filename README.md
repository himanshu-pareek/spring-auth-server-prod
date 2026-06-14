# OAuth2 Authorization Server

## Features

* Created using Spring Authorization Server

## Core Model / Components

ref: https://docs.spring.io/spring-authorization-server/reference/core-model-components.html

* `RegisteredClient` - A client that is registered with the authorization server
  ```java
  public class RegisteredClient implements Serializable {
        private String id;
        private String clientId;
        private Instant clientIdIssuedAt;
        // The value should be encoded using Spring Security’s PasswordEncoder.
        private String clientSecret;
        private Instant clientSecretExpiresAt;
        private String clientName;
        private Set<ClientAuthenticationMethod> clientAuthenticationMethods;
        private Set<AuthorizationGrantType> authorizationGrantTypes;
        private Set<String> redirectUris;
        private Set<String> postLogoutRedirectUris;
        private Set<String> scopes;
        // Example, require PKCE?, require authorization consent?, and others
        private ClientSettings clientSettings;
        // Example, Token TTL, Access Token Type, Reuse refresh token?
        private TokenSettings tokenSettings;
        // ...
    }
    ```
* `RegisteredClientRepository` - Central component where new clients can be registered and existing clients can be queried
* `OAuth2Authorization` - A representation of an OAuth2 authorization, which holds state related to the authorization granted to a client, by the resource-owner or by the client (in case of client-credentials authorization grant flow)
  ```java
  public class OAuth2Authorization implements Serializable {
        private String id;
        private String registeredClientId;
        private String principalName;
        private AuthorizationGrantType authorizationGrantType;
        private Set<String> authorizedScopes;
        // Ex - OAuth2AuthorizationCode, AccessToken, RefreshToken, IdToken
        private Map<Class<? extends OAuth2Token>, Token<?>> tokens;
        private Map<String, Object> attributes;
        // ...
  }
  ```
* `OAuth2AuthorizationService` - The central component where new authorizations are stored and existing authorizations are queried.
  * `OAuth2AuthorizationConsent` - A representation of an authorization "consent" (decision) from an OAuth2 Authorization Request flow - for example, the `authorization_code` grant, which holds the authorities granted to a client by the resource-owner.
  ```java
  public final class OAuth2AuthorizationConsent implements Serializable {
      private final String registeredClientId;
      private final String principalName;
      private final Set<GrantedAuthority> authorities;
      // ...
    }
  ```
* `OAuth2AuthorizationService` - The central component where new authorization consents are stored and existing authorization consents are queried.
* `OAuth2TokenContext` - A context object that holds information associated with an `OAuth2Token` and is used by an `OAuth2TokenGenerator` and `OAuth2TokenCustomizer`.
  ```java
    public interface OAuth2TokenContext extends Context {
        default RegisteredClient getRegisteredClient();
        default <T extends Authentication> T getPrincipal();
        default AuthorizationServerContext getAuthorizationServerContext();
        @Nullable
        default OAuth2Authorization getAuthorization();
        default Set<String> getAuthorizedScopes();
        default OAuth2TokenType getTokenType();
        default AuthorizationGrantType getAuthorizationGrantType();
        default <T extends Authentication> T getAuthorizationGrant();
    }
    ```
* `OAuth2TokenGenerator` - Responsible for generating an `OAuth2Token` from the information contained in the provided `OAuth2TokenContext`.
* `OAuth2TokenCustomizer` - Provides the ability to customize the attributes of an `OAuth2Token`, which are accessbile in the provided `OAuth2TokenContext`.
* `SessionRegistry` - Used to track authenticated sessions, if OpenID Connect 1.0 is enabled.

## HOWTO

### Test Authorization Code Authorization Grant Flow

1. Modify the constants inside [AuthorizationCodeGrantFlow.java](./flows/AuthorizationCodeGrantFlow.java) file
2. Run the script from inside the [flows directory](./flows) using `java java AuthorizationCodeGrantFlow.java` command
3. Copy the authorization url and open it inside a browser
4. Copy the authorization code
5. Paste the authorization code inside the terminal and press Enter
6. Copy the value of `access_token` and inspect using [jwt.io](https://jwt.io) if you want in case of JWT Token

## References

1. The OAuth 2.1 Authorization Framework - https://datatracker.ietf.org/doc/html/draft-ietf-oauth-v2-1-07
2. 
