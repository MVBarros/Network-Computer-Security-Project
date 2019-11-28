drop table if exists files;

create table files(filename varchar  not null,
				   fileowner varchar not null,
				   t_created varchar not null,
				   content bytea not null,
				  primary key (filename, fileowner, t_created));

