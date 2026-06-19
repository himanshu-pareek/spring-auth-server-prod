package dev.javarush.youtube.auth_server.client;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.security.jackson.SecurityJacksonModules;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.jackson.OAuth2AuthorizationServerJacksonModule;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.JacksonModule;
import tools.jackson.databind.json.JsonMapper;

import java.util.List;
import java.util.Optional;

public class CustomJdbcRegisteredClientRepository implements RegisteredClientRepository {

    private final ClientEntityRepository entityRepository;
    private final ObjectMapper objectMapper;

    public CustomJdbcRegisteredClientRepository(ClientEntityRepository entityRepository) {
        this.entityRepository = entityRepository;

        ClassLoader classLoader = CustomJdbcRegisteredClientRepository.class.getClassLoader();
        List<JacksonModule> securityModules = SecurityJacksonModules.getModules(classLoader);
        this.objectMapper = JsonMapper.builder()
                .addModules(securityModules)
                .addModule(new OAuth2AuthorizationServerJacksonModule())
                .build();
    }

    @Override
    public void save(@NonNull RegisteredClient registeredClient) {
        ClientEntity clientEntity = ClientEntity.fromObject(registeredClient, this.objectMapper);
        this.entityRepository.upsert(clientEntity);
    }

    @Override
    public @Nullable RegisteredClient findById(@NonNull String id) {
        Optional<ClientEntity> clientEntity = this.entityRepository.findById(id);
        return clientEntity.map(e -> e.toObject(this.objectMapper)).orElse(null);
    }

    @Override
    public @Nullable RegisteredClient findByClientId(@NonNull String clientId) {
        Optional<ClientEntity> clientEntity = this.entityRepository.findByClientId(clientId);
        return clientEntity.map(e -> e.toObject(this.objectMapper)).orElse(null);
    }
}
