CREATE TABLE IF NOT EXISTS test_data (
	id int unsigned not null auto_increment,
	add_time timestamp not null comment '手工测试时间',
	project varchar(50) not null comment '工程名',
	class_name varchar(120) not null comment '类名',
	method_name varchar(50) not null comment '方法名',
	args varchar(10240) not null comment '入参',
	status tinyint(4) unsigned not null comment '手工测试结果',
	duration int unsigned not null comment '接口执行时长',
	
	ok_count int unsigned not null comment '自动测试成功次数',
	error_count int unsigned not null comment '自动测试失败次数',
	update_time timestamp comment '自动测试时间',
	
	host varchar(100) not null comment '手工测试主机',
	url varchar(100) not null comment '接口路径',
	result varchar(4000000) not null comment '出参',
	
	PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS config_data (
	id int unsigned not null auto_increment,
	facade_path varchar(200) not null comment 'facade绝对路径',
	effect tinyint(4) unsigned not null comment '显示效果',
	often_size tinyint(4) not null comment '常用接口类数量，访问频次最高的类',
	show_service tinyint(4) not null comment '是否显示其他服务',
	api_token varchar(100) not null comment 'yapi接口新增token',
	token varchar(100) not null comment '接口测试指定token，会随header请求',
	update_time timestamp comment '更新时间',
	
	PRIMARY KEY (id)
);