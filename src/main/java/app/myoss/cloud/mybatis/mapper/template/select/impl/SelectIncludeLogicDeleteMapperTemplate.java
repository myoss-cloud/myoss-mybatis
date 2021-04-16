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
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.ResultMap;
import org.apache.ibatis.mapping.SqlSource;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.SystemMetaObject;
import org.apache.ibatis.session.Configuration;

import app.myoss.cloud.mybatis.mapper.template.AbstractMapperTemplate;
import app.myoss.cloud.mybatis.mapper.template.select.SelectByPrimaryKeyIncludeLogicDeleteMapper;
import app.myoss.cloud.mybatis.mapper.template.select.SelectCountIncludeLogicDeleteMapper;
import app.myoss.cloud.mybatis.mapper.template.select.SelectListIncludeLogicDeleteMapper;
import app.myoss.cloud.mybatis.mapper.template.select.SelectOneIncludeLogicDeleteMapper;
import app.myoss.cloud.mybatis.mapper.template.select.SelectPageIncludeLogicDeleteMapper;
import app.myoss.cloud.mybatis.table.TableInfo;
import app.myoss.cloud.mybatis.table.TableMetaObject;
import app.myoss.cloud.mybatis.table.annotation.Column;

/**
 * 生成 select MappedStatement 模版类
 *
 * @author Jerry.Chen
 * @since 2018年6月10日 下午10:57:24
 */
public class SelectIncludeLogicDeleteMapperTemplate extends AbstractMapperTemplate {

