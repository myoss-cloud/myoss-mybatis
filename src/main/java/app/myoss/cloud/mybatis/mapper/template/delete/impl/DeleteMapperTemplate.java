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

package app.myoss.cloud.mybatis.mapper.template.delete.impl;

import java.io.Serializable;

import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.mapping.SqlSource;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.SystemMetaObject;
import org.apache.ibatis.session.Configuration;

import app.myoss.cloud.mybatis.mapper.template.AbstractMapperTemplate;
import app.myoss.cloud.mybatis.mapper.template.delete.DeleteByConditionMapper;
import app.myoss.cloud.mybatis.mapper.template.delete.DeleteByPrimaryKeyMapper;
import app.myoss.cloud.mybatis.table.TableColumnInfo;
import app.myoss.cloud.mybatis.table.TableInfo;
import app.myoss.cloud.mybatis.table.TableMetaObject;
import app.myoss.cloud.mybatis.table.annotation.FillRule;

/**
 * 生成通用 delete MappedStatement 模版类
 *
 * @author Jerry.Chen
 * @since 2018年5月1日 下午6:04:12
 */
public class DeleteMapperTemplate extends AbstractMapperTemplate {
    /**
     * 删除记录，生成 delete 语句。
     * <p>
     * delete 语句示例如下：
     *
     * <pre>
     * DELETE FROM table_name
     * &lt;where&gt;
     *  AND id = #{id}
     *  AND is_deleted = 'N'
     * &lt;/where&gt;
     * </pre>
     * <p>
     * update 语句示例如下：
     *
     * <pre>
     * UPDATE table_name
     * &lt;set&gt;
     *   is_deleted = 'Y',
     * &lt;/set&gt;
     * &lt;where&gt;
     *   AND id = #{id}
     *   AND is_deleted = 'N'
     * &lt;/where&gt;
     * </pre>
     *
     * @param tableInfo 数据库表结构信息
     * @param ms sql语句节点信息，会将生成的sql语句替换掉原有的 {@link MappedStatement#sqlSource}
     * @return 生成的sql语句
     * @see DeleteByPrimaryKeyMapper#deleteByPrimaryKey(Serializable)
     */
    public String deleteByPrimaryKey(TableInfo tableInfo, MappedStatement ms) {
        MetaObject metaObject = SystemMetaObject.forObject(ms);
        Configuration configuration = ms.getConfiguration();

        // 生成 sql 语句
        StringBuilder builder = new StringBuilder(128);
        if (tableInfo.isLogicDelete()) {
            // 逻辑删除
            metaObject.setValue("sqlCommandType", SqlCommandType.UPDATE);
            builder.append("UPDATE ").append(TableMetaObject.getTableName(tableInfo)).append("\n");
            builder.append("<set>\n");
            for (TableColumnInfo columnInfo : tableInfo.getLogicDeleteColumns()) {
                builder.append("  ").append(columnInfo.getActualColumn());
                if (CharSequence.class.isAssignableFrom(columnInfo.getJavaType())) {
                    builder.append(" = '").append(columnInfo.getLogicDeleteValue()).append("'");
                } else {
                    builder.append(" = ").append(columnInfo.getLogicDeleteValue());
                }
                builder.append(",\n");
            }
            builder.append("</set>\n");
        } else {
            builder.append("DELETE FROM ").append(TableMetaObject.getTableName(tableInfo)).append("\n");
        }
        builder.append(tableInfo.getWherePrimaryKeySql());
        String sql = builder.toString();

        // 替换 sqlSource 对象
        SqlSource sqlSource = xmlLanguageDriver.createSqlSource(configuration, "<script>\n" + sql + "\n</script>",
                null);
        metaObject.setValue("sqlSource", sqlSource);
        return sql;
    }

