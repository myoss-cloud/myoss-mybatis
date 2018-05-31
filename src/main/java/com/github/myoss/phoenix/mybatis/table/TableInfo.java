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

package com.github.myoss.phoenix.mybatis.table;

import java.util.Set;

import org.apache.ibatis.mapping.ResultMap;
import org.apache.ibatis.session.Configuration;

import lombok.Data;

/**
 * 数据库表结构信息，包含数据库字段信息、实体类class等信息
 *
 * @author Jerry.Chen
 * @since 2018年4月26日 上午11:02:15
 */
@Data
public class TableInfo {
    /**
     * 数据库中的catalog，如果设置了此属性，将在表名前面加上catalog指定的值
     * <p>
     * (Optional) The catalog of the table.
     * <p>
     * Defaults to the default catalog.
     */
    private String               catalog;
    /**
     * 数据库中的schema，如果设置了此属性，将在表名前面加上schema指定的值
     * <p>
     * (Optional) The schema of the table.
     * <p>
     * Defaults to the default schema for user.
     */
    private String               schema;
    /**
     * 表名称
     */
    private String               tableName;
    /**
     * 编码之后的数据库表名，比如：表名是关键字、有空格
     */
    private String               escapedTableName;
    /**
     * 实体类class
     */
    private Class<?>             entityClass;
    /**
     * mapper interface class
     */
    private Class<?>             mapperInterfaceClass;

    /**
     * 表字段信息
     */
    private Set<TableColumnInfo> columns;

    /**
     * 表主键ID
     */
    private Set<TableColumnInfo> primaryKeyColumns;
    /**
     * 数据库表"序列生成器"属性配置
     */
    private TableSequence        tableSequence;

    /**
     * 逻辑删除数据，软删除，用字段标记数据被删除了，不做物理删除
     */
    private boolean              logicDelete = false;
    /**
     * 逻辑删除字段信息
     */
    private Set<TableColumnInfo> logicDeleteColumns;

    /**
     * 生成实体的 BaseResultMap 对象，表映射结果集
     *
     * @see TableMetaObject#builderBaseResultMap(TableInfo, Configuration)
     */
    private ResultMap            baseResultMap;

    /**
     * 生成 select 查询所有列sql语句
     *
     * @see TableMetaObject#builderSelectAllColumns(TableInfo)
     */
    private String               selectAllColumnsSql;
    /**
     * 生成 where 主键条件sql语句
     *
     * @see TableMetaObject#builderWherePrimaryKeySql(TableInfo)
     */
    private StringBuilder        wherePrimaryKeySql;
    /**
     * 生成 where 所有条件sql语句
     *
     * @see TableMetaObject#builderWhereConditionSql(TableInfo)
     */
    private StringBuilder        whereConditionSql;
    /**
     * 生成 where 所有条件sql语句，带有参数前缀
     *
     * @see TableMetaObject#builderWhereConditionWithParameterSql(TableInfo,
     *      String)
     */
    private StringBuilder        whereConditionWithParameterSql;

    /**
     * 获取表名称，优先取 {@link #escapedTableName}，如果没有则取 {@link #tableName}
     *
     * @return 表名称
     */
    public String getActualTableName() {
        return escapedTableName != null ? escapedTableName : tableName;
    }
}
