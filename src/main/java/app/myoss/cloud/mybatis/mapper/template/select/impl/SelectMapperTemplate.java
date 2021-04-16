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

package app.myoss.cloud.mybatis.mapper.template.select.impl;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.ResultMap;
import org.apache.ibatis.mapping.SqlSource;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.SystemMetaObject;
import org.apache.ibatis.session.Configuration;

import app.myoss.cloud.mybatis.mapper.template.AbstractMapperTemplate;
import app.myoss.cloud.mybatis.mapper.template.select.SelectByPrimaryKeyMapper;
import app.myoss.cloud.mybatis.mapper.template.select.SelectCountMapper;
import app.myoss.cloud.mybatis.mapper.template.select.SelectListMapper;
import app.myoss.cloud.mybatis.mapper.template.select.SelectOneMapper;
import app.myoss.cloud.mybatis.mapper.template.select.SelectPageMapper;
import app.myoss.cloud.mybatis.table.TableInfo;
import app.myoss.cloud.mybatis.table.TableMetaObject;

/**
 * 生成 select MappedStatement 模版类
 *
 * @author Jerry.Chen
 * @since 2018年4月25日 下午6:53:03
 */
public class SelectMapperTemplate extends AbstractMapperTemplate {

    /**
     * 查询记录，生成 select 语句。
     * <p>
     * 示例如下：
     *
     * <pre>
     * SELECT id,... FROM table_name
     * &lt;where&gt;
     *   &lt;if test=&quot;id != null&quot;&gt;
     *     and id = #{id}
     *   &lt;/if&gt;
     *   and is_deleted = 'N'
     * &lt;/where&gt;
     * </pre>
     *
     * @param tableInfo 数据库表结构信息
     * @param ms sql语句节点信息，会将生成的sql语句替换掉原有的 {@link MappedStatement#sqlSource}
     * @return 生成的sql语句
     * @see SelectOneMapper#selectOne(Object)
     */
    public String selectOne(TableInfo tableInfo, MappedStatement ms) {
        MetaObject metaObject = SystemMetaObject.forObject(ms);
        // 替换 resultMap 对象
        List<ResultMap> resultMaps = Stream.of(tableInfo.getBaseResultMap())
                .collect(Collectors.collectingAndThen(Collectors.toList(), Collections::unmodifiableList));
        metaObject.setValue("resultMaps", resultMaps);

        // 生成 sql 语句
        StringBuilder builder = new StringBuilder(2048);
        builder.append("SELECT ").append(tableInfo.getSelectAllColumnsSql());
        builder.append(" FROM ").append(TableMetaObject.getTableName(tableInfo)).append("\n");
        builder.append(tableInfo.getWhereConditionSql());
        String extendSql = getWhereExtend(ms);
        if (extendSql != null) {
            builder.insert(builder.length() - 8, extendSql);
        }
        String sql = builder.toString();

        // 替换 sqlSource 对象
        Configuration configuration = ms.getConfiguration();
        SqlSource sqlSource = xmlLanguageDriver.createSqlSource(configuration, "<script>\n" + sql + "\n</script>",
                null);
        metaObject.setValue("sqlSource", sqlSource);
        return sql;
    }

    /**
     * 查询记录，生成 select 语句。
     * <p>
     * 示例如下：
     *
     * <pre>
     * SELECT id,... FROM table_name
     * &lt;where&gt;
     *   &lt;if test=&quot;id != null&quot;&gt;
     *     and id = #{id}
     *   &lt;/if&gt;
     *   and is_deleted = 'N'
     * &lt;/where&gt;
     * </pre>
     *
     * @param tableInfo 数据库表结构信息
     * @param ms sql语句节点信息，会将生成的sql语句替换掉原有的 {@link MappedStatement#sqlSource}
     * @return 生成的sql语句
     * @see SelectListMapper#selectList(Object)
     */
    public String selectList(TableInfo tableInfo, MappedStatement ms) {
        return selectOne(tableInfo, ms);
    }