    /**
     * 删除记录，生成 delete 语句；如果数据库表支持"逻辑删除"，则会生成 update 语句。
     * <p>
     * delete 语句示例如下：
     *
     * <pre>
     * DELETE FROM table_name
     * &lt;where&gt;
     *  AND id = #{id}
     *  AND is_deleted = 'N'
     * &lt;/where&gt;
     * </pre>
     * <p>
     * update 语句示例如下：
     *
     * <pre>
     * UPDATE table_name
     * &lt;set&gt;
     *   is_deleted = 'Y',
     *   modifier = #{modifier},
     *   gmt_modified = #{gmtModified},
     * &lt;/set&gt;
     * &lt;where&gt;
     *   AND id = #{id}
     *   AND is_deleted = 'N'
     * &lt;/where&gt;
     * </pre>
     *
     * @param tableInfo 数据库表结构信息
     * @param ms sql语句节点信息，会将生成的sql语句替换掉原有的 {@link MappedStatement#sqlSource}
     * @return 生成的sql语句
     * @see DeleteByPrimaryKeyMapper#deleteWithPrimaryKey(Object)
     */
    public String deleteWithPrimaryKey(TableInfo tableInfo, MappedStatement ms) {
        MetaObject metaObject = SystemMetaObject.forObject(ms);
        Configuration configuration = ms.getConfiguration();

        // 生成 sql 语句
        StringBuilder builder = new StringBuilder(128);
        buildDeleteEntitySql(tableInfo, metaObject, builder);
        builder.append(tableInfo.getWherePrimaryKeySql());
        String sql = builder.toString();

        // 替换 sqlSource 对象
        SqlSource sqlSource = xmlLanguageDriver.createSqlSource(configuration, "<script>\n" + sql + "\n</script>",
                null);
        metaObject.setValue("sqlSource", sqlSource);
        return sql;
    }

    private void buildDeleteEntitySql(TableInfo tableInfo, MetaObject metaObject, StringBuilder builder) {
        if (tableInfo.isLogicDelete()) {
            // 逻辑删除
            metaObject.setValue("sqlCommandType", SqlCommandType.UPDATE);
            builder.append("UPDATE ").append(TableMetaObject.getTableName(tableInfo)).append("\n");
            builder.append("<set>\n");
            for (TableColumnInfo columnInfo : tableInfo.getColumns()) {
                boolean logicDelete = columnInfo.isLogicDelete();
                boolean fillUpdate = columnInfo.haveFillRule(FillRule.UPDATE);
                if (!(logicDelete || fillUpdate)) {
                    continue;
                }
                builder.append("  ").append(columnInfo.getActualColumn());
                if (logicDelete) {
                    if (CharSequence.class.isAssignableFrom(columnInfo.getJavaType())) {
                        builder.append(" = '").append(columnInfo.getLogicDeleteValue()).append("'");
                    } else {
                        builder.append(" = ").append(columnInfo.getLogicDeleteValue());
                    }
                } else {
                    builder.append(" = #{").append(columnInfo.getProperty());
                    builder.append("}");
                }
                builder.append(",\n");
            }
            builder.append("</set>\n");
        } else {
            builder.append("DELETE FROM ").append(TableMetaObject.getTableName(tableInfo)).append("\n");
        }
    }

    /**
     * 删除记录，生成 delete 语句。
     * <p>
     * delete 语句示例如下：
     *
     * <pre>
     * DELETE FROM table_name
     * &lt;where&gt;
     *    &lt;if test=&quot;id != null&quot;&gt;
     *      and id = #{id}
     *    &lt;/if&gt;
     * &lt;/where&gt;
     * </pre>
     * <p>
     * update 语句示例如下：
     *
     * <pre>
     * UPDATE table_name
     * &lt;set&gt;
     *   is_deleted = 'Y',
     *   modifier = #{modifier},
     *   gmt_modified = #{gmtModified},
     * &lt;/set&gt;
     * &lt;where&gt;
     *    &lt;if test=&quot;id != null&quot;&gt;
     *      and id = #{id}
     *    &lt;/if&gt;
     *    AND is_deleted = 'N'
     * &lt;/where&gt;
     * </pre>
     *
     * @param tableInfo 数据库表结构信息
     * @param ms sql语句节点信息，会将生成的sql语句替换掉原有的 {@link MappedStatement#sqlSource}
     * @return 生成的sql语句
     * @see DeleteByConditionMapper#deleteByCondition(Object)
     */
    public String deleteByCondition(TableInfo tableInfo, MappedStatement ms) {
        MetaObject metaObject = SystemMetaObject.forObject(ms);
        Configuration configuration = ms.getConfiguration();

        // 生成 sql 语句
        StringBuilder builder = new StringBuilder(2048);
        buildDeleteEntitySql(tableInfo, metaObject, builder);
        builder.append(tableInfo.getWhereConditionSql());
        String extendSql = getWhereExtend(ms);
        if (extendSql != null) {
            builder.insert(builder.length() - 8, extendSql);
        }
        String sql = builder.toString();

        // 替换 sqlSource 对象
        SqlSource sqlSource = xmlLanguageDriver.createSqlSource(configuration, "<script>\n" + sql + "\n</script>",
                null);
        metaObject.setValue("sqlSource", sqlSource);
        return sql;
    }

