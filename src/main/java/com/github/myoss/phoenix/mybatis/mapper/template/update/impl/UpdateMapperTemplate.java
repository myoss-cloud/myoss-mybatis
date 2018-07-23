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

package com.github.myoss.phoenix.mybatis.mapper.template.update.impl;

import java.util.Map;

import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlSource;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.SystemMetaObject;
import org.apache.ibatis.session.Configuration;

import com.github.myoss.phoenix.mybatis.mapper.template.AbstractMapperTemplate;
import com.github.myoss.phoenix.mybatis.mapper.template.update.UpdateByConditionMapper;
import com.github.myoss.phoenix.mybatis.mapper.template.update.UpdateByPrimaryKeyAllColumnMapper;
import com.github.myoss.phoenix.mybatis.mapper.template.update.UpdateByPrimaryKeyMapper;
import com.github.myoss.phoenix.mybatis.mapper.template.update.UpdateUseMapByConditionMapper;
import com.github.myoss.phoenix.mybatis.table.TableColumnInfo;
import com.github.myoss.phoenix.mybatis.table.TableInfo;
import com.github.myoss.phoenix.mybatis.table.TableMetaObject;
import com.github.myoss.phoenix.mybatis.table.annotation.FillRule;

/**
 * 生成通用 update MappedStatement 模版类
 *
 * @author Jerry.Chen
 * @since 2018年5月1日 下午9:15:39
 */
public class UpdateMapperTemplate extends AbstractMapperTemplate {
    /**
     * 更新记录，生成 update 语句。
     * <p>
     * 示例如下：
     *
     * <pre>
     * UPDATE table_name
     * &lt;set&gt;
     *   &lt;if test=&quot;id != null&quot;&gt;
     *     id = #{id},
     *   &lt;/if&gt;
     * &lt;/set&gt;
     * &lt;where&gt;
     *  AND id = #{id}
     *  AND is_deleted = 'N'
     * &lt;/where&gt;
     * </pre>
     *
     * @param tableInfo 数据库表结构信息
     * @param ms sql语句节点信息，会将生成的sql语句替换掉原有的 {@link MappedStatement#sqlSource}
     * @return 生成的sql语句
     * @see UpdateByPrimaryKeyMapper#updateByPrimaryKey(Object)
     */
    public String updateByPrimaryKey(TableInfo tableInfo, MappedStatement ms) {
        MetaObject metaObject = SystemMetaObject.forObject(ms);
        Configuration configuration = ms.getConfiguration();

        // 生成 sql 语句
        StringBuilder builder = new StringBuilder(2048);
        builder.append("UPDATE ").append(TableMetaObject.getTableName(tableInfo)).append("\n");
        builder.append("<set>\n");
        for (TableColumnInfo columnInfo : tableInfo.getColumns()) {
            if (!columnInfo.isUpdatable() || columnInfo.isPrimaryKey() || columnInfo.isLogicDelete()) {
                continue;
            }
            boolean fillUpdate = columnInfo.haveFillRule(FillRule.UPDATE);
            if (!fillUpdate) {
                builder.append("  <if test=\"").append(columnInfo.getProperty()).append(" != null\">\n");
            }
            builder.append("    ").append(columnInfo.getActualColumn()).append(" = #{").append(
                    columnInfo.getProperty());
            if (columnInfo.getJdbcType() != null) {
                builder.append(",jdbcType=BIGINT");
            }
            builder.append("},\n");
            if (!fillUpdate) {
                builder.append("  </if>\n");
            }
        }
        builder.append("</set>\n");
        builder.append(tableInfo.getWherePrimaryKeySql());
        String sql = builder.toString();

        // 替换 sqlSource 对象
        SqlSource sqlSource = xmlLanguageDriver.createSqlSource(configuration, "<script>\n" + sql + "\n</script>",
                null);
        metaObject.setValue("sqlSource", sqlSource);
        return sql;
    }

    /**
     * 更新记录，生成 update 语句。
     * <p>
     * 示例如下：
     *
     * <pre>
     * UPDATE table_name
     * &lt;set&gt;
     *     name = #{name},
     * &lt;/set&gt;
     * &lt;where&gt;
     *  AND id = #{id}
     *  AND is_deleted = 'N'
     * &lt;/where&gt;
     * </pre>
     *
     * @param tableInfo 数据库表结构信息
     * @param ms sql语句节点信息，会将生成的sql语句替换掉原有的 {@link MappedStatement#sqlSource}
     * @return 生成的sql语句
     * @see UpdateByPrimaryKeyAllColumnMapper#updateByPrimaryKeyAllColumn(Object)
     */
    public String updateByPrimaryKeyAllColumn(TableInfo tableInfo, MappedStatement ms) {
        MetaObject metaObject = SystemMetaObject.forObject(ms);
        Configuration configuration = ms.getConfiguration();

        // 生成 sql 语句
        StringBuilder builder = new StringBuilder(1024);
        builder.append("UPDATE ").append(TableMetaObject.getTableName(tableInfo)).append("\n");
        builder.append("<set>\n");
        for (TableColumnInfo columnInfo : tableInfo.getColumns()) {
            if (!columnInfo.isUpdatable() || columnInfo.isPrimaryKey() || columnInfo.isLogicDelete()) {
                continue;
            }
            builder.append("  ").append(columnInfo.getActualColumn()).append(" = #{").append(columnInfo.getProperty());
            if (columnInfo.getJdbcType() != null) {
                builder.append(",jdbcType=BIGINT");
            }
            builder.append("},\n");
        }
        builder.append("</set>\n");
        builder.append(tableInfo.getWherePrimaryKeySql());
        String sql = builder.toString();

        // 替换 sqlSource 对象
        SqlSource sqlSource = xmlLanguageDriver.createSqlSource(configuration, "<script>\n" + sql + "\n</script>",
                null);
        metaObject.setValue("sqlSource", sqlSource);
        return sql;
    }

