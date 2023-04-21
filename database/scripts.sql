CREATE SCHEMA IF NOT EXISTS bank CHARACTER SET utf8;
USE bank;

create table account (
   account_number varchar(20) not null,
   balance decimal(19,2),
   member_id bigint,
   primary key (account_number)
) engine=InnoDB;

create table external_transfer_deposit_out_box (
   id bigint not null,
    pay_load longtext,
    primary key (id)
) engine=InnoDB;

create table external_transfer_out_box (
   id bigint not null,
    pay_load longtext,
    primary key (id)
) engine=InnoDB;

create table hibernate_sequence (
   next_val bigint
) engine=InnoDB;


insert into hibernate_sequence values ( 1 );

create table member (
   id bigint not null auto_increment,
    name varchar(255),
    primary key (id)
) engine=InnoDB;

create table transfer_history (
    create_date date not null,
    guid varchar(255) not null,
    amount_after_deposit decimal(19,2),
    amount_after_withdrawal decimal(19,2),
    created_at datetime(6),
    updated_at datetime(6),
    deposit_account_number varchar(20),
    deposit_bank_id varchar(2),
    deposit_member_name varchar(30),
    memo_to_receiver varchar(10),
    memo_to_sender varchar(10),
    public_transfer_id varchar(40) UNIQUE,
    state varchar(20),
    transfer_amount decimal(19,2),
    withdrawal_account_number varchar(20),
    withdrawal_bank_id varchar(2),
    withdrawal_member_name varchar(30),
    primary key (create_date, guid)
) engine=InnoDB;