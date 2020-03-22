create table if not exists doongji_upload_file (
    id identity primary key,
    path varchar(4096),
    filename varchar(1024),
    filesize number,
    mime_type varchar(255)
);