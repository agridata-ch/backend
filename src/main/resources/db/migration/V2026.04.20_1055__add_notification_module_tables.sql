-- =============================================
-- Notification module tables
-- =============================================

-- ENUMs
CREATE TYPE notification_batch_status_code AS ENUM ('PENDING', 'IN_PROGRESS', 'COMPLETE', 'FAILED', 'PARTIALLY_FAILED');
CREATE TYPE notification_channel_code AS ENUM ('EMAIL', 'MOBILE');
CREATE TYPE notification_dispatch_status_code AS ENUM ('SENT', 'FAILED');

-- 1) notification_template
CREATE TABLE IF NOT EXISTS notification_template
(
    id                            UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    archived                      BOOLEAN                     NOT NULL DEFAULT false,
    created_by                    UUID                        NULL,
    created_at                    TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    modified_by                   UUID                        NULL,
    modified_at                   TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    event_type_code               VARCHAR(50)                 NOT NULL,
    template_version              INT                         NOT NULL DEFAULT 1,
    email_subject                 JSONB                       NULL,
    email_text                    JSONB                       NULL,
    webapp_text                   JSONB                       NULL,
    mobile_text                   JSONB                       NULL,
    required_generic_placeholders JSONB                       NULL
);

CREATE INDEX IF NOT EXISTS idx_notification_template_event_type_code ON notification_template (event_type_code);
CREATE INDEX IF NOT EXISTS idx_notification_template_archived ON notification_template (archived);

-- 2) notification_batch
CREATE TABLE IF NOT EXISTS notification_batch
(
    id                   UUID PRIMARY KEY                   DEFAULT gen_random_uuid(),
    archived             BOOLEAN                            NOT NULL DEFAULT false,
    created_by           UUID                               NULL,
    created_at           TIMESTAMP WITHOUT TIME ZONE        NOT NULL,
    modified_by          UUID                               NULL,
    modified_at          TIMESTAMP WITHOUT TIME ZONE        NOT NULL,
    template_id          UUID                               NOT NULL,
    generic_placeholders JSONB                              NULL,
    status_code          notification_batch_status_code     NOT NULL DEFAULT 'PENDING'
);

ALTER TABLE notification_batch
    ADD CONSTRAINT fk_notification_batch_template
        FOREIGN KEY (template_id) REFERENCES notification_template (id);

CREATE INDEX IF NOT EXISTS idx_notification_batch_template_id ON notification_batch (template_id);
CREATE INDEX IF NOT EXISTS idx_notification_batch_status_code ON notification_batch (status_code);
CREATE INDEX IF NOT EXISTS idx_notification_batch_archived ON notification_batch (archived);

-- 3) notification_recipient
CREATE TABLE IF NOT EXISTS notification_recipient
(
    id         UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    archived   BOOLEAN                     NOT NULL DEFAULT false,
    created_by UUID                        NULL,
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    modified_by UUID                       NULL,
    modified_at TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    batch_id   UUID                        NOT NULL,
    user_id    UUID                        NULL,
    email      VARCHAR(255)                NULL
);

ALTER TABLE notification_recipient
    ADD CONSTRAINT fk_notification_recipient_batch
        FOREIGN KEY (batch_id) REFERENCES notification_batch (id);

CREATE INDEX IF NOT EXISTS idx_notification_recipient_batch_id ON notification_recipient (batch_id);
CREATE INDEX IF NOT EXISTS idx_notification_recipient_user_id ON notification_recipient (user_id);
CREATE INDEX IF NOT EXISTS idx_notification_recipient_archived ON notification_recipient (archived);

-- 4) notification_inbox
CREATE TABLE IF NOT EXISTS notification_inbox
(
    id           UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    archived     BOOLEAN                     NOT NULL DEFAULT false,
    created_by   UUID                        NULL,
    created_at   TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    modified_by  UUID                        NULL,
    modified_at  TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    recipient_id UUID                        NOT NULL,
    user_id      UUID                        NOT NULL,
    is_read      BOOLEAN                     NOT NULL DEFAULT false
);

ALTER TABLE notification_inbox
    ADD CONSTRAINT fk_notification_inbox_recipient
        FOREIGN KEY (recipient_id) REFERENCES notification_recipient (id);

ALTER TABLE notification_inbox
    ADD CONSTRAINT uq_notification_inbox_recipient_id UNIQUE (recipient_id);

CREATE INDEX IF NOT EXISTS idx_notification_inbox_recipient_id ON notification_inbox (recipient_id);
CREATE INDEX IF NOT EXISTS idx_notification_inbox_user_id ON notification_inbox (user_id);
CREATE INDEX IF NOT EXISTS idx_notification_inbox_is_read ON notification_inbox (is_read);
CREATE INDEX IF NOT EXISTS idx_notification_inbox_archived ON notification_inbox (archived);

-- 5) notification_dispatch
CREATE TABLE IF NOT EXISTS notification_dispatch
(
    id           UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    archived     BOOLEAN                              NOT NULL DEFAULT false,
    created_by   UUID                                 NULL,
    created_at   TIMESTAMP WITHOUT TIME ZONE          NOT NULL,
    modified_by  UUID                                 NULL,
    modified_at  TIMESTAMP WITHOUT TIME ZONE          NOT NULL,
    recipient_id UUID                                 NOT NULL,
    channel_code notification_channel_code            NOT NULL,
    status_code  notification_dispatch_status_code    NOT NULL,
    error        VARCHAR(1000)                        NULL
);

ALTER TABLE notification_dispatch
    ADD CONSTRAINT fk_notification_dispatch_recipient
        FOREIGN KEY (recipient_id) REFERENCES notification_recipient (id);

CREATE INDEX IF NOT EXISTS idx_notification_dispatch_recipient_id ON notification_dispatch (recipient_id);
CREATE INDEX IF NOT EXISTS idx_notification_dispatch_status_code ON notification_dispatch (status_code);
CREATE INDEX IF NOT EXISTS idx_notification_dispatch_archived ON notification_dispatch (archived);

insert into users (id, archived, created_at, modified_at, given_name)
values ('9fc651e8-def0-4456-a8d4-7d4c9d3dfc04', false, NOW(), NOW(), 'SYSTEM:NOTIFICATION_QUEUE_WORKER');
