drop table if exists files;
drop table if exists users;

create table users(username varchar  primary key not null);
				   

create table files(filename varchar  not null,
				   fileowner varchar not null, 
				   t_created varchar not null, 
				   content bytea not null,
				  primary key (filename, fileowner),
				  foreign key(fileowner) references users(username) on delete cascade) ;

