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

package app.myoss.cloud.mybatis.test.integration.h2.test4.entity;

import app.myoss.cloud.mybatis.repository.entity.AuditIdEntity;
import app.myoss.cloud.mybatis.table.Sequence;
import app.myoss.cloud.mybatis.table.annotation.Column;
import app.myoss.cloud.mybatis.table.annotation.GenerationType;
import app.myoss.cloud.mybatis.table.annotation.SequenceGenerator;
import app.myoss.cloud.mybatis.table.annotation.SequenceGenerator.Order;
import app.myoss.cloud.mybatis.table.annotation.SequenceKey;
import app.myoss.cloud.mybatis.table.annotation.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;

/**
 * This class corresponds to the database table t_sys_user_log
 * <p>
 * Database Table Remarks: 系统用户日志记录表
 * </p>
 *
 * @author jerry
 * @since 2018年5月14日 下午10:33:55
 */
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@Data
@SequenceGenerator(strategy = GenerationType.SEQUENCE_KEY, sequenceKey = @SequenceKey(sequenceClass = Sequence.class, sequenceClassName = "app.myoss.cloud.mybatis.test.integration.h2.test4.SysUserLogControllerIntTests.SequenceCustomizer", sequenceName = "sequenceUserLog", keyProperty = {
        "id" }, keyColumn = { "id" }, order = Order.BEFORE))
@Table(name = "t_sys_user_log")
public class SysUserLog extends AuditIdEntity<Long> {
    private static final long serialVersionUID = 1L;

    /**
     * Database Column Name: t_sys_user_log.employee_number
     * <p>
     * Database Column Remarks: 员工编号
     * </p>
     */
    @Column(name = "employee_number", jdbcTypeName = "VARCHAR")
    private String            employeeNumber;

    /**
     * Database Column Name: t_sys_user_log.info
     * <p>
     * Database Column Remarks: 日志信息
     * </p>
     */
    @Column(name = "info", jdbcTypeName = "VARCHAR")
    private String            info;

}
