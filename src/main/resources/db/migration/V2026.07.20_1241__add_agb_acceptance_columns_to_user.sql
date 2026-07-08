ALTER TABLE users
    ADD COLUMN last_accepted_agb_date TIMESTAMP WITHOUT TIME ZONE,
    ADD COLUMN last_accepted_agb_revision_id UUID,
    ADD CONSTRAINT fk_users_on_last_accepted_agb_revision
        FOREIGN KEY (last_accepted_agb_revision_id)
        REFERENCES agb_revision (id);