create table if not exists application
(
    id                bigint auto_increment
        primary key,
    name              varchar(255)  not null,
    photo             varchar(3000) not null,
    student_id        int           not null,
    major             varchar(25)   not null,
    phone_number      varchar(25)   not null,
    self_introduction text          not null,
    apply_reason      text          not null,
    skill_evaluation  text          not null,
    golf_memory       text          not null,
    other_club        text          not null,
    swing_video       varchar(3000) not null,
    submit_time       datetime      not null,
    document_pass     tinyint       null,
    final_pass        tinyint       null,
    interview_time    datetime      null,
    email             varchar(255)  null,
    birth_date        date          null
);

create table if not exists board
(
    id         bigint auto_increment
        primary key,
    title      varchar(255) not null,
    content    text         not null,
    created_at datetime     null,
    category   varchar(255) null,
    user_id    bigint       null,
    deleted    tinyint      null
);

create table if not exists board_template
(
    id       bigint auto_increment
        primary key,
    title    varchar(255) not null,
    contents text         not null
);

create table if not exists coupon
(
    id       bigint auto_increment
        primary key,
    quantity bigint       not null,
    type     varchar(255) not null
);

create table if not exists email_alarm
(
    id    bigint auto_increment
        primary key,
    email varchar(255) not null
);

create table if not exists image
(
    id       bigint auto_increment
        primary key,
    url      varchar(3000) not null,
    board_id bigint        not null
);

create table if not exists recruitment_period
(
    id                   bigint auto_increment
        primary key,
    start_date           datetime not null,
    end_date             datetime not null,
    first_result_date    datetime not null,
    interview_start_date datetime not null,
    interview_end_date   datetime not null,
    final_result_date    datetime not null,
    orientation_date     datetime not null
);

create table if not exists reply
(
    id         bigint auto_increment
        primary key,
    content    text     not null,
    board_id   bigint   not null,
    user_id    bigint   not null,
    created_at datetime not null
);

create table if not exists user
(
    id           bigint auto_increment
        primary key,
    kakao_id     bigint       not null,
    name         varchar(255) not null,
    phone_number varchar(255) not null,
    student_id   int          not null,
    major        varchar(255) not null,
    semester     int          not null,
    role         varchar(255) not null,
    user_class   varchar(255) not null
);

create table if not exists user_coupon
(
    id        bigint auto_increment
        primary key,
    user_id   bigint not null,
    coupon_id bigint not null
);