    /**
     * 删除记录，生成 delete 语句。
     * <p>
     * delete 语句示例如下：
     *
     * <pre>
     * DELETE FROM table_name
     * &lt;where&gt;
     *    &lt;if test=&quot;id != null&quot;&gt;
     *      and id = #{id}
     *    &lt;/if&gt;
     * &lt;/where&gt;
     * </pre>
     * <p>
     * update 语句示例如下：
     *
     * <pre>
     * UPDATE table_name
     * &lt;set&gt;
     *   is_deleted = 'Y',
     *   &lt;if test=&quot;record.creator != null&quot;&gt;
     *     creator = #{record.creator},
     *   &lt;/if&gt;
     *   modifier = #{record.modifier},
     * &lt;/set&gt;
     * &lt;where&gt;
     *   &lt;if test=&quot;condition != null&quot;&gt;
     *     &lt;if test=&quot;condition.creator != null&quot;&gt;
     *       and creator = #{condition.creator}
     *     &lt;/if&gt;
     *     &lt;if test=&quot;condition.modifier != null&quot;&gt;
     *       and modifier = #{condition.modifier}
     *     &lt;/if&gt;
     *     &lt;if test=&quot;condition.id != null&quot;&gt;
     *       and id = #{condition.id}
     *     &lt;/if&gt;
     *   &lt;/if&gt;
     *   and is_deleted = 'N'
     * &lt;/where&gt;
     * </pre>
     *
     * @param tableInfo 数据库表结构信息
     * @param ms sql语句节点信息，会将生成的sql语句替换掉原有的 {@link MappedStatement#sqlSource}
     * @return 生成的sql语句
     * @see DeleteByConditionMapper#deleteByConditionAndUpdate(Object, Object)
     */
    public String deleteByConditionAndUpdate(TableInfo tableInfo, MappedStatement ms) {
        MetaObject metaObject = SystemMetaObject.forObject(ms);
        Configuration configuration = ms.getConfiguration();

        // 生成 sql 语句
        StringBuilder builder = new StringBuilder(2048);
        builder.append("UPDATE ").append(TableMetaObject.getTableName(tableInfo)).append("\n");
        builder.append("<set>\n");
        for (TableColumnInfo columnInfo : tableInfo.getColumns()) {
            boolean logicDelete = columnInfo.isLogicDelete();
            boolean fillUpdate = columnInfo.haveFillRule(FillRule.UPDATE);
            if (!(logicDelete || fillUpdate || !columnInfo.isPrimaryKey())) {
                continue;
            }
            if (logicDelete) {
                builder.append("  ").append(columnInfo.getActualColumn());
                if (CharSequence.class.isAssignableFrom(columnInfo.getJavaType())) {
                    builder.append(" = '").append(columnInfo.getLogicDeleteValue()).append("'");
                } else {
                    builder.append(" = ").append(columnInfo.getLogicDeleteValue());
                }
                builder.append(",\n");
            } else {
                if (!fillUpdate) {
                    builder.append("  <if test=\"record.").append(columnInfo.getProperty()).append(" != null\">\n  ");
                }
                builder.append("  ")
                        .append(columnInfo.getActualColumn())
                        .append(" = #{record.")
                        .append(columnInfo.getProperty());
                builder.append("},\n");
                if (!fillUpdate) {
                    builder.append("  </if>\n");
                }
            }
        }
        builder.append("</set>\n");
        builder.append(tableInfo.getWhereConditionWithParameterSql());
        StringBuilder extendSql = getWhereExtendCondition(ms);
        if (extendSql != null) {
            builder.insert(builder.length() - 8, extendSql);
        }
        String sql = builder.toString();

        // 替换 sqlSource 对象
        SqlSource sqlSource = xmlLanguageDriver.createSqlSource(configuration, "<script>\n" + sql + "\n</script>",
                null);
        metaObject.setValue("sqlSource", sqlSource);
        return sql;
    }
}