    /**
     * 查询记录，生成 select 语句。
     * <p>
     * 示例如下：
     *
     * <pre>
     * SELECT COUNT(1) FROM table_name
     * &lt;where&gt;
     *   &lt;if test=&quot;id != null&quot;&gt;
     *     and id = #{id}
     *   &lt;/if&gt;
     *   and is_deleted = 'N'
     * &lt;/where&gt;
     * </pre>
     *
     * @param tableInfo 数据库表结构信息
     * @param ms sql语句节点信息，会将生成的sql语句替换掉原有的 {@link MappedStatement#sqlSource}
     * @return 生成的sql语句
     * @see SelectCountMapper#selectCount(Object)
     */
    public String selectCount(TableInfo tableInfo, MappedStatement ms) {
        MetaObject metaObject = SystemMetaObject.forObject(ms);

        // 生成 sql 语句
        StringBuilder builder = new StringBuilder(2048);
        builder.append("SELECT COUNT(1) FROM ").append(TableMetaObject.getTableName(tableInfo)).append("\n");
        builder.append(tableInfo.getWhereConditionSql());
        String extendSql = getWhereExtend(ms);
        if (extendSql != null) {
            builder.insert(builder.length() - 8, extendSql);
        }
        String sql = builder.toString();

        // 替换 sqlSource 对象
        Configuration configuration = ms.getConfiguration();
        SqlSource sqlSource = xmlLanguageDriver.createSqlSource(configuration, "<script>\n" + sql + "\n</script>",
                null);
        metaObject.setValue("sqlSource", sqlSource);
        return sql;
    }

    /**
     * 查询记录，生成 select 语句。
     * <p>
     * 示例如下：
     *
     * <pre>
     * SELECT COUNT(1) FROM table_name
     * &lt;where&gt;
     *   &lt;if test=&quot;condition != null&quot;&gt;
     *     &lt;if test=&quot;condition.id != null&quot;&gt;
     *       and id = #{condition.id}
     *     &lt;/if&gt;
     *   &lt;/if&gt;
     *   &lt;if test=&quot;extraCondition != null&quot;&gt;
     *     &lt;include refid=&quot;Where_Extra_Condition&quot; /&gt;
     *   &lt;/if&gt;
     *   and is_deleted = 'N'
     * &lt;/where&gt;
     * </pre>
     *
     * @param tableInfo 数据库表结构信息
     * @param ms sql语句节点信息，会将生成的sql语句替换掉原有的 {@link MappedStatement#sqlSource}
     * @return 生成的sql语句
     * @see SelectCountMapper#selectCount2(Object, Map)
     */
    public String selectCount2(TableInfo tableInfo, MappedStatement ms) {
        MetaObject metaObject = SystemMetaObject.forObject(ms);

        // 生成 sql 语句
        StringBuilder builder = new StringBuilder(2048);
        builder.append("SELECT COUNT(1) FROM ").append(TableMetaObject.getTableName(tableInfo)).append("\n");
        builder.append(tableInfo.getWhereConditionWithParameterSql());
        StringBuilder extraConditionSql = getWhereExtraCondition(ms);
        if (extraConditionSql != null) {
            builder.insert(builder.length() - 8, extraConditionSql);
        }
        StringBuilder extendSql = getWhereExtendCondition(ms);
        if (extendSql != null) {
            builder.insert(builder.length() - 8, extendSql);
        }
        String sql = builder.toString();

        // 替换 sqlSource 对象
        Configuration configuration = ms.getConfiguration();
        SqlSource sqlSource = xmlLanguageDriver.createSqlSource(configuration, "<script>\n" + sql + "\n</script>",
                null);
        metaObject.setValue("sqlSource", sqlSource);
        return sql;
    }

