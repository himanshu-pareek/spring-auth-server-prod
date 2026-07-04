import { authenticateWithPassKey } from "./webauthn.js"

document.addEventListener("DOMContentLoaded", () => {
    const passKeySignInForm = document.getElementById("passkey-login-form")

    if (!window.PublicKeyCredential) {
        passKeySignInForm.remove()
    }

    passKeySignInForm.addEventListener("submit", async (event) => {
        event.preventDefault()

        const formData = new FormData(event.target)
        const formEntries = Object.fromEntries(formData)

        try {
            if (!window.PublicKeyCredential) {
                window.alert('WebAuthn is not supported')
                return
            }

            const result = await authenticateWithPassKey({
                'X-CSRF-TOKEN': formEntries['_csrf']
            })

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
            window.alert('Error occurred: ' + error.message)
        }
    })
})
