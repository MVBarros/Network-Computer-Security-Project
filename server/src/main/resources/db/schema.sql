drop table if exists files;

create table files(fileId varchar  not null,
				   content bytea not null,
				  primary key (fileId));
