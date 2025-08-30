create table club_activity(
    id bigint primary key auto_increment,
    application_id bigint,
    club_name varchar(60),
    start_date datetime(6),
    end_date datetime(6),
    role varchar(1000)
);

alter table application drop column other_club;