    /**
     * 查询记录，不会过滤掉已经被标记为逻辑删除（{@link Column#logicDelete}）的数据，生成 select 语句。
     * <p>
     * 示例如下：
     *
     * <pre>
     * SELECT id,... FROM table_name
     * &lt;where&gt;
     *   &lt;if test=&quot;id != null&quot;&gt;
     *     and id = #{id}
     *   &lt;/if&gt;
     * &lt;/where&gt;
     * </pre>
     *
     * @param tableInfo 数据库表结构信息
     * @param ms sql语句节点信息，会将生成的sql语句替换掉原有的 {@link MappedStatement#sqlSource}
     * @return 生成的sql语句
     * @see SelectOneIncludeLogicDeleteMapper#selectOneIncludeLogicDelete(Object)
     */
    public String selectOneIncludeLogicDelete(TableInfo tableInfo, MappedStatement ms) {
        MetaObject metaObject = SystemMetaObject.forObject(ms);
        // 替换 resultMap 对象
        List<ResultMap> resultMaps = Stream.of(tableInfo.getBaseResultMap())
                .collect(Collectors.collectingAndThen(Collectors.toList(), Collections::unmodifiableList));
        metaObject.setValue("resultMaps", resultMaps);

        // 生成 sql 语句
        StringBuilder builder = new StringBuilder(2048);
        builder.append("SELECT ").append(tableInfo.getSelectAllColumnsSql());
        builder.append(" FROM ").append(TableMetaObject.getTableName(tableInfo)).append("\n");
        builder.append(tableInfo.getWhereConditionIncludeLogicDeleteSql());
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
     * 查询记录，不会过滤掉已经被标记为逻辑删除（{@link Column#logicDelete}）的数据，生成 select 语句。
     * <p>
     * 示例如下：
     *
     * <pre>
     * SELECT id,... FROM table_name
     * &lt;where&gt;
     *   &lt;if test=&quot;id != null&quot;&gt;
     *     and id = #{id}
     *   &lt;/if&gt;
     * &lt;/where&gt;
     * </pre>
     *
     * @param tableInfo 数据库表结构信息
     * @param ms sql语句节点信息，会将生成的sql语句替换掉原有的 {@link MappedStatement#sqlSource}
     * @return 生成的sql语句
     * @see SelectListIncludeLogicDeleteMapper#selectListIncludeLogicDelete(Object)
     */
    public String selectListIncludeLogicDelete(TableInfo tableInfo, MappedStatement ms) {
        return selectOneIncludeLogicDelete(tableInfo, ms);
    }

    /**
     * 查询记录，不会过滤掉已经被标记为逻辑删除（{@link Column#logicDelete}）的数据，生成 select 语句。
     * <p>
     * 示例如下：
     *
     * <pre>
     * SELECT COUNT(1) FROM table_name
     * &lt;where&gt;
     *   &lt;if test=&quot;id != null&quot;&gt;
     *     and id = #{id}
     *   &lt;/if&gt;
     * &lt;/where&gt;
     * </pre>
     *
     * @param tableInfo 数据库表结构信息
     * @param ms sql语句节点信息，会将生成的sql语句替换掉原有的 {@link MappedStatement#sqlSource}
     * @return 生成的sql语句
     * @see SelectCountIncludeLogicDeleteMapper#selectCountIncludeLogicDelete(Object)
     */
    public String selectCountIncludeLogicDelete(TableInfo tableInfo, MappedStatement ms) {
        MetaObject metaObject = SystemMetaObject.forObject(ms);

        // 生成 sql 语句
        StringBuilder builder = new StringBuilder(2048);
        builder.append("SELECT COUNT(1) FROM ").append(TableMetaObject.getTableName(tableInfo)).append("\n");
        builder.append(tableInfo.getWhereConditionIncludeLogicDeleteSql());
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
     * 查询记录，不会过滤掉已经被标记为逻辑删除（{@link Column#logicDelete}）的数据，生成 select 语句。
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
     * &lt;/where&gt;
     * </pre>
     *
     * @param tableInfo 数据库表结构信息
     * @param ms sql语句节点信息，会将生成的sql语句替换掉原有的 {@link MappedStatement#sqlSource}
     * @return 生成的sql语句
     * @see SelectCountIncludeLogicDeleteMapper#selectCountIncludeLogicDelete2(Object,
     *      java.util.Map)
     */
    public String selectCountIncludeLogicDelete2(TableInfo tableInfo, MappedStatement ms) {
        MetaObject metaObject = SystemMetaObject.forObject(ms);

        // 生成 sql 语句
        StringBuilder builder = new StringBuilder(2048);
        builder.append("SELECT COUNT(1) FROM ").append(TableMetaObject.getTableName(tableInfo)).append("\n");
        builder.append(tableInfo.getWhereConditionWithParameterIncludeLogicDeleteSql());
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
     * 查询记录，不会过滤掉已经被标记为逻辑删除（{@link Column#logicDelete}）的数据，生成 select 语句。
     * <p>
     * 示例如下：
     *
     * <pre>
     * SELECT id,... FROM table_name
     * &lt;where&gt;
     *   &lt;if test=&quot;id != null&quot;&gt;
     *     and id = #{id}
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
     * @see SelectPageIncludeLogicDeleteMapper#selectPageIncludeLogicDelete(Object,
     *      int, int, List)
     */
    public String selectPageIncludeLogicDelete(TableInfo tableInfo, MappedStatement ms) {
        MetaObject metaObject = SystemMetaObject.forObject(ms);
        // 替换 resultMap 对象
        List<ResultMap> resultMaps = Stream.of(tableInfo.getBaseResultMap())
                .collect(Collectors.collectingAndThen(Collectors.toList(), Collections::unmodifiableList));
        metaObject.setValue("resultMaps", resultMaps);

        // 生成 sql 语句
        StringBuilder builder = new StringBuilder(2048);
        builder.append("SELECT ").append(tableInfo.getSelectAllColumnsSql());
        builder.append(" FROM ").append(TableMetaObject.getTableName(tableInfo)).append("\n");
        builder.append(tableInfo.getWhereConditionWithParameterIncludeLogicDeleteSql());
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
     * 查询记录，不会过滤掉已经被标记为逻辑删除（{@link Column#logicDelete}）的数据，生成 select 语句。
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
     * @see SelectPageIncludeLogicDeleteMapper#selectPageIncludeLogicDelete2(Object,
     *      java.util.Map, int, int, List)
     */
    public String selectPageIncludeLogicDelete2(TableInfo tableInfo, MappedStatement ms) {
        MetaObject metaObject = SystemMetaObject.forObject(ms);
        // 替换 resultMap 对象
        List<ResultMap> resultMaps = Stream.of(tableInfo.getBaseResultMap())
                .collect(Collectors.collectingAndThen(Collectors.toList(), Collections::unmodifiableList));
        metaObject.setValue("resultMaps", resultMaps);

        // 生成 sql 语句
        StringBuilder builder = new StringBuilder(2048);
        builder.append("SELECT ").append(tableInfo.getSelectAllColumnsSql());
        builder.append(" FROM ").append(TableMetaObject.getTableName(tableInfo)).append("\n");
        builder.append(tableInfo.getWhereConditionWithParameterIncludeLogicDeleteSql());
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
     * 查询记录，不会过滤掉已经被标记为逻辑删除（{@link Column#logicDelete}）的数据，生成 select 语句。
     * <p>
     * 示例如下：
     *
     * <pre>
     * SELECT id,... FROM table_name
     * &lt;where&gt;
     *  AND id = #{id}
     * &lt;/where&gt;
     * </pre>
     *
     * @param tableInfo 数据库表结构信息
     * @param ms sql语句节点信息，会将生成的sql语句替换掉原有的 {@link MappedStatement#sqlSource}
     * @return 生成的sql语句
     * @see SelectByPrimaryKeyIncludeLogicDeleteMapper#selectByPrimaryKeyIncludeLogicDelete(Serializable)
     */
    public String selectByPrimaryKeyIncludeLogicDelete(TableInfo tableInfo, MappedStatement ms) {
        MetaObject metaObject = SystemMetaObject.forObject(ms);
        // 替换 resultMap 对象
        List<ResultMap> resultMaps = Stream.of(tableInfo.getBaseResultMap())
                .collect(Collectors.collectingAndThen(Collectors.toList(), Collections::unmodifiableList));
        metaObject.setValue("resultMaps", resultMaps);

        // 生成 sql 语句
        StringBuilder builder = new StringBuilder(1024);
        builder.append("SELECT ").append(tableInfo.getSelectAllColumnsSql());
        builder.append(" FROM ").append(TableMetaObject.getTableName(tableInfo)).append("\n");
        builder.append(tableInfo.getWherePrimaryKeyIncludeLogicDeleteSql());
        String sql = builder.toString();

        // 替换 sqlSource 对象
        Configuration configuration = ms.getConfiguration();
        SqlSource sqlSource = xmlLanguageDriver.createSqlSource(configuration, "<script>\n" + sql + "\n</script>",
                null);
        metaObject.setValue("sqlSource", sqlSource);
        return sql;
    }

    /**
     * 查询记录，不会过滤掉已经被标记为逻辑删除（{@link Column#logicDelete}）的数据，生成 select 语句。
     * <p>
     * 示例如下：
     *
     * <pre>
     * SELECT id,... FROM table_name
     * &lt;where&gt;
     *  AND id = #{id}
     * &lt;/where&gt;
     * </pre>
     *
     * @param tableInfo 数据库表结构信息
     * @param ms sql语句节点信息，会将生成的sql语句替换掉原有的 {@link MappedStatement#sqlSource}
     * @return 生成的sql语句
     * @see SelectByPrimaryKeyIncludeLogicDeleteMapper#selectWithPrimaryKeyIncludeLogicDelete(Object)
     */
    public String selectWithPrimaryKeyIncludeLogicDelete(TableInfo tableInfo, MappedStatement ms) {
        return selectByPrimaryKeyIncludeLogicDelete(tableInfo, ms);
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
     * &lt;/where&gt;
     * </pre>
     *
     * @param tableInfo 数据库表结构信息
     * @param ms sql语句节点信息，会将生成的sql语句替换掉原有的 {@link MappedStatement#sqlSource}
     * @return 生成的sql语句
     * @see SelectByPrimaryKeyIncludeLogicDeleteMapper#selectListByPrimaryKeyIncludeLogicDelete(Collection)
     */
    public String selectListByPrimaryKeyIncludeLogicDelete(TableInfo tableInfo, MappedStatement ms) {
        MetaObject metaObject = SystemMetaObject.forObject(ms);
        // 替换 resultMap 对象
        List<ResultMap> resultMaps = Stream.of(tableInfo.getBaseResultMap())
                .collect(Collectors.collectingAndThen(Collectors.toList(), Collections::unmodifiableList));
        metaObject.setValue("resultMaps", resultMaps);

        // 生成 sql 语句
        StringBuilder builder = new StringBuilder(1024);
        builder.append("SELECT ").append(tableInfo.getSelectAllColumnsSql());
        builder.append(" FROM ").append(TableMetaObject.getTableName(tableInfo)).append("\n");
        builder.append(TableMetaObject.builderWhereByListPrimaryKeySql(tableInfo, true));
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
     * &lt;/where&gt;
     * </pre>
     *
     * @param tableInfo 数据库表结构信息
     * @param ms sql语句节点信息，会将生成的sql语句替换掉原有的 {@link MappedStatement#sqlSource}
     * @return 生成的sql语句
     * @see SelectByPrimaryKeyIncludeLogicDeleteMapper#selectListWithPrimaryKeyIncludeLogicDelete(Collection)
     */
    public String selectListWithPrimaryKeyIncludeLogicDelete(TableInfo tableInfo, MappedStatement ms) {
        MetaObject metaObject = SystemMetaObject.forObject(ms);
        // 替换 resultMap 对象
        List<ResultMap> resultMaps = Stream.of(tableInfo.getBaseResultMap())
                .collect(Collectors.collectingAndThen(Collectors.toList(), Collections::unmodifiableList));
        metaObject.setValue("resultMaps", resultMaps);

        // 生成 sql 语句
        StringBuilder builder = new StringBuilder(1024);
        builder.append("SELECT ").append(tableInfo.getSelectAllColumnsSql());
        builder.append(" FROM ").append(TableMetaObject.getTableName(tableInfo)).append("\n");
        builder.append(TableMetaObject.builderWhereWithListPrimaryKeySql(tableInfo, true));
        String sql = builder.toString();

        // 替换 sqlSource 对象
        Configuration configuration = ms.getConfiguration();
        SqlSource sqlSource = xmlLanguageDriver.createSqlSource(configuration, "<script>\n" + sql + "\n</script>",
                null);
        metaObject.setValue("sqlSource", sqlSource);
        return sql;
    }
}
