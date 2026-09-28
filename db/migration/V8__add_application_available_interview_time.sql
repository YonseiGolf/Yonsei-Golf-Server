CREATE TABLE application_available_interview_time (
    application_id BIGINT NOT NULL,
    interview_time_id BIGINT NOT NULL,
    PRIMARY KEY (application_id, interview_time_id),
    FOREIGN KEY (application_id) REFERENCES application(id),
    FOREIGN KEY (interview_time_id) REFERENCES interview_time(id)
);
