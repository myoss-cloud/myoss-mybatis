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

package com.github.myoss.phoenix.mybatis.mapper.template.delete.impl;

import java.io.Serializable;

import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlSource;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.SystemMetaObject;
import org.apache.ibatis.session.Configuration;

import com.github.myoss.phoenix.mybatis.mapper.template.AbstractMapperTemplate;
import com.github.myoss.phoenix.mybatis.mapper.template.delete.DeleteByConditionMapper;
import com.github.myoss.phoenix.mybatis.mapper.template.delete.DeleteByPrimaryKeyMapper;
import com.github.myoss.phoenix.mybatis.table.TableInfo;
import com.github.myoss.phoenix.mybatis.table.TableMetaObject;

/**
 * 生成通用 delete MappedStatement 模版类
 *
 * @author Jerry.Chen 2018年5月1日 下午6:04:12
 */
public class DeleteMapperTemplate extends AbstractMapperTemplate {
    /**
     * 删除记录，生成 delete 语句。
     * <p>
     * 示例如下：
     *
     * <pre>
     * DELETE FROM `table_name`
     * &lt;where&gt;
     *  AND id = #{id}
     *  AND is_deleted = 'N'
     * &lt;/where&gt;
     * </pre>
     *
     * @param tableInfo 数据库表结构信息
     * @param ms sql语句节点信息，会将生成的sql语句替换掉原有的 {@link MappedStatement#sqlSource}
     * @return 生成的sql语句
     * @see DeleteByPrimaryKeyMapper#deleteByPrimaryKey(Serializable)
     * @see DeleteByPrimaryKeyMapper#deleteWithPrimaryKey(Object)
     */
    public String deleteByPrimaryKey(TableInfo tableInfo, MappedStatement ms) {
        MetaObject metaObject = SystemMetaObject.forObject(ms);
        Configuration configuration = ms.getConfiguration();

        // 生成 sql 语句
        StringBuilder builder = new StringBuilder(128);
        builder.append("DELETE FROM `").append(TableMetaObject.getTableName(tableInfo)).append("`\n");
        builder.append(tableInfo.getWherePrimaryKeySql());
        String sql = builder.toString();

        // 替换 sqlSource 对象
        SqlSource sqlSource = xmlLanguageDriver
                .createSqlSource(configuration, "<script>\n" + sql + "\n</script>", null);
        metaObject.setValue("sqlSource", sqlSource);
        return sql;
    }

    /**
     * 删除记录，生成 delete 语句。
     * <p>
     * 示例如下：
     *
     * <pre>
     * DELETE FROM `table_name`
     * &lt;where&gt;
     *  AND id = #{id}
     *  AND is_deleted = 'N'
     * &lt;/where&gt;
     * </pre>
     *
     * @param tableInfo 数据库表结构信息
     * @param ms sql语句节点信息，会将生成的sql语句替换掉原有的 {@link MappedStatement#sqlSource}
     * @return 生成的sql语句
     * @see DeleteByPrimaryKeyMapper#deleteWithPrimaryKey(Object)
     */
    public String deleteWithPrimaryKey(TableInfo tableInfo, MappedStatement ms) {
        return deleteByPrimaryKey(tableInfo, ms);
    }

    /**
     * 删除记录，生成 delete 语句。
     * <p>
     * 示例如下：
     *
     * <pre>
     * DELETE FROM `table_name`
     * &lt;where&gt;
     *    &lt;if test=&quot;id != null&quot;&gt;
     *      and id = #{id}
     *    &lt;/if&gt;
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
        builder.append("DELETE FROM `").append(TableMetaObject.getTableName(tableInfo)).append("`\n");
        builder.append(tableInfo.getWhereConditionSql());
        String sql = builder.toString();

        // 替换 sqlSource 对象
        SqlSource sqlSource = xmlLanguageDriver
                .createSqlSource(configuration, "<script>\n" + sql + "\n</script>", null);
        metaObject.setValue("sqlSource", sqlSource);
        return sql;
    }
}
