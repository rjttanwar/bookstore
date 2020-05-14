--liquibase formatted sql

--changeset rjttanwar:1
create table if not exists book (
       id bigint not null auto_increment,
        author varchar(255) not null,
        count integer,
        isbn bigint not null,
        price float not null,
        title varchar(255) not null,
        primary key (id)
    );

