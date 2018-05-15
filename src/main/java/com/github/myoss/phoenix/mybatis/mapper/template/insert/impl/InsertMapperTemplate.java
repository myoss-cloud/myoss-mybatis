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

package com.github.myoss.phoenix.mybatis.mapper.template.insert.impl;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.apache.ibatis.builder.xml.XMLStatementBuilder;
import org.apache.ibatis.executor.keygen.Jdbc3KeyGenerator;
import org.apache.ibatis.executor.keygen.KeyGenerator;
import org.apache.ibatis.executor.keygen.NoKeyGenerator;
import org.apache.ibatis.executor.keygen.SelectKeyGenerator;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.mapping.SqlSource;
import org.apache.ibatis.mapping.StatementType;
import org.apache.ibatis.parsing.XNode;
import org.apache.ibatis.parsing.XPathParser;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.SystemMetaObject;
import org.apache.ibatis.scripting.LanguageDriver;
import org.apache.ibatis.session.Configuration;

import com.github.myoss.phoenix.mybatis.executor.keygen.SequenceKeyGenerator;
import com.github.myoss.phoenix.mybatis.mapper.template.AbstractMapperTemplate;
import com.github.myoss.phoenix.mybatis.mapper.template.insert.InsertAllColumnMapper;
import com.github.myoss.phoenix.mybatis.mapper.template.insert.InsertBatchMapper;
import com.github.myoss.phoenix.mybatis.mapper.template.insert.InsertMapper;
import com.github.myoss.phoenix.mybatis.table.TableColumnInfo;
import com.github.myoss.phoenix.mybatis.table.TableInfo;
import com.github.myoss.phoenix.mybatis.table.TableMetaObject;
import com.github.myoss.phoenix.mybatis.table.TableSequence;
import com.github.myoss.phoenix.mybatis.table.annotation.FillRule;
import com.github.myoss.phoenix.mybatis.table.annotation.GenerationType;
import com.github.myoss.phoenix.mybatis.table.annotation.SequenceGenerator.Order;

/**
 * 生成通用 insert MappedStatement 模版类
 *
 * @author Jerry.Chen 2018年4月29日 下午4:46:05
 */
public class InsertMapperTemplate extends AbstractMapperTemplate {
    /**
     * 生成 selectKey 序列，并增加到 {@link Configuration} 全局配置中
     *
     * @see GenerationType#SELECT_KEY
     * @see XMLStatementBuilder#parseSelectKeyNodes
     */
    private SelectKeyGenerator addSelectKeyGenerator(String id, TableSequence sequence, Class<?> parameterTypeClass,
                                                     Configuration configuration, LanguageDriver langDriver,
                                                     String parentId) {
        Class<?> resultTypeClass = sequence.getResultType()[0];
        StatementType statementType = sequence.getStatementType();
        String keyProperty = StringUtils.join(sequence.getKeyProperties(), ",");
        String keyColumn = StringUtils.join(sequence.getKeyColumns(), ",");
        boolean executeBefore = sequence.getOrder().equals(Order.BEFORE);

        // 生成 selectKey sql
        String selectSql = sequence.getSql();
        StringBuilder sqlXml = new StringBuilder(150 + selectSql.length());
        sqlXml.append("<selectKey keyProperty=\"").append(keyProperty).append("\"");
        sqlXml.append(" keyColumn=\"").append(keyColumn).append("\"");
        if (resultTypeClass != null) {
            sqlXml.append(" resultType=\"").append(resultTypeClass.getCanonicalName()).append("\"");
        }
        sqlXml.append(" statementType=\"").append(statementType).append("\"");
        sqlXml.append(" order=\"").append(sequence.getOrder()).append("\"");
        sqlXml.append(">").append(selectSql).append("</selectKey>");
        XPathParser xPathParser = new XPathParser(sqlXml.toString());
        XNode nodeToHandle = xPathParser.evalNode("selectKey");

        SqlSource sqlSource = langDriver.createSqlSource(configuration, nodeToHandle, parameterTypeClass);
        SqlCommandType sqlCommandType = SqlCommandType.SELECT;
        KeyGenerator keyGenerator = new NoKeyGenerator();
        MapperBuilderAssistant builderAssistant = new MapperBuilderAssistant(configuration, parentId);
        builderAssistant.setCurrentNamespace(StringUtils.substringBeforeLast(parentId, "."));
        builderAssistant.addMappedStatement(id, sqlSource, statementType, sqlCommandType, null, null, null,
                parameterTypeClass, null, resultTypeClass, null, false, false, false, keyGenerator, keyProperty,
                keyColumn, null, langDriver, null);

        id = builderAssistant.applyCurrentNamespace(id, false);

        MappedStatement keyStatement = configuration.getMappedStatement(id, false);
        SelectKeyGenerator selectKeyGenerator = new SelectKeyGenerator(keyStatement, executeBefore);
        configuration.addKeyGenerator(id, selectKeyGenerator);
        return selectKeyGenerator;
    }

