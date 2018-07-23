/*
 * Copyright 2018-2018 https://github.com/myoss
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.github.myoss.phoenix.mybatis.test.integration.h2.test2.entity;

import java.util.Date;

import com.github.myoss.phoenix.mybatis.repository.entity.AuditIdEntity;
import com.github.myoss.phoenix.mybatis.table.annotation.Column;
import com.github.myoss.phoenix.mybatis.table.annotation.GenerationType;
import com.github.myoss.phoenix.mybatis.table.annotation.SelectKey;
import com.github.myoss.phoenix.mybatis.table.annotation.SequenceGenerator;
import com.github.myoss.phoenix.mybatis.table.annotation.SequenceGenerator.Order;
import com.github.myoss.phoenix.mybatis.table.annotation.Table;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;

/**
 * This class corresponds to the database table t_sys_user_history
 * <p>
 * Database Table Remarks: 系统用户信息历史备份表
 * </p>
 *
 * @author jerry
 * @since 2018年5月14日 下午3:39:43
 */
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@Data
@SequenceGenerator(strategy = GenerationType.SELECT_KEY, selectKey = @SelectKey(sql = "select ifnull(max(`id`) ,0) + 1 from t_sys_user_history", keyProperty = {
        "id" }, keyColumn = { "id" }, resultType = Long.class, order = Order.BEFORE))
@Table(name = "t_sys_user_history")
public class UserHistory extends AuditIdEntity<Long> {
    private static final long serialVersionUID = 1L;

    /**
     * Database Column Name: t_sys_user_history.employee_number
     * <p>
     * Database Column Remarks: 员工编号
     * </p>
     */
    @Column(name = "employee_number", jdbcTypeName = "VARCHAR")
    private String            employeeNumber;

    /**
     * Database Column Name: t_sys_user_history.account
     * <p>
     * Database Column Remarks: 登录账号
     * </p>
     */
    @Column(name = "account", jdbcTypeName = "VARCHAR")
    private String            account;

    /**
     * Database Column Name: t_sys_user_history.password
     * <p>
     * Database Column Remarks: 密码
     * </p>
     */
    @Column(name = "password", escapedName = "`password`", jdbcTypeName = "VARCHAR")
    private String            password;

    /**
     * Database Column Name: t_sys_user_history.salt
     * <p>
     * Database Column Remarks: 密钥
     * </p>
     */
    @Column(name = "salt", jdbcTypeName = "VARCHAR")
    private String            salt;

    /**
     * Database Column Name: t_sys_user_history.name
     * <p>
     * Database Column Remarks: 姓名
     * </p>
     */
    @Column(name = "name", escapedName = "`name`", jdbcTypeName = "VARCHAR")
    private String            name;

    /**
     * Database Column Name: t_sys_user_history.gender
     * <p>
     * Database Column Remarks: 性别
     * </p>
     */
    @Column(name = "gender", jdbcTypeName = "CHAR")
    private String            gender;

    /**
     * Database Column Name: t_sys_user_history.birthday
     * <p>
     * Database Column Remarks: 生日
     * </p>
     */
    @Column(name = "birthday", jdbcTypeName = "DATE")
    private Date              birthday;

    /**
     * Database Column Name: t_sys_user_history.email
     * <p>
     * Database Column Remarks: 邮箱
     * </p>
     */
    @Column(name = "email", jdbcTypeName = "VARCHAR")
    private String            email;

    /**
     * Database Column Name: t_sys_user_history.phone
     * <p>
     * Database Column Remarks: 联系电话
     * </p>
     */
    @Column(name = "phone", jdbcTypeName = "VARCHAR")
    private String            phone;

    /**
     * Database Column Name: t_sys_user_history.telephone
     * <p>
     * Database Column Remarks: 工作电话
     * </p>
     */
    @Column(name = "telephone", jdbcTypeName = "VARCHAR")
    private String            telephone;

    /**
     * Database Column Name: t_sys_user_history.company_id
     * <p>
     * Database Column Remarks: 所属公司
     * </p>
     */
    @Column(name = "company_id", jdbcTypeName = "BIGINT")
    private Long              companyId;

    /**
     * Database Column Name: t_sys_user_history.dept_id
     * <p>
     * Database Column Remarks: 用户所属部门
     * </p>
     */
    @Column(name = "dept_id", jdbcTypeName = "BIGINT")
    private Long              deptId;

    /**
     * Database Column Name: t_sys_user_history.position_id
     * <p>
     * Database Column Remarks: 所在职位
     * </p>
     */
    @Column(name = "position_id", jdbcTypeName = "BIGINT")
    private Long              positionId;

    /**
     * Database Column Name: t_sys_user_history.parent_user_id
     * <p>
     * Database Column Remarks: 归属领导id
     * </p>
     */
    @Column(name = "parent_user_id", jdbcTypeName = "BIGINT")
    private Long              parentUserId;

    /**
     * Database Column Name: t_sys_user_history.status
     * <p>
     * Database Column Remarks: 状态（1: 启用; 2: 禁用）
     * </p>
     */
    @Column(name = "status", escapedName = "`status`", jdbcTypeName = "CHAR")
    private String            status;

    /**
     * Database Column Name: t_sys_user_history.entry_date
     * <p>
     * Database Column Remarks: 入职时间
     * </p>
     */
    @Column(name = "entry_date", jdbcTypeName = "DATE")
    private Date              entryDate;

    /**
     * Database Column Name: t_sys_user_history.leave_date
     * <p>
     * Database Column Remarks: 离职日期
     * </p>
     */
    @Column(name = "leave_date", jdbcTypeName = "DATE")
    private Date              leaveDate;

}
