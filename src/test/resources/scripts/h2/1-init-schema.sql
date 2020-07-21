CREATE TABLE IF NOT EXISTS `sequence` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键id',
  `gmt_created` datetime NOT NULL COMMENT '创建时间',
  `gmt_modified` datetime NOT NULL COMMENT '修改时间',
  `name` varchar(64) NOT NULL COMMENT '序列名称',
  `value` bigint(20) NOT NULL COMMENT '序列值',
  PRIMARY KEY (`id`)
);

CREATE TABLE IF NOT EXISTS `t_sys_user` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键id',
  `employee_number` varchar(32) DEFAULT NULL COMMENT '员工编号',
  `account` varchar(32) DEFAULT NULL COMMENT '登录账号',
  `password` varchar(64) DEFAULT NULL COMMENT '密码',
  `salt` varchar(16) DEFAULT NULL COMMENT '密钥',
  `name` varchar(32) DEFAULT NULL COMMENT '姓名',
  `gender` char(1) DEFAULT NULL COMMENT '性别',
  `birthday` date DEFAULT NULL COMMENT '生日',
  `email` varchar(64) DEFAULT NULL COMMENT '邮箱',
  `phone` varchar(32) DEFAULT NULL COMMENT '联系电话',
  `telephone` varchar(32) DEFAULT NULL COMMENT '工作电话',
  `company_id` bigint(20) DEFAULT NULL COMMENT '所属公司',
  `dept_id` bigint(20) DEFAULT NULL COMMENT '用户所属部门',
  `position_id` bigint(20) DEFAULT NULL COMMENT '所在职位',
  `parent_user_id` bigint(20) DEFAULT NULL COMMENT '归属领导id',
  `status` char(1) DEFAULT NULL COMMENT '状态（1: 启用; 2: 禁用）',
  `entry_date` date DEFAULT NULL COMMENT '入职时间',
  `leave_date` date DEFAULT NULL COMMENT '离职日期',
  `is_deleted` char(1) NOT NULL DEFAULT 'N' COMMENT '是否删除',
  `creator` varchar(32) NOT NULL COMMENT '创建者',
  `modifier` varchar(32) NOT NULL COMMENT '修改者',
  `gmt_created` datetime NOT NULL COMMENT '创建时间',
  `gmt_modified` datetime NOT NULL COMMENT '修改时间',
  PRIMARY KEY (`id`)
);

COMMENT ON TABLE `t_sys_user` IS '系统用户信息表';

CREATE TABLE IF NOT EXISTS `t_sys_user_history` (
  `id` bigint(20) NOT NULL COMMENT '主键id',
  `employee_number` varchar(32) DEFAULT NULL COMMENT '员工编号',
  `account` varchar(32) DEFAULT NULL COMMENT '登录账号',
  `password` varchar(64) DEFAULT NULL COMMENT '密码',
  `salt` varchar(16) DEFAULT NULL COMMENT '密钥',
  `name` varchar(32) DEFAULT NULL COMMENT '姓名',
  `gender` char(1) DEFAULT NULL COMMENT '性别',
  `birthday` date DEFAULT NULL COMMENT '生日',
  `email` varchar(64) DEFAULT NULL COMMENT '邮箱',
  `phone` varchar(32) DEFAULT NULL COMMENT '联系电话',
  `telephone` varchar(32) DEFAULT NULL COMMENT '工作电话',
  `company_id` bigint(20) DEFAULT NULL COMMENT '所属公司',
  `dept_id` bigint(20) DEFAULT NULL COMMENT '用户所属部门',
  `position_id` bigint(20) DEFAULT NULL COMMENT '所在职位',
  `parent_user_id` bigint(20) DEFAULT NULL COMMENT '归属领导id',
  `status` char(1) DEFAULT NULL COMMENT '状态（1: 启用; 2: 禁用）',
  `entry_date` date DEFAULT NULL COMMENT '入职时间',
  `leave_date` date DEFAULT NULL COMMENT '离职日期',
  `is_deleted` char(1) NOT NULL DEFAULT 'N' COMMENT '是否删除',
  `creator` varchar(32) NOT NULL COMMENT '创建者',
  `modifier` varchar(32) NOT NULL COMMENT '修改者',
  `gmt_created` datetime NOT NULL COMMENT '创建时间',
  `gmt_modified` datetime NOT NULL COMMENT '修改时间',
  PRIMARY KEY (`id`)
);

COMMENT ON TABLE `t_sys_user_history` IS '系统用户信息历史备份表';

CREATE TABLE IF NOT EXISTS `t_sys_user_log` (
  `id` bigint(20) NOT NULL COMMENT '主键id',
  `employee_number` varchar(32) DEFAULT NULL COMMENT '员工编号',
  `info` varchar(32) DEFAULT NULL COMMENT '日志信息',
  `is_deleted` char(1) NOT NULL DEFAULT 'N' COMMENT '是否删除',
  `creator` varchar(32) NOT NULL COMMENT '创建者',
  `modifier` varchar(32) NOT NULL COMMENT '修改者',
  `gmt_created` datetime NOT NULL COMMENT '创建时间',
  `gmt_modified` datetime NOT NULL COMMENT '修改时间',
  PRIMARY KEY (`id`)
);

COMMENT ON TABLE `t_sys_user_log` IS '系统用户日志记录表';
