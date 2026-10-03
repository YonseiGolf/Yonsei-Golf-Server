-- Mail wording an admin changed. A type without a row is sent with the default wording in code.
CREATE TABLE mail_template
(
    id      BIGINT PRIMARY KEY AUTO_INCREMENT,
    type    VARCHAR(64)  NOT NULL,
    subject VARCHAR(255) NOT NULL,
    body    TEXT         NOT NULL,
    CONSTRAINT uk_mail_template_type UNIQUE (type)
);