    /**
     * 查询记录，生成 select 语句。
     * <p>
     * 示例如下：
     *
     * <pre>
     * SELECT id,... FROM table_name
     * &lt;where&gt;
     *   &lt;if test=&quot;id != null&quot;&gt;
     *     and id = #{id}
     *   &lt;/if&gt;
     *   and is_deleted = 'N'
     * &lt;/where&gt;
     * &lt;if test=&quot;orders != null and orders.size &gt; 0&quot;&gt;
     *   order by
     *   &lt;foreach collection=&quot;orders&quot; item=&quot;item&quot; separator=&quot;,&quot;&gt;
     *     ${item.property} ${item.direction}
     *   &lt;/foreach&gt;
     * &lt;/if&gt;
     * limit #{offset}, #{pageSize}
     * </pre>
     *
     * @param tableInfo 数据库表结构信息
     * @param ms sql语句节点信息，会将生成的sql语句替换掉原有的 {@link MappedStatement#sqlSource}
     * @return 生成的sql语句
     * @see SelectPageMapper#selectPage(Object, int, int, List)
     */
    public String selectPage(TableInfo tableInfo, MappedStatement ms) {
        MetaObject metaObject = SystemMetaObject.forObject(ms);
        // 替换 resultMap 对象
        List<ResultMap> resultMaps = Stream.of(tableInfo.getBaseResultMap())
                .collect(Collectors.collectingAndThen(Collectors.toList(), Collections::unmodifiableList));
        metaObject.setValue("resultMaps", resultMaps);

        // 生成 sql 语句
        StringBuilder builder = new StringBuilder(2048);
        builder.append("SELECT ").append(tableInfo.getSelectAllColumnsSql());
        builder.append(" FROM ").append(TableMetaObject.getTableName(tableInfo)).append("\n");
        builder.append(tableInfo.getWhereConditionWithParameterSql());
        StringBuilder extendSql = getWhereExtendCondition(ms);
        if (extendSql != null) {
            builder.insert(builder.length() - 8, extendSql);
        }
        builder.append("\n<if test=\"orders != null and orders.size > 0\">");
        builder.append("\n  order by");
        builder.append("\n  <foreach collection=\"orders\" item=\"item\" separator=\",\">");
        builder.append("\n    ${item.property} ${item.direction}");
        builder.append("\n  </foreach>");
        builder.append("\n</if>");
        builder.append("\nlimit #{offset}, #{pageSize}");
        String sql = builder.toString();

        // 替换 sqlSource 对象
        Configuration configuration = ms.getConfiguration();
        SqlSource sqlSource = xmlLanguageDriver.createSqlSource(configuration, "<script>\n" + sql + "\n</script>",
                null);
        metaObject.setValue("sqlSource", sqlSource);
        return sql;
    }

    /**
     * 查询记录，生成 select 语句。
     * <p>
     * 示例如下：
     *
     * <pre>
     * SELECT id,... FROM table_name
     * &lt;where&gt;
     *   &lt;if test=&quot;condition.id != null&quot;&gt;
     *     and id = #{condition.id}
     *   &lt;/if&gt;
     *   and is_deleted = 'N'
     *   &lt;if test=&quot;extraCondition != null&quot;&gt;
     *     &lt;include refid=&quot;Where_Extra_Condition&quot; /&gt;
     *   &lt;/if&gt;
     * &lt;/where&gt;
     * &lt;if test=&quot;orders != null and orders.size &gt; 0&quot;&gt;
     *   order by
     *   &lt;foreach collection=&quot;orders&quot; item=&quot;item&quot; separator=&quot;,&quot;&gt;
     *     ${item.property} ${item.direction}
     *   &lt;/foreach&gt;
     * &lt;/if&gt;
     * limit #{offset}, #{pageSize}
     * </pre>
     *
     * @param tableInfo 数据库表结构信息
     * @param ms sql语句节点信息，会将生成的sql语句替换掉原有的 {@link MappedStatement#sqlSource}
     * @return 生成的sql语句
     * @see SelectPageMapper#selectPage2(Object, Map, int, int, List)
     */
    public String selectPage2(TableInfo tableInfo, MappedStatement ms) {
        MetaObject metaObject = SystemMetaObject.forObject(ms);
        // 替换 resultMap 对象
        List<ResultMap> resultMaps = Stream.of(tableInfo.getBaseResultMap())
                .collect(Collectors.collectingAndThen(Collectors.toList(), Collections::unmodifiableList));
        metaObject.setValue("resultMaps", resultMaps);

        // 生成 sql 语句
        StringBuilder builder = new StringBuilder(2048);
        builder.append("SELECT ").append(tableInfo.getSelectAllColumnsSql());
        builder.append(" FROM ").append(TableMetaObject.getTableName(tableInfo)).append("\n");
        builder.append(tableInfo.getWhereConditionWithParameterSql());
        StringBuilder extraConditionSql = getWhereExtraCondition(ms);
        if (extraConditionSql != null) {
            builder.insert(builder.length() - 8, extraConditionSql);
        }
        StringBuilder extendSql = getWhereExtendCondition(ms);
        if (extendSql != null) {
            builder.insert(builder.length() - 8, extendSql);
        }
        builder.append("\n<if test=\"orders != null and orders.size > 0\">");
        builder.append("\n  order by");
        builder.append("\n  <foreach collection=\"orders\" item=\"item\" separator=\",\">");
        builder.append("\n    ${item.property} ${item.direction}");
        builder.append("\n  </foreach>");
        builder.append("\n</if>");
        builder.append("\nlimit #{offset}, #{pageSize}");
        String sql = builder.toString();

        // 替换 sqlSource 对象
        Configuration configuration = ms.getConfiguration();
        SqlSource sqlSource = xmlLanguageDriver.createSqlSource(configuration, "<script>\n" + sql + "\n</script>",
                null);
        metaObject.setValue("sqlSource", sqlSource);
        return sql;
    }

