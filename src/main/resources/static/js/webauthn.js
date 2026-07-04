import { getAuthenticationOptions, verifyCredential } from './api.js'

export async function authenticateWithPassKey(headers) {
    const { startAuthentication } = window.SimpleWebAuthnBrowser;

    if (!startAuthentication) {
        throw new Error('WebAuthn library not loaded properly')
    }

    // 1. Get authentication options from the server
    const options = await getAuthenticationOptions(headers)

    try {
        // 2. Pass options to the authenticator (browser prompts user)
        const authenticationResponse = await startAuthentication(options)

        // 3. Send the response back to the server
        return await verifyCredential(authenticationResponse, headers)
    } catch (error) {
        console.error('Authentication error: ', error)
        if (error.name == 'NotAllowedError') {
            throw new Error('Authentication was cancelled or timed out')
        } else {
            throw new Error(`Failed to authenticate with PassKey: ${error.message}`)
        }
    }
}
