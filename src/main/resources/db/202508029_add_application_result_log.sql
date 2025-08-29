create table application_result_log
(
    id                bigint primary key auto_increment,
    application_id    bigint,
    notification_type varchar(64),
    sent_at           datetime(6)
);

alter table email_alarm
    add column semester bigint;

alter table email_alarm
    add column sent_at datetime(6);