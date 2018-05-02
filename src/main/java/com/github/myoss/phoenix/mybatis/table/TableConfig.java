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

import lombok.Data;

import com.github.myoss.phoenix.core.utils.NameStyle;

/**
 * MyBatis Table 全局配置；实体类映射数据库表的全局配置
 *
 * @author Jerry.Chen 2018年4月27日 上午12:24:07
 */
@Data
public class TableConfig {
    /**
     * 数据库中的catalog，如果设置了此属性，将在表名前面加上catalog指定的值
     * <p>
     * (Optional) The catalog of the table.
     * <p>
     * Defaults to the default catalog.
     */
    private String    catalog         = "";
    /**
     * 数据库中的schema，如果设置了此属性，将在表名前面加上schema指定的值
     * <p>
     * (Optional) The schema of the table.
     * <p>
     * Defaults to the default schema for user.
     */
    private String    schema          = "";
    /**
     * 数据库表名字前缀
     */
    private String    tableNamePrefix = "";
    /**
     * 数据库表名字后缀
     */
    private String    tableNameSuffix = "";
    /**
     * 数据库表名命名风格
     * <p>
     * table name style
     * <p>
     * Defaults to snake_case.
     */
    private NameStyle tableNameStyle  = NameStyle.SNAKE_CASE;
    /**
     * 数据库表字段名命名风格
     * <p>
     * column name style
     * <p>
     * Defaults to snake_case.
     */
    private NameStyle columnNameStyle = NameStyle.SNAKE_CASE;

    /**
     * 逻辑删除数据，软删除，用字段标记数据被删除了，不做物理删除
     */
    private boolean   logicDelete     = false;
    /**
     * 数据库表中默认的"逻辑删除"字段名，如果数据库表中有匹配的字段名，则可以不用在每个实体类中设置
     */
    private String    logicDeleteColumnName;
    /**
     * 数据标记为"逻辑删除"的值
     */
    private String    logicDeleteValue;
    /**
     * 数据标记为"逻辑未删除"的值
     */
    private String    logicUnDeleteValue;
}
