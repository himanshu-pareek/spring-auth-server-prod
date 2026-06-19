package dev.javarush.youtube.auth_server.seeding;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.provisioning.UserDetailsManager;
import org.springframework.stereotype.Component;

@Component
@Profile("seeding")
public class UserSeeder implements CommandLineRunner {
    private final UserDetailsManager userDetailsManager;

    public UserSeeder(UserDetailsManager userDetailsManager) {
        this.userDetailsManager = userDetailsManager;
    }

    @Override
    public void run(String... args) throws Exception {
        if (!this.userDetailsManager.userExists("alice")) {
            UserDetails alice = User.withUsername("alice")
                    .password("passAlice")
                    .roles("USER", "ADMIN")
                    .build();
            this.userDetailsManager.createUser(alice);
        }
        if (!this.userDetailsManager.userExists("bob")) {
            UserDetails bob = User.withUsername("bob")
                    .password("passBob")
                    .roles("USER")
                    .build();
            this.userDetailsManager.createUser(bob);
        }
    }
}