    /**
     * 查询记录，生成 select 语句。
     * <p>
     * 示例如下：
     *
     * <pre>
     * SELECT id,... FROM table_name
     * &lt;where&gt;
     *  AND id = #{id}
     *  AND is_deleted = 'N'
     * &lt;/where&gt;
     * </pre>
     *
     * @param tableInfo 数据库表结构信息
     * @param ms sql语句节点信息，会将生成的sql语句替换掉原有的 {@link MappedStatement#sqlSource}
     * @return 生成的sql语句
     * @see SelectByPrimaryKeyMapper#selectByPrimaryKey(Serializable)
     * @see SelectByPrimaryKeyMapper#selectWithPrimaryKey(Object)
     */
    public String selectByPrimaryKey(TableInfo tableInfo, MappedStatement ms) {
        MetaObject metaObject = SystemMetaObject.forObject(ms);
        // 替换 resultMap 对象
        List<ResultMap> resultMaps = Stream.of(tableInfo.getBaseResultMap())
                .collect(Collectors.collectingAndThen(Collectors.toList(), Collections::unmodifiableList));
        metaObject.setValue("resultMaps", resultMaps);

        // 生成 sql 语句
        StringBuilder builder = new StringBuilder(1024);
        builder.append("SELECT ").append(tableInfo.getSelectAllColumnsSql());
        builder.append(" FROM ").append(TableMetaObject.getTableName(tableInfo)).append("\n");
        builder.append(tableInfo.getWherePrimaryKeySql());
        String sql = builder.toString();

        // 替换 sqlSource 对象
        Configuration configuration = ms.getConfiguration();
        SqlSource sqlSource = xmlLanguageDriver.createSqlSource(configuration, "<script>\n" + sql + "\n</script>",
                null);
        metaObject.setValue("sqlSource", sqlSource);
        return sql;
    }

    /**
     * 查询记录，生成 select 语句。
     * <p>
     * 示例如下：
     *
     * <pre>
     * SELECT id,... FROM table_name
     * &lt;where&gt;
     *  AND id = #{id}
     *  AND is_deleted = 'N'
     * &lt;/where&gt;
     * </pre>
     *
     * @param tableInfo 数据库表结构信息
     * @param ms sql语句节点信息，会将生成的sql语句替换掉原有的 {@link MappedStatement#sqlSource}
     * @return 生成的sql语句
     * @see SelectByPrimaryKeyMapper#selectWithPrimaryKey(Object)
     */
    public String selectWithPrimaryKey(TableInfo tableInfo, MappedStatement ms) {
        return selectByPrimaryKey(tableInfo, ms);
    }