    /**
     * 生成"序列生成器"
     */
    private GenerationType addKeyGenerator(TableInfo tableInfo, MetaObject metaObject, String id,
                                           Configuration configuration) {
        TableSequence tableSequence = tableInfo.getTableSequence();
        if (tableSequence == null) {
            return null;
        }
        GenerationType strategy = tableSequence.getStrategy();
        KeyGenerator keyGenerator;
        if (strategy == GenerationType.USE_GENERATED_KEYS) {
            keyGenerator = new Jdbc3KeyGenerator();
        } else if (strategy == GenerationType.SELECT_KEY) {
            String selectId = StringUtils.substringAfterLast(id, ".") + SelectKeyGenerator.SELECT_KEY_SUFFIX;
            keyGenerator = addSelectKeyGenerator(selectId, tableSequence, tableInfo.getEntityClass(), configuration,
                    xmlLanguageDriver, id);
        } else if (strategy == GenerationType.SEQUENCE_KEY) {
            boolean executeBefore = tableSequence.getOrder().equals(Order.BEFORE);
            keyGenerator = new SequenceKeyGenerator(tableInfo, executeBefore);
        } else {
            throw new UnsupportedOperationException("keyGenerator strategy " + strategy.getType() + " unsupported");
        }
        metaObject.setValue("keyGenerator", keyGenerator);
        return strategy;
    }

    /**
     * 创建新的记录，生成 insert 语句。
     * <p>
     * 示例如下：
     *
     * <pre>
     * INSERT INTO table_name (id, ...) VALUES (#{id}, ...)
     * </pre>
     *
     * @param tableInfo 数据库表结构信息
     * @param ms sql语句节点信息，会将生成的sql语句替换掉原有的 {@link MappedStatement#sqlSource}
     * @return 生成的sql语句
     * @see InsertAllColumnMapper#insertAllColumn(Object)
     */
    public String insertAllColumn(TableInfo tableInfo, MappedStatement ms) {
        MetaObject metaObject = SystemMetaObject.forObject(ms);
        String id = ms.getId();
        Configuration configuration = ms.getConfiguration();

        // 生成"序列生成器"
        addKeyGenerator(tableInfo, metaObject, id, configuration);

        // 生成 sql 语句
        StringBuilder builder = new StringBuilder(2048);
        builder.append("INSERT INTO ").append(TableMetaObject.getTableName(tableInfo)).append(" (");
        StringBuilder values = new StringBuilder(1024);
        for (TableColumnInfo columnInfo : tableInfo.getColumns()) {
            if (!columnInfo.isInsertable() || columnInfo.isAutoIncrement()) {
                continue;
            }
            builder.append(columnInfo.getActualColumn()).append(", ");

            values.append("#{").append(columnInfo.getProperty());
            if (columnInfo.getJdbcType() != null) {
                values.append(",jdbcType=BIGINT");
            }
            values.append("}, ");
        }
        values.deleteCharAt(values.length() - 2);
        builder.deleteCharAt(builder.length() - 2).append(")\n");
        builder.append(" VALUES (").append(values).append(")");
        String sql = builder.toString();

        // 替换 sqlSource 对象
        SqlSource sqlSource = xmlLanguageDriver
                .createSqlSource(configuration, "<script>\n" + sql + "\n</script>", null);
        metaObject.setValue("sqlSource", sqlSource);
        return sql;
    }

