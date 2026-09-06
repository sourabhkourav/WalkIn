ALTER TABLE application_user
    DROP CONSTRAINT ck_application_user_role;

UPDATE application_user
SET role = 'PLATFORM_ADMIN'
WHERE role = 'ADMIN';

ALTER TABLE application_user
    ADD COLUMN company_id INTEGER REFERENCES company(company_id);

-- Legacy recruiter accounts cannot be assigned safely without knowing their company.
-- Keep them for audit/history, but prevent login until a platform admin assigns them.
UPDATE application_user
SET enabled = FALSE
WHERE role = 'RECRUITER' AND company_id IS NULL;

ALTER TABLE application_user
    ADD CONSTRAINT ck_application_user_role
        CHECK (role IN ('PLATFORM_ADMIN', 'COMPANY_ADMIN', 'RECRUITER'));

ALTER TABLE application_user
    ADD CONSTRAINT ck_application_user_company
        CHECK (
            (role = 'PLATFORM_ADMIN' AND company_id IS NULL)
            OR (role IN ('COMPANY_ADMIN', 'RECRUITER') AND company_id IS NOT NULL)
            OR (enabled = FALSE AND company_id IS NULL)
        );

CREATE INDEX idx_application_user_company ON application_user(company_id);
