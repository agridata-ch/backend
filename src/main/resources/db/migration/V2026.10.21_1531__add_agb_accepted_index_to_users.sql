CREATE INDEX idx_users_agb_accepted
    ON users (uid)
    WHERE archived = false AND last_accepted_agb_date IS NOT NULL;
