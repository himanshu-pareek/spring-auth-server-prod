# OAuth2 Authorization Server

## Features

* Created using Spring Authorization Server

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
