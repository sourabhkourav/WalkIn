ALTER TABLE student
    ADD COLUMN notification_channel VARCHAR(20) NOT NULL DEFAULT 'SMS';

ALTER TABLE student
    ADD COLUMN advance_notice_minutes INTEGER NOT NULL DEFAULT 30;

ALTER TABLE student
    ADD CONSTRAINT ck_student_notification_channel
        CHECK (notification_channel IN ('SMS', 'EMAIL', 'WHATSAPP'));

ALTER TABLE student
    ADD CONSTRAINT ck_student_advance_notice_minutes
        CHECK (advance_notice_minutes BETWEEN 5 AND 240);
