ALTER TABLE candidate_registration
    ADD COLUMN status_changed_at TIMESTAMP WITH TIME ZONE;

ALTER TABLE candidate_registration
    ADD COLUMN status_changed_by VARCHAR(100);

ALTER TABLE candidate_registration
    ADD COLUMN version BIGINT NOT NULL DEFAULT 0;

UPDATE candidate_registration
SET status_changed_at = registered_at
WHERE status_changed_at IS NULL;

ALTER TABLE candidate_registration
    ALTER COLUMN status_changed_at SET NOT NULL;
