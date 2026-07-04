import { getAuthenticationOptions, verifyCredential } from './api.js'

document.addEventListener("DOMContentLoaded", async () => {
    const passKeySignInForm = document.getElementById("passkey-login-form")

    const { browserSupportsWebAuthn } = SimpleWebAuthnBrowser

    if (!browserSupportsWebAuthn()) {
        passKeySignInForm.style.display = 'none'
        return
    }

    const csrfToken = getCsrfToken(passKeySignInForm)
    if (!csrfToken) {
        passKeySignInForm.style.display = 'none'
        return
    }

    const headers = { 'X-CSRF-TOKEN': csrfToken }
    const authenticationOptions = await getAuthenticationOptions(headers)

    signInUsingPassKey(authenticationOptions, headers, true)

    passKeySignInForm.addEventListener("submit", async (event) => {
        event.preventDefault()

        if (!window.PublicKeyCredential) {
            window.alert('WebAuthn is not supported')
            return
        }

        signInUsingPassKey(authenticationOptions, false)
    })
})

const signInUsingPassKey = async (authenticationOptions, headers, autoFill) => {
    try {
        const { startAuthentication } = window.SimpleWebAuthnBrowser;

        if (!startAuthentication) {
            throw new Error('WebAuthn library not loaded properly')
        }


        const authenticationResponse = await startAuthentication({ optionsJSON: authenticationOptions, useBrowserAutofill: autoFill })

        const result = await verifyCredential(authenticationResponse, headers)

        if (result.authenticated) {
            if (result.redirectUrl) {
                window.location.href = result.redirectUrl
            } else {
                window.alert('Authentication is successful')
            }
        } else {
            window.alert('Authentication failed. Please try again.')
        }
    } catch (error) {
        console.error('Authentication error: ', error)
        if (error.name == 'NotAllowedError') {
            throw new Error('Authentication was cancelled or timed out')
        } else {
            throw new Error(`Failed to authenticate with PassKey: ${error.message}`)
        }
    }
}

const getCsrfToken = (formElement) => {
    const formData = new FormData(formElement)
    const formEntries = Object.fromEntries(formData)
    return formEntries['_csrf']
}
