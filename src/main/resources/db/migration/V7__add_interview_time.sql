CREATE TABLE interview_time (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    recruitment_period_id BIGINT,
    interview_date_time DATETIME,
    FOREIGN KEY (recruitment_period_id) REFERENCES recruitment_period(id)
);
