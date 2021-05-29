use test;

drop table  if exists student;

create table student (
	id int auto_increment primary key,
	name varchar(50) COLLATE utf8mb4_unicode_ci default ''
) DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci ;

