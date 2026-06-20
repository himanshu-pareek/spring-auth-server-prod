package dev.javarush.youtube.auth_server.authentication;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jspecify.annotations.NonNull;
import org.springframework.security.authentication.ott.OneTimeToken;
import org.springframework.security.web.authentication.ott.OneTimeTokenGenerationSuccessHandler;
import org.springframework.security.web.util.UrlUtils;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;

public class ConsoleOTTGenerationSuccessHandler implements OneTimeTokenGenerationSuccessHandler {
    @Override
    public void handle(
            @NonNull HttpServletRequest request,
            HttpServletResponse response,
            OneTimeToken oneTimeToken
    ) throws IOException {
        String magicLink = UriComponentsBuilder.fromUriString(UrlUtils.buildFullRequestUrl(request))
                .replacePath(request.getContextPath())
                .replaceQuery(null)
                .fragment(null)
                .path("auth/login/ott")
                .queryParam("token", oneTimeToken.getTokenValue())
                .toUriString();

        // For local testing, print the link to the console.
        // In production, use JavaMailSender to email this link to the requested user.
        System.out.println("\n=== MAGIC LINK GENERATED ===");
        System.out.println(magicLink);
        System.out.println("============================\n");

        // Redirect to a page where user can enter the OTT
        response.sendRedirect("/auth/login/ott");
    }
}
