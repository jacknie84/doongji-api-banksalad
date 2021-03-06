create table if not exists doongji_upload_file (
    id identity primary key,
    path varchar(4096),
    filename varchar(1024),
    filesize number,
    mime_type varchar(255)
);

create table if not exists doongji_shared_excel (
    id identity primary key,
    upload_id number,
    user_id varchar(255)
);

create table if not exists doongji_household_accounts (
    id identity primary key,
    use_date varchar(255),
    use_time varchar(255),
    type varchar(255),
    category varchar(255),
    sub_category varchar(255),
    description varchar(255),
    use_amount number,
    use_currency varchar(255),
    use_object varchar(255),
    user_id  varchar(255)
);

create table if not exists doongji_retrieved_condition (
    id identity primary key,
    name varchar(255),
    favorite boolean,
    last_retrieved_date varchar(255)
);

create table if not exists doongji_retrieved_condition_predicate (
    id identity primary key,
    condition_id number,
    field_name varchar(255),
    operator varchar(255),
    field_values varchar(255)
);