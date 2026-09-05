ALTER TABLE hiring_drive
    ADD COLUMN first_name_requirement VARCHAR(20) NOT NULL DEFAULT 'REQUIRED';
ALTER TABLE hiring_drive
    ADD COLUMN last_name_requirement VARCHAR(20) NOT NULL DEFAULT 'REQUIRED';
ALTER TABLE hiring_drive
    ADD COLUMN email_requirement VARCHAR(20) NOT NULL DEFAULT 'REQUIRED';
ALTER TABLE hiring_drive
    ADD COLUMN contact_number_requirement VARCHAR(20) NOT NULL DEFAULT 'REQUIRED';
ALTER TABLE hiring_drive
    ADD COLUMN resume_requirement VARCHAR(20) NOT NULL DEFAULT 'HIDDEN';

ALTER TABLE hiring_drive
    ADD CONSTRAINT ck_hiring_drive_first_name_requirement
        CHECK (first_name_requirement IN ('HIDDEN', 'OPTIONAL', 'REQUIRED'));
ALTER TABLE hiring_drive
    ADD CONSTRAINT ck_hiring_drive_last_name_requirement
        CHECK (last_name_requirement IN ('HIDDEN', 'OPTIONAL', 'REQUIRED'));
ALTER TABLE hiring_drive
    ADD CONSTRAINT ck_hiring_drive_email_requirement
        CHECK (email_requirement IN ('HIDDEN', 'OPTIONAL', 'REQUIRED'));
ALTER TABLE hiring_drive
    ADD CONSTRAINT ck_hiring_drive_contact_number_requirement
        CHECK (contact_number_requirement IN ('HIDDEN', 'OPTIONAL', 'REQUIRED'));
ALTER TABLE hiring_drive
    ADD CONSTRAINT ck_hiring_drive_resume_requirement
        CHECK (resume_requirement IN ('HIDDEN', 'OPTIONAL', 'REQUIRED'));
