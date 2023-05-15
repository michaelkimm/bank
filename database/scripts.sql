CREATE SCHEMA IF NOT EXISTS bank CHARACTER SET utf8;
USE bank;

create table account (
   account_number varchar(20) not null,
    balance decimal(19,2),
    member_id bigint,
    primary key (account_number)
) engine=InnoDB;

create table custom_sequence (
   sequence_name varchar(255) not null,
    next_val bigint,
    primary key (sequence_name)
) engine=InnoDB;

insert into custom_sequence(sequence_name, next_val) values ('EXTERNAL_TRANSFER_DEPOSIT_SUCCESS_RESPONSE_OUTBOX_SEQ',0);

insert into custom_sequence(sequence_name, next_val) values ('EXTERNAL_TRANSFER_OUTBOX_SEQ',0);

insert into custom_sequence(sequence_name, next_val) values ('EXTERNAL_TRANSFER_DEPOSIT_OUTBOX_SEQ',0);

create table external_transfer_deposit_out_box (
   id bigint not null,
    pay_load longtext,
    primary key (id)
) engine=InnoDB;

create table external_transfer_deposit_success_response_out_box (
   id bigint not null,
    pay_load longtext,
    primary key (id)
) engine=InnoDB;

create table external_transfer_out_box (
   id bigint not null,
    pay_load longtext,
    primary key (id)
) engine=InnoDB;

create table member (
   member_id bigint not null auto_increment,
    name varchar(255),
    primary key (member_id)
) engine=InnoDB;

create table transfer_history (
   create_date date not null,
    guid varchar(255) not null,
    created_at datetime,
    updated_at datetime,
    amount_after_deposit decimal(19,2),
    amount_after_withdrawal decimal(19,2),
    deposit_account_number varchar(20),
    deposit_bank_id varchar(2),
    deposit_member_name varchar(30),
    memo_to_receiver varchar(20),
    memo_to_sender varchar(20),
    public_transfer_id varchar(40) UNIQUE,
    state varchar(20),
    transfer_amount decimal(19,2),
    withdrawal_account_number varchar(20),
    withdrawal_bank_id varchar(2),
    withdrawal_member_name varchar(30),
    primary key (create_date, guid)
) engine=InnoDB;