CREATE TABLE IF NOT EXISTS application
(
    id                BIGINT AUTO_INCREMENT PRIMARY KEY,
    name              VARCHAR(255)  NOT NULL,
    photo             VARCHAR(3000) NOT NULL,
    student_id        BIGINT        NOT NULL,
    major             VARCHAR(25)   NOT NULL,
    phone_number      VARCHAR(25)   NOT NULL,
    self_introduction TEXT          NOT NULL,
    apply_reason      TEXT          NOT NULL,
    skill_evaluation  TEXT          NOT NULL,
    golf_memory       TEXT          NOT NULL,
    other_club        TEXT          NOT NULL,
    swing_video       VARCHAR(3000) NOT NULL,
    submit_time       DATETIME      NOT NULL,
    document_pass     BIT(1)        NULL,
    final_pass        BIT(1)        NULL,
    interview_time    DATETIME      NULL,
    email             VARCHAR(255)  NULL,
    birth_date        DATE          NULL
);

CREATE TABLE IF NOT EXISTS board
(
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    title      VARCHAR(255) NOT NULL,
    content    TEXT         NOT NULL,
    created_at DATETIME     NULL,
    category   VARCHAR(255) NULL,
    user_id    BIGINT       NULL,
    deleted    BIT(1)       NULL
);

CREATE TABLE IF NOT EXISTS board_template
(
    id       BIGINT AUTO_INCREMENT PRIMARY KEY,
    title    VARCHAR(255) NOT NULL,
    contents TEXT         NOT NULL
);

CREATE TABLE IF NOT EXISTS coupon
(
    id       BIGINT AUTO_INCREMENT PRIMARY KEY,
    quantity BIGINT       NOT NULL,
    type     VARCHAR(255) NOT NULL
);

CREATE TABLE IF NOT EXISTS email_alarm
(
    id    BIGINT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(255) NOT NULL
);

CREATE TABLE IF NOT EXISTS image
(
    id       BIGINT AUTO_INCREMENT PRIMARY KEY,
    url      VARCHAR(3000) NOT NULL,
    board_id BIGINT        NOT NULL
);

CREATE TABLE IF NOT EXISTS recruitment_period
(
    id                   BIGINT AUTO_INCREMENT PRIMARY KEY,
    start_date           DATE NOT NULL,
    end_date             DATE NOT NULL,
    first_result_date    DATE NOT NULL,
    interview_start_date DATE NOT NULL,
    interview_end_date   DATE NOT NULL,
    final_result_date    DATE NOT NULL,
    orientation_date     DATE NOT NULL
);

CREATE TABLE IF NOT EXISTS reply
(
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    content    TEXT     NOT NULL,
    board_id   BIGINT   NOT NULL,
    user_id    BIGINT   NOT NULL,
    created_at DATETIME NOT NULL
);

CREATE TABLE IF NOT EXISTS user
(
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    kakao_id     BIGINT       NOT NULL,
    name         VARCHAR(255) NOT NULL,
    phone_number VARCHAR(255) NOT NULL,
    student_id   INT          NOT NULL,
    major        VARCHAR(255) NOT NULL,
    semester     INT          NOT NULL,
    role         VARCHAR(255) NOT NULL,
    user_class   VARCHAR(255) NOT NULL
);

CREATE TABLE IF NOT EXISTS user_coupon
(
    id        BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id   BIGINT NOT NULL,
    coupon_id BIGINT NOT NULL
);
