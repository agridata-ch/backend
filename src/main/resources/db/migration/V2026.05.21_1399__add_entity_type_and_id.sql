ALTER TABLE notification_batch
    ADD target_id UUID;

ALTER TABLE notification_batch
    ADD target_type_code VARCHAR(50);