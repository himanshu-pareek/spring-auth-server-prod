ALTER TABLE oauth2_registered_client
    ADD CONSTRAINT oauth2_registered_client_client_id_key UNIQUE (client_id);
