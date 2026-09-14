CREATE TABLE application_result_log
(
    id                BIGINT PRIMARY KEY AUTO_INCREMENT,
    application_id    BIGINT,
    notification_type VARCHAR(64),
    sent_at           DATETIME(6)
);

ALTER TABLE email_alarm ADD COLUMN semester BIGINT;
ALTER TABLE email_alarm ADD COLUMN sent_at DATETIME(6);
