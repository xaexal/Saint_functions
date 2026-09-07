create database saint;
use saint;
create table church (
	id int unsigned auto_increment primary key comment '교회아이디 기본키',
    name varchar(62) not null comment '교회명',
    pastor_id int unsigned default 0 comment '담임목사의 id',
    phone varchar(32) comment '대표전화번호',
    postcode char(5) comment '소재지 우편번호',
    address varchar(128) comment '소재지주소',
    found_year varchar(8) comment '창립연월일',
    affliate varchar(24) comment '소속교단',
    created datetime default current_timestamp,
    updated datetime default current_timestamp on update current_timestamp
);    
create table member (
	id int unsigned auto_increment primary key comment '교인 아이디 기본키',
    church_id int unsigned comment '소속교회 아이디', 
    name varchar(32) not null comment '교인이름',
    mobile varchar(24),
    gender enum ('f','m',''),
    family_id int unsigned,
	created datetime default current_timestamp,
    updated datetime default current_timestamp on update current_timestamp,
    foreign key(church_id) references church(id) ON DELETE CASCADE ON UPDATE CASCADE
);    
create table family (
	id int unsigned auto_increment primary key comment '가족 아이디 기본키',
    member_id int unsigned comment '가족에 속한 교인의 아이디',
    created datetime default current_timestamp,
    updated datetime default current_timestamp on update current_timestamp,
    foreign key(member_id) references member(id) ON DELETE CASCADE ON UPDATE CASCADE
);    
alter table member add userid varchar(24) after name, add passcode varchar(24) after userid;
alter table member change name name varchar(32) comment '교인이름';
desc member;
create table position (
	id int unsigned auto_increment primary key comment '직분코드 기본키',
    title varchar(32) not null comment '직분명',
    church_id int unsigned,
    foreign key (church_id) references church(id),
    created datetime default current_timestamp,
    updated datetime default current_timestamp on update current_timestamp
);   
CREATE TABLE position_history (
    id INT UNSIGNED AUTO_INCREMENT PRIMARY KEY COMMENT '직분임명내역 기본키',
    member_id int unsigned,
    position_id INT UNSIGNED, 
    assigned VARCHAR(10),
    created DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '레코드 생성일시',
    updated DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);
alter table position_history add foreign key (position_id) REFERENCES position(id);
create table cell (
	id  int unsigned auto_increment primary key comment '다락방(순) 아이디 기본키',
    name varchar(62) not null comment '다락방(순) 이름',
    started datetime,
    closed datetime,
    created datetime default current_timestamp,
    updated datetime default current_timestamp on update current_timestamp
);    
create table cell_history (
	id int unsigned auto_increment primary key,
    member_id int unsigned,foreign key(member_id) references member(id),
    position_id int unsigned comment '다락방(순) 직분아이디',
    assigned date default (current_date) comment '다락방 배정일',
    finished date default (current_date) comment '다락방 수료일',
    created datetime default current_timestamp,
    updated datetime default current_timestamp on update current_timestamp
);
desc position_history;