-- liquibase formatted sql

-- changeset gpassos:0001-create-tables
create table if not exists "users" (
    id           uuid  not null primary key default gen_random_uuid(),
    ssn          text not null,
    status       text not null,
    created_at   timestamp not null default now(),
    updated_at   timestamp not null default now()
);
--rollback drop table users;

create table if not exists reports (
    id           uuid  not null primary key default gen_random_uuid(),
    user_id      uuid not null,
    external_id1 text not null,
    content      text not null,
    created_at   timestamp not null default now(),
    updated_at   timestamp not null default now()
);
--rollback drop table reports;
