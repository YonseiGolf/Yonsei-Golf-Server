CREATE TABLE club_activity
(
    id             BIGINT PRIMARY KEY AUTO_INCREMENT,
    application_id BIGINT,
    club_name      VARCHAR(60),
    start_date     DATE,
    end_date       DATE,
    role           VARCHAR(1000)
);

ALTER TABLE application DROP COLUMN other_club;
