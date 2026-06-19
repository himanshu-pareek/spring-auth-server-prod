package dev.javarush.youtube.auth_server.client;

import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface ClientEntityRepository extends CrudRepository<ClientEntity, String> {
    Optional<ClientEntity> findByClientId(String clientId);

    default void upsert(ClientEntity entity) {
        assert entity.getId() != null;
        if (this.existsById(entity.getId())) {
            entity.markOld();
        }
        this.save(entity);
    }
}
