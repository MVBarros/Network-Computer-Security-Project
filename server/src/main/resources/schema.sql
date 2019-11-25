drop table if exists authorizations;
drop table if exists files;
drop table if exists users;

create table users(username varchar  primary key not null);
				   

create table files(filename varchar  not null,
				   fileowner varchar not null, 
				   t_created varchar not null, 
				   content bytea not null,
				  primary key (filename, fileowner),
				  foreign key(fileowner) references users(username) on delete cascade) ;

create table authorizations(filename varchar not null,
						   fileowner varchar not null,
						   username varchar not null,
						   permission integer not null,
						   primary key(filename, fileowner, username),
						   foreign key(filename, fileowner) references files(filename, fileowner) on delete cascade,
						   foreign key (username) references users(username) on delete cascade);
