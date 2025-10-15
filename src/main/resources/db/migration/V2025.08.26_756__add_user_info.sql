ALTER TABLE users
    ADD address_country VARCHAR(50);

ALTER TABLE users
    ADD address_locality VARCHAR(500);

ALTER TABLE users
    ADD address_postal_code VARCHAR(50);

ALTER TABLE users
    ADD address_street VARCHAR(500);

ALTER TABLE users
    ADD family_name VARCHAR(500);

ALTER TABLE users
    ADD given_name VARCHAR(500);

ALTER TABLE users
    ADD phone_number VARCHAR(50);

ALTER TABLE users
    ADD last_login_date TIMESTAMP WITHOUT TIME ZONE;

ALTER TABLE users
    DROP COLUMN name;

ALTER TABLE users
    DROP COLUMN uid;