    /**
     * 更新记录，生成 update 语句。
     * <p>
     * 示例如下：
     *
     * <pre>
     * UPDATE table_name
     * &lt;set&gt;
     *   &lt;if test=&quot;record.name != null&quot;&gt;
     *     name = #{record.name},
     *   &lt;/if&gt;
     * &lt;/set&gt;
     * &lt;where&gt;
     *  &lt;if test=&quot;condition.id != null&quot;&gt;
     *    and id = #{condition.id}
     *  &lt;/if&gt;
     *  AND is_deleted = 'N'
     * &lt;/where&gt;
     * </pre>
     *
     * @param tableInfo 数据库表结构信息
     * @param ms sql语句节点信息，会将生成的sql语句替换掉原有的 {@link MappedStatement#sqlSource}
     * @return 生成的sql语句
     * @see UpdateByConditionMapper#updateByCondition(Object, Object)
     */
    public String updateByCondition(TableInfo tableInfo, MappedStatement ms) {
        MetaObject metaObject = SystemMetaObject.forObject(ms);
        Configuration configuration = ms.getConfiguration();

        // 生成 sql 语句
        StringBuilder builder = new StringBuilder(4096);
        builder.append("UPDATE ").append(TableMetaObject.getTableName(tableInfo)).append("\n");
        builder.append("<set>\n");
        for (TableColumnInfo columnInfo : tableInfo.getColumns()) {
            if (!columnInfo.isUpdatable() || columnInfo.isPrimaryKey() || columnInfo.isLogicDelete()) {
                continue;
            }
            boolean fillUpdate = columnInfo.haveFillRule(FillRule.UPDATE);
            if (!fillUpdate) {
                builder.append("  <if test=\"record.").append(columnInfo.getProperty()).append(" != null\">\n");
            }
            builder.append("    ").append(columnInfo.getActualColumn()).append(" = #{record.").append(
                    columnInfo.getProperty());
            if (columnInfo.getJdbcType() != null) {
                builder.append(",jdbcType=BIGINT");
            }
            builder.append("},\n");
            if (!fillUpdate) {
                builder.append("</if>\n");
            }
        }
        builder.append("</set>\n");
        builder.append(tableInfo.getWhereConditionWithParameterSql());
        String sql = builder.toString();

        // 替换 sqlSource 对象
        SqlSource sqlSource = xmlLanguageDriver.createSqlSource(configuration, "<script>\n" + sql + "\n</script>",
                null);
        metaObject.setValue("sqlSource", sqlSource);
        return sql;
    }

    /**
     * 更新记录，生成 update 语句。
     * <p>
     * 示例如下：
     *
     * <pre>
     * UPDATE table_name
     * &lt;set&gt;
     *   &lt;foreach collection=&quot;record.keys&quot; item=&quot;k&quot; separator=&quot;,&quot;&gt;
     *     ${k} = #{record[${k}]}
     *   &lt;/foreach&gt;
     * &lt;/set&gt;
     * &lt;where&gt;
     *  &lt;if test=&quot;condition.id != null&quot;&gt;
     *    and id = #{condition.id}
     *  &lt;/if&gt;
     *  AND is_deleted = 'N'
     * &lt;/where&gt;
     * </pre>
     *
     * @param tableInfo 数据库表结构信息
     * @param ms sql语句节点信息，会将生成的sql语句替换掉原有的 {@link MappedStatement#sqlSource}
     * @return 生成的sql语句
     * @see UpdateUseMapByConditionMapper#updateUseMapByCondition(Map, Object)
     */
    public String updateUseMapByCondition(TableInfo tableInfo, MappedStatement ms) {
        MetaObject metaObject = SystemMetaObject.forObject(ms);
        Configuration configuration = ms.getConfiguration();

        // 生成 sql 语句
        StringBuilder builder = new StringBuilder(2048);
        builder.append("UPDATE ").append(TableMetaObject.getTableName(tableInfo)).append("\n");
        builder.append("<set>\n");
        builder.append("  <foreach collection=\"record.keys\" item=\"k\" separator=\",\">\n");
        builder.append("    ${k}").append(" = #{record[${k}]}\n");
        builder.append("  </foreach>\n");
        builder.append("</set>\n");
        builder.append(tableInfo.getWhereConditionWithParameterSql());
        String sql = builder.toString();

        // 替换 sqlSource 对象
        SqlSource sqlSource = xmlLanguageDriver.createSqlSource(configuration, "<script>\n" + sql + "\n</script>",
                null);
        metaObject.setValue("sqlSource", sqlSource);
        return sql;
    }
}