    /**
     * 查询记录，生成 select 语句。
     * <p>
     * 示例如下：
     *
     * <pre>
     * SELECT id,... FROM table_name
     * &lt;where&gt;
     *  AND id in
     *  &lt;foreach collection=&quot;ids&quot; item=&quot;item&quot; separator=&quot;,&quot; open=&quot;(&quot; close=&quot;)&quot;&gt;
     *    #{item}
     *  &lt;/foreach&gt;
     *  AND is_deleted = 'N'
     * &lt;/where&gt;
     * </pre>
     *
     * @param tableInfo 数据库表结构信息
     * @param ms sql语句节点信息，会将生成的sql语句替换掉原有的 {@link MappedStatement#sqlSource}
     * @return 生成的sql语句
     * @see SelectByPrimaryKeyMapper#selectListByPrimaryKey(Collection)
     */
    public String selectListByPrimaryKey(TableInfo tableInfo, MappedStatement ms) {
        MetaObject metaObject = SystemMetaObject.forObject(ms);
        // 替换 resultMap 对象
        List<ResultMap> resultMaps = Stream.of(tableInfo.getBaseResultMap())
                .collect(Collectors.collectingAndThen(Collectors.toList(), Collections::unmodifiableList));
        metaObject.setValue("resultMaps", resultMaps);

        // 生成 sql 语句
        StringBuilder builder = new StringBuilder(1024);
        builder.append("SELECT ").append(tableInfo.getSelectAllColumnsSql());
        builder.append(" FROM ").append(TableMetaObject.getTableName(tableInfo)).append("\n");
        builder.append(TableMetaObject.builderWhereByListPrimaryKeySql(tableInfo, false));
        String sql = builder.toString();

        // 替换 sqlSource 对象
        Configuration configuration = ms.getConfiguration();
        SqlSource sqlSource = xmlLanguageDriver.createSqlSource(configuration, "<script>\n" + sql + "\n</script>",
                null);
        metaObject.setValue("sqlSource", sqlSource);
        return sql;
    }

    /**
     * 查询记录，生成 select 语句。
     * <p>
     * 示例如下：
     *
     * <pre>
     * SELECT id,... FROM table_name
     * &lt;where&gt;
     *  AND id in
     *  &lt;foreach collection=&quot;ids&quot; item=&quot;item&quot; separator=&quot;,&quot; open=&quot;(&quot; close=&quot;)&quot;&gt;
     *    #{item}
     *  &lt;/foreach&gt;
     *  AND is_deleted = 'N'
     * &lt;/where&gt;
     * </pre>
     *
     * @param tableInfo 数据库表结构信息
     * @param ms sql语句节点信息，会将生成的sql语句替换掉原有的 {@link MappedStatement#sqlSource}
     * @return 生成的sql语句
     * @see SelectByPrimaryKeyMapper#selectListWithPrimaryKey(Collection)
     */
    public String selectListWithPrimaryKey(TableInfo tableInfo, MappedStatement ms) {
        MetaObject metaObject = SystemMetaObject.forObject(ms);
        // 替换 resultMap 对象
        List<ResultMap> resultMaps = Stream.of(tableInfo.getBaseResultMap())
                .collect(Collectors.collectingAndThen(Collectors.toList(), Collections::unmodifiableList));
        metaObject.setValue("resultMaps", resultMaps);

        // 生成 sql 语句
        StringBuilder builder = new StringBuilder(1024);
        builder.append("SELECT ").append(tableInfo.getSelectAllColumnsSql());
        builder.append(" FROM ").append(TableMetaObject.getTableName(tableInfo)).append("\n");
        builder.append(TableMetaObject.builderWhereWithListPrimaryKeySql(tableInfo, false));
        String sql = builder.toString();

        // 替换 sqlSource 对象
        Configuration configuration = ms.getConfiguration();
        SqlSource sqlSource = xmlLanguageDriver.createSqlSource(configuration, "<script>\n" + sql + "\n</script>",
                null);
        metaObject.setValue("sqlSource", sqlSource);
        return sql;
    }
}
