/*
 * Copyright 2018-2021 https://github.com/myoss
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

package app.myoss.cloud.mybatis.test.integration.h2.test5.entity;

import app.myoss.cloud.core.lang.json.JsonObject;
import app.myoss.cloud.mybatis.repository.entity.AuditIdEntity;
import app.myoss.cloud.mybatis.table.Sequence;
import app.myoss.cloud.mybatis.table.annotation.Column;
import app.myoss.cloud.mybatis.table.annotation.GenerationType;
import app.myoss.cloud.mybatis.table.annotation.SequenceGenerator;
import app.myoss.cloud.mybatis.table.annotation.SequenceGenerator.Order;
import app.myoss.cloud.mybatis.table.annotation.SequenceKey;
import app.myoss.cloud.mybatis.table.annotation.Table;
import app.myoss.cloud.mybatis.test.integration.h2.test5.typehandler.JsonObjectTypeHandler;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;

/**
 * This class corresponds to the database table t_sys_menu
 * <p>
 * Database Table Remarks: 系统菜单表
 * </p>
 *
 * @author jerry
 * @since 2021年4月15日 下午10:33:55
 */
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@Data
@SequenceGenerator(strategy = GenerationType.SEQUENCE_KEY, sequenceKey = @SequenceKey(sequenceClass = Sequence.class, sequenceClassName = "app.myoss.cloud.mybatis.test.integration.h2.test5.SysMenuControllerIntTests.SequenceCustomizer", sequenceName = "sequenceUserLog", keyProperty = {
        "id" }, keyColumn = { "id" }, order = Order.BEFORE))
@Table(name = "t_sys_menu")
public class SysMenu extends AuditIdEntity<Long> {
    private static final long serialVersionUID = 1L;

    /**
     * Database Column Name: t_sys_menu.menu_name
     * <p>
     * Database Column Remarks: 菜单名称
     * </p>
     */
    @Column(name = "menu_name")
    private String            menuName;

    /**
     * Database Column Name: t_sys_menu.menu_path
     * <p>
     * Database Column Remarks: 菜单路径
     * </p>
     */
    @Column(name = "menu_path")
    private String            menuPath;

    /**
     * Database Column Name: t_sys_menu.menu_icon
     * <p>
     * Database Column Remarks: 菜单图标
     * </p>
     */
    @Column(name = "menu_icon")
    private String            menuIcon;

    /**
     * Database Column Name: t_sys_menu.content
     * <p>
     * Database Column Remarks: 菜单详情内容
     * </p>
     */
    @Column(name = "content", typeHandler = JsonObjectTypeHandler.class)
    private JsonObject        content;
}