    /**
     * 创建新的记录，生成 insert 语句。
     * <p>
     * 示例如下：
     *
     * <pre>
     * INSERT INTO table_name
     * &lt;trim prefix=&quot;(&quot; suffix=&quot;)&quot; suffixOverrides=&quot;,&quot;&gt;
     *   &lt;if test=&quot;id != null&quot;&gt;
     *     id,
     *   &lt;/if&gt;
     * &lt;/trim&gt;
     * &lt;trim prefix=&quot;values (&quot; suffix=&quot;)&quot; suffixOverrides=&quot;,&quot;&gt;
     *   &lt;if test=&quot;id != null&quot;&gt;
     *     #{id},
     *   &lt;/if&gt;
     * &lt;/trim&gt;
     * </pre>
     *
     * @param tableInfo 数据库表结构信息
     * @param ms sql语句节点信息，会将生成的sql语句替换掉原有的 {@link MappedStatement#sqlSource}
     * @return 生成的sql语句
     * @see InsertMapper#insert(Object)
     */
    public String insert(TableInfo tableInfo, MappedStatement ms) {
        MetaObject metaObject = SystemMetaObject.forObject(ms);
        String id = ms.getId();
        Configuration configuration = ms.getConfiguration();

        // 生成"序列生成器"
        GenerationType generationType = addKeyGenerator(tableInfo, metaObject, id, configuration);

        // 生成 sql 语句
        StringBuilder builder = new StringBuilder(4096);
        builder.append("INSERT INTO ").append(TableMetaObject.getTableName(tableInfo)).append("\n");
        builder.append("<trim prefix=\"(\" suffix=\")\" suffixOverrides=\",\">\n");
        StringBuilder values = new StringBuilder(2048);
        values.append("<trim prefix=\"values (\" suffix=\")\" suffixOverrides=\",\">\n");
        for (TableColumnInfo columnInfo : tableInfo.getColumns()) {
            if (!columnInfo.isInsertable() || columnInfo.isAutoIncrement()) {
                continue;
            }
            // 如果是主键字段（不是自动增长的主键）或者字段有自动填充的规则，不加 if 表达式判断
            boolean fillInsert = (columnInfo.isPrimaryKey() && GenerationType.USE_GENERATED_KEYS != generationType)
                    || columnInfo.haveFillRule(FillRule.INSERT);
            if (!fillInsert) {
                builder.append("  <if test=\"").append(columnInfo.getProperty()).append(" != null\">\n");
            }
            builder.append("    ").append(columnInfo.getActualColumn()).append(",\n");
            if (!fillInsert) {
                builder.append("  </if>\n");
            }

            if (!fillInsert) {
                values.append("  <if test=\"").append(columnInfo.getProperty()).append(" != null\">\n");
            }
            values.append("    #{").append(columnInfo.getProperty());
            if (columnInfo.getJdbcType() != null) {
                values.append(",jdbcType=BIGINT");
            }
            values.append("},\n");
            if (!fillInsert) {
                values.append("  </if>\n");
            }
        }
        builder.append("</trim>\n").append(values).append("</trim>\n");
        String sql = builder.toString();

        // 替换 sqlSource 对象
        SqlSource sqlSource = xmlLanguageDriver
                .createSqlSource(configuration, "<script>\n" + sql + "\n</script>", null);
        metaObject.setValue("sqlSource", sqlSource);
        return sql;
    }

    /**
     * 批量创建新的记录，生成 insert 语句。
     * <p>
     * 示例如下：
     *
     * <pre>
     * INSERT INTO table_name (id, ...) VALUES
     * &lt;foreach collection=&quot;list&quot; item=&quot;item&quot; separator=&quot;,&quot;&gt;
     * (#{id}, ...)
     * &lt;/foreach&gt;
     * </pre>
     *
     * @param tableInfo 数据库表结构信息
     * @param ms sql语句节点信息，会将生成的sql语句替换掉原有的 {@link MappedStatement#sqlSource}
     * @return 生成的sql语句
     * @see InsertBatchMapper#insertBatch(List)
     */
    public String insertBatch(TableInfo tableInfo, MappedStatement ms) {
        MetaObject metaObject = SystemMetaObject.forObject(ms);
        String id = ms.getId();
        Configuration configuration = ms.getConfiguration();

        // 生成"序列生成器"
        addKeyGenerator(tableInfo, metaObject, id, configuration);

        // 生成 sql 语句
        StringBuilder builder = new StringBuilder(2048);
        builder.append("INSERT INTO ").append(TableMetaObject.getTableName(tableInfo)).append(" (");
        StringBuilder values = new StringBuilder(1024);
        values.append("<foreach collection=\"list\" item=\"item\" separator=\",\">");
        values.append("\n(");
        for (TableColumnInfo columnInfo : tableInfo.getColumns()) {
            if (!columnInfo.isInsertable() || columnInfo.isAutoIncrement()) {
                continue;
            }
            builder.append(columnInfo.getActualColumn()).append(", ");

            values.append("#{item.").append(columnInfo.getProperty());
            if (columnInfo.getJdbcType() != null) {
                values.append(",jdbcType=BIGINT");
            }
            values.append("}, ");
        }
        values.deleteCharAt(values.length() - 2).append(")\n</foreach>");
        builder.deleteCharAt(builder.length() - 2).append(")\n");
        builder.append(" values \n").append(values);
        String sql = builder.toString();

        // 替换 sqlSource 对象
        SqlSource sqlSource = xmlLanguageDriver
                .createSqlSource(configuration, "<script>\n" + sql + "\n</script>", null);
        metaObject.setValue("sqlSource", sqlSource);
        return sql;
    }
}
