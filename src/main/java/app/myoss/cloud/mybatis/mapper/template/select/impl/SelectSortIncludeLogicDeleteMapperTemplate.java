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
import app.myoss.cloud.mybatis.mapper.template.select.SelectListIncludeLogicDeleteMapper;
import app.myoss.cloud.mybatis.table.TableInfo;
import app.myoss.cloud.mybatis.table.TableMetaObject;
import app.myoss.cloud.mybatis.table.annotation.Column;

/**
 * 生成 select order by MappedStatement 模版类
 *
 * @author Jerry.Chen
 * @since 2018年5月10日 上午1:09:17
 */
public class SelectSortIncludeLogicDeleteMapperTemplate extends AbstractMapperTemplate {

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
     * &lt;/where&gt;
     * &lt;if test=&quot;orders != null and orders.size &gt; 0&quot;&gt;
     *   order by
     *   &lt;foreach collection=&quot;orders&quot; item=&quot;item&quot; separator=&quot;,&quot;&gt;
     *     ${item.property} ${item.direction}
     *   &lt;/foreach&gt;
     * &lt;/if&gt;
     * </pre>
     *
     * @param tableInfo 数据库表结构信息
     * @param ms sql语句节点信息，会将生成的sql语句替换掉原有的 {@link MappedStatement#sqlSource}
     * @return 生成的sql语句
     * @see SelectListIncludeLogicDeleteMapper#selectListWithSortIncludeLogicDelete(Object,
     *      List)
     */
    public String selectListWithSortIncludeLogicDelete(TableInfo tableInfo, MappedStatement ms) {
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
     * </pre>
     *
     * @param tableInfo 数据库表结构信息
     * @param ms sql语句节点信息，会将生成的sql语句替换掉原有的 {@link MappedStatement#sqlSource}
     * @return 生成的sql语句
     * @see SelectListIncludeLogicDeleteMapper#selectListWithSortIncludeLogicDelete2(Object,
     *      java.util.Map, List)
     */
    public String selectListWithSortIncludeLogicDelete2(TableInfo tableInfo, MappedStatement ms) {
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
        String sql = builder.toString();

        // 替换 sqlSource 对象
        Configuration configuration = ms.getConfiguration();
        SqlSource sqlSource = xmlLanguageDriver.createSqlSource(configuration, "<script>\n" + sql + "\n</script>",
                null);
        metaObject.setValue("sqlSource", sqlSource);
        return sql;
    }
}
