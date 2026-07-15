CREATE TABLE agb_revision
(
    id                   UUID                        NOT NULL,
    archived             BOOLEAN                     NOT NULL,
    created_by           UUID,
    created_at           TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    modified_by          UUID,
    modified_at          TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    valid_from           TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    valid_to             TIMESTAMP WITHOUT TIME ZONE,
    enforce_consent_from TIMESTAMP WITHOUT TIME ZONE,
    version              VARCHAR(20)                 NOT NULL,
    agb_text             JSONB                       NOT NULL,
    CONSTRAINT pk_agb_revision PRIMARY KEY (id)
);

ALTER TABLE agb_revision
    ADD CONSTRAINT uc_agb_revision_validity_no_overlap
        EXCLUDE USING gist (
        tsrange(valid_from, valid_to, '[)') WITH &&
        )
        WHERE (archived = false);

CREATE UNIQUE INDEX uc_agb_revision_version
    ON agb_revision (version)
    WHERE archived = false;
