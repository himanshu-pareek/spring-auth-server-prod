# OAuth2 Authorization Server

## Features

* Created using [Spring Authorization Server](https://docs.spring.io/spring-authorization-server/reference/index.html)
* Stores the users, clients, generated tokens and granted scopes in the postgresql database
* Stores the user session in Redis
* Persists user sessions, generated tokens and granted scopes across server restarts and scross multiple instances
* Provides ability to log in using Github
  * Other Identity Providers (OAuth2 Authorization Servers) can be integrated in the similar way
* Provides ability to log in using One Time Token (OTT)
* Uses custom configured pages for all kind of loging options

## Core Model / Components

[Default DB Schema](https://github.com/spring-projects/spring-security/tree/main/oauth2/oauth2-authorization-server/src/main/resources/org/springframework/security/oauth2/server/authorization) provided by Spring Security Authorization Server

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

## Productionize the Auth Server

### Persisting RSA KeyPair across restarts

1. Generate the private key
    ```shell
    # Generates a 2048-bit RSA private key directly into PKCS#8 PEM format
    openssl genpkey -algorithm RSA -out private_key.pem -pkeyopt rsa_keygen_bits:2048
    ```
2. Put private key into environment variable
   ```shell
   # A) If using kubernetes to deploy the server, then
   # A.1) Create k8s secret:
   kubectl create secret generic rsa-keys --from-file=PRIVATE_KEY_ENV=./private_key.pem
   
   # A.2) Map it into your deployment file:
   apiVersion: apps/v1
    kind: Deployment
    metadata:
    name: my-java-app
    spec:
    template:
    spec:
    containers:
    - name: my-java-container
    image: my-registry/my-java-app:latest
    env:
      - name: RSA_PRIVATE_KEY
      valueFrom:
      secretKeyRef:
      name: rsa-keys
      key: PRIVATE_KEY_ENV
   
   # B) In local, create environment variable using the content of the file:
   export RSA_PRIVATE_KEY="\"$(cat private_key.pem | tr -d '\n')\""
    ```
3. Loading it in Java (Look at the [RSAKeyConfig](./src/main/java/dev/javarush/youtube/auth_server/security/RSAKeyConfig.java) for code)
   ```java
   private static KeyPair generateRsaKey2() {
        // // 1. Read and clean the private key string from the environment
        String privateKeyContent = getPrivateKeyContent();

        try {
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");

            // 2. Decode and generate the Private Key
            PrivateKey privateKey = getPrivateKey(privateKeyContent, keyFactory);

            // 3. Derive the Public Key from the Private Key
            // Standard Java RSA private keys implement the RSAPrivateCrtKey interface
            PublicKey publicKey = getPublicKey(privateKey, keyFactory);

            return new KeyPair(publicKey, privateKey);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new RuntimeException(e);
        }
    }
    ```

## HOWTO

### Start application

1. Prepare Database Server by running the following command to sping up a PostgreSQL database in docker container or you can use any other instance as well:
   ```shell
   # This command requires some environment variables to be present
   # (mentioned in the file itself)
   # Those environment variables can be provided by creating a
   # .env file in the current directory as well following the syntax
   # of .env.template
   docker compose -f compose.yaml up -d
    ```
2. Create environment variable: Make sure all the environment variables are set (or present in `.env` file) as mentioned in the `.env.template` file.
3. Run the application

### Test Authorization Code Authorization Grant Flow

1. Modify the constants inside [AuthorizationCodeGrantFlow.java](./flows/AuthorizationCodeGrantFlow.java) file
2. Run the script from inside the [flows directory](./flows) using `java java AuthorizationCodeGrantFlow.java` command
3. Copy the authorization url and open it inside a browser
4. Copy the authorization code
5. Paste the authorization code inside the terminal and press Enter
6. Copy the value of `access_token` and inspect using [jwt.io](https://jwt.io) if you want in case of JWT Token

## References

1. The OAuth 2.1 Authorization Framework - https://datatracker.ietf.org/doc/html/draft-ietf-oauth-v2-1-07
2. Spring Authorization Server - https://docs.spring.io/spring-authorization-server/reference/index.html
