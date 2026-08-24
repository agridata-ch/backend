-- Guarantees that a BUR consent request cannot have overlapping ownership periods for the same
-- (data_request_id, data_producer_uid, data_producer_bur): the [uid_bur_relation_since, uid_bur_relation_until)
-- ranges of active rows must not overlap.
CREATE EXTENSION IF NOT EXISTS btree_gist;

ALTER TABLE consent_request
    ADD CONSTRAINT ex_consent_request_bur_no_overlap
    EXCLUDE USING gist (
        data_request_id WITH =,
        data_producer_uid WITH =,
        data_producer_bur WITH =,
        tsrange(uid_bur_relation_since, uid_bur_relation_until) WITH &&
    )
    WHERE (archived = false AND data_producer_bur IS NOT NULL);
