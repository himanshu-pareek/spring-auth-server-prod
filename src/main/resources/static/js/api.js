// Ref: https://docs.spring.io/spring-security/reference/servlet/authentication/passkeys.html
const AUTH_OPTIONS_PATH = '/webauthn/authenticate/options'
const AUTH_VERIFY_PATH = '/login/webauthn'

export const getAuthenticationOptions = async (headers) => {
    const response = await fetch(AUTH_OPTIONS_PATH, {
        method: 'POST',
        headers: {
            ...headers,
            'Content-Type': 'application/json',
        },
    })

    if (!response.ok) {
        throw new Error('Failed to get authentication options from server')
    }

    return await response.json()
}

export const verifyCredential = async (verificationPayload, headers) => {
    const response = await fetch(AUTH_VERIFY_PATH, {
        method: 'POST',
        headers: {
            ...headers,
            'Content-Type': 'application/json',
        },
        body: JSON.stringify(verificationPayload)
    })

    if (!response.ok) {
        throw new Error(`Authentication failed with status ${response.status}`)
    }

    // Attempt to parse JSON, or return empty object if plain text
    try {
        return await response.json();
    } catch (e) {
        return { authenticated: response.status === 200 };
    }
}
