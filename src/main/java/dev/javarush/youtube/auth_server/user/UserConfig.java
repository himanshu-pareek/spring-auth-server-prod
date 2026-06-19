package dev.javarush.youtube.auth_server.user;

import org.springframework.context.annotation.Bean;
import org.springframework.security.provisioning.JdbcUserDetailsManager;
import org.springframework.security.provisioning.UserDetailsManager;

import javax.sql.DataSource;

public class UserConfig {
    @Bean
    UserDetailsManager userDetailsManager(DataSource dataSource) {
        var manager = new JdbcUserDetailsManager(dataSource);
        manager.setEnableUpdatePassword(true);
        return manager;
    }
}
