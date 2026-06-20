package dev.javarush.youtube.auth_server.auth;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/auth/login")
public class LoginController {

    @GetMapping
    String loginPage() {
        return "auth/login";
    }
}
