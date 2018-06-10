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

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.binding.BindingException;
import org.apache.ibatis.mapping.ResultFlag;
import org.apache.ibatis.mapping.ResultMap;
import org.apache.ibatis.mapping.ResultMapping;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.type.TypeHandler;
import org.springframework.util.ClassUtils;

import com.github.myoss.phoenix.core.exception.BizRuntimeException;
import com.github.myoss.phoenix.core.utils.NameStyle;
import com.github.myoss.phoenix.mybatis.table.annotation.Column;
import com.github.myoss.phoenix.mybatis.table.annotation.FillRule;
import com.github.myoss.phoenix.mybatis.table.annotation.GenerationType;
import com.github.myoss.phoenix.mybatis.table.annotation.SelectKey;
import com.github.myoss.phoenix.mybatis.table.annotation.SequenceGenerator;
import com.github.myoss.phoenix.mybatis.table.annotation.SequenceKey;
import com.github.myoss.phoenix.mybatis.table.annotation.Table;
import com.github.myoss.phoenix.mybatis.type.UnsupportedTypeHandler;

/**
 * 数据库表结构信息工具类
 *
 * @author Jerry.Chen
 * @since 2018年4月26日 上午10:46:25
 */
public class TableMetaObject {
    /**
     * 实体类 => 表对象
     */
    private static final Map<Class<?>, TableInfo>    ENTITY_TABLE_MAP     = new ConcurrentHashMap<>();
    private static final Map<String, Sequence>       SEQUENCE_BEAN_MAP    = new ConcurrentHashMap<>();
    private static final Class<? extends Annotation> PERSISTENCE_ID_CLASS = resolveAnnotationClassName("javax.persistence.Id");

    @SuppressWarnings("unchecked")
    public static Class<? extends Annotation> resolveAnnotationClassName(String className) {
        ClassLoader classLoader = TableMetaObject.class.getClassLoader();
        if (ClassUtils.isPresent(className, classLoader)) {
            return (Class<? extends Annotation>) ClassUtils.resolveClassName(className, classLoader);
        }
        return null;
    }

    /**
     * 将"序列生成器实例对象"添加到缓存中
     *
     * @param sequenceBeanName 序列的 Spring Bean 实例名称
     * @param sequence 序列生成器实例对象
     */
    public static void addSequenceBean(String sequenceBeanName, Sequence sequence) {
        if (SEQUENCE_BEAN_MAP.containsKey(sequenceBeanName)) {
            throw new IllegalArgumentException("already contains value for " + sequenceBeanName + ", sequence: "
                    + sequence);
        }
        SEQUENCE_BEAN_MAP.putIfAbsent(sequenceBeanName, sequence);
    }

    /**
     * 获取缓存中的"序列生成器实例对象"
     *
     * @param sequenceBeanName 序列的 Spring Bean 实例名称
     * @return 序列生成器实例对象
     */
    public static Sequence getSequenceBean(String sequenceBeanName) {
        return SEQUENCE_BEAN_MAP.get(sequenceBeanName);
    }

    /**
     * 获取缓存中的全部"序列生成器实例对象"
     *
     * @return 全部"序列生成器实例对象"
     */
    public static Map<String, Sequence> getSequenceBeanMap() {
        return SEQUENCE_BEAN_MAP;
    }

    /**
     * 获取缓存中的全部"数据库表结构信息"
     *
     * @return 全部"数据库表结构信息"
     */
    public static Map<Class<?>, TableInfo> getTableInfoMap() {
        return ENTITY_TABLE_MAP;
    }

    /**
     * 获取缓存中的"数据库表结构信息"
     *
     * @param entityClass 实体类class
     * @return 数据库表结构信息
     */
    public static TableInfo getTableInfo(Class<?> entityClass) {
        return ENTITY_TABLE_MAP.get(entityClass);
    }

    /**
     * 根据"mapper interface class" 中的泛型获取"实体类class"
     *
     * @param mapperInterfaceClass mapper interface class
     * @return 实体类class
     */
    public static Class<?> getEntityClassByMapperInterface(Class<?> mapperInterfaceClass) {
        Type[] types = mapperInterfaceClass.getGenericInterfaces();
        for (Type type : types) {
            if (type instanceof ParameterizedType) {
                Type[] actualTypeArguments = ((ParameterizedType) type).getActualTypeArguments();
                if (actualTypeArguments.length == 0) {
                    continue;
                }
                return (Class<?>) actualTypeArguments[0];
            }
        }
        return null;
    }

    /**
     * 获取缓存中的"数据库表结构信息"，或者获取初始化"数据库表结构信息"
     *
     * @param mapperInterface mapper interface class
     * @param config MyBatis Table 全局配置；实体类映射数据库表的全局配置
     * @param configuration MyBatis 全局配置
     * @return 数据库表结构信息
     */
    public static TableInfo getTableInfoByMapperInterface(Class<?> mapperInterface, TableConfig config,
                                                          Configuration configuration) {
        Class<?> entityClass = getEntityClassByMapperInterface(mapperInterface);
        TableInfo tableInfo = getTableInfo(entityClass, config, configuration);
        tableInfo.setMapperInterfaceClass(mapperInterface);
        return tableInfo;
    }

    /**
     * 获取缓存中的"数据库表结构信息"，或者获取初始化"数据库表结构信息"
     *
     * @param entityClass 实体类class
     * @param config MyBatis Table 全局配置；实体类映射数据库表的全局配置
     * @param configuration MyBatis 全局配置
     * @return 数据库表结构信息
     */
    public static TableInfo getTableInfo(Class<?> entityClass, TableConfig config, Configuration configuration) {
        TableInfo tableInfo = ENTITY_TABLE_MAP.get(entityClass);
        if (tableInfo != null) {
            return tableInfo;
        }
        NameStyle tableNameStyle = config.getTableNameStyle();
        tableInfo = new TableInfo();
        tableInfo.setCatalog(config.getCatalog());
        tableInfo.setSchema(config.getSchema());
        if (entityClass.isAnnotationPresent(Table.class)) {
            Table table = entityClass.getAnnotation(Table.class);
            String catalog = StringUtils.defaultIfBlank(table.catalog(), config.getCatalog());
            String schema = StringUtils.defaultIfBlank(table.schema(), config.getSchema());
            tableInfo.setCatalog(catalog);
            tableInfo.setSchema(schema);
            if (StringUtils.isNotBlank(table.name())) {
                String name = config.getTableNamePrefix() + table.name() + config.getTableNameSuffix();
                tableInfo.setTableName(name);
            }
            if (StringUtils.isNotBlank(table.escapedName())) {
                String name = config.getTableNamePrefix() + table.escapedName() + config.getTableNameSuffix();
                tableInfo.setEscapedTableName(name);
            }
            tableNameStyle = table.nameStyle();
        }
        if (StringUtils.isBlank(tableInfo.getTableName())) {
            String name = tableNameStyle.transform(entityClass.getSimpleName());
            String tableName = config.getTableNamePrefix() + name + config.getTableNameSuffix();
            tableInfo.setTableName(tableName);
        }
        tableInfo.setEntityClass(entityClass);
        initTableSequence(entityClass.getAnnotation(SequenceGenerator.class), tableInfo, null);
        TableSequence tableSequence = tableInfo.getTableSequence();
        String[] keyProperties = tableSequence != null ? tableSequence.getKeyProperties() : null;
        String[] keyColumns = new String[keyProperties == null ? 0 : keyProperties.length];
        Class<?>[] resultTypes = new Class[keyProperties == null ? 0 : keyProperties.length];

        // 处理字段信息
        NameStyle columnNameStyle = config.getColumnNameStyle();
        Set<TableColumnInfo> columns = new LinkedHashSet<>();
        Set<TableColumnInfo> pkColumns = new LinkedHashSet<>();
        Set<TableColumnInfo> logicDeleteColumns = new LinkedHashSet<>();
        Map<String, PropertyDescriptor> propertyDescriptorMap = getPropertyDescriptorMap(entityClass);
        List<Field> fields = getFieldList(entityClass);
        for (Field field : fields) {
            String name = field.getName();
            TableColumnInfo columnInfo = new TableColumnInfo();
            columns.add(columnInfo);
            columnInfo.setTableInfo(tableInfo);
            columnInfo.setProperty(name);
            PropertyDescriptor propertyDescriptor = propertyDescriptorMap.get(name);
            columnInfo.setJavaType(propertyDescriptor.getPropertyType());
            columnInfo.setPropertyDescriptor(propertyDescriptor);
            Column column = field.getAnnotation(Column.class);
            if (column != null) {
                if (StringUtils.isNotBlank(column.name())) {
                    columnInfo.setColumn(column.name());
                }
                if (StringUtils.isNotBlank(column.escapedName())) {
                    columnInfo.setEscapedColumn(column.escapedName());
                }
                if (column.primaryKey()) {
                    columnInfo.setPrimaryKey(true);
                }
                if (column.typeHandler() != UnsupportedTypeHandler.class) {
                    columnInfo.setTypeHandler(column.typeHandler());
                }
                columnInfo.setInsertable(column.insertable());
                columnInfo.setUpdatable(column.updatable());
                columnInfo.setSelectable(column.selectable());
                Map<FillRule, String> fillRules = Stream.of(column.fillRule()).filter(s -> !FillRule.NONE.equals(s))
                        .collect(Collectors.toMap(Function.identity(), FillRule::getValue));
                columnInfo.setFillRules(fillRules);
            }
            if (StringUtils.isBlank(columnInfo.getColumn())) {
                columnInfo.setColumn(columnNameStyle.transform(name));
            }
            initLogicDelete(config, tableInfo, columnInfo, column, logicDeleteColumns);

            if (!columnInfo.isPrimaryKey() && PERSISTENCE_ID_CLASS != null
                    && field.isAnnotationPresent(PERSISTENCE_ID_CLASS)) {
                columnInfo.setPrimaryKey(true);
            }
            if (columnInfo.isPrimaryKey()) {
                pkColumns.add(columnInfo);
            }

            // 收集序列字段信息
            int indexOf = ArrayUtils.indexOf(keyProperties, columnInfo.getProperty());
            if (indexOf > -1) {
                // 收集数据库字段名
                keyColumns[indexOf] = columnInfo.getColumn();
                resultTypes[indexOf] = columnInfo.getJavaType();
            }
            initTableSequence(field.getAnnotation(SequenceGenerator.class), tableInfo, columnInfo);
        }
        tableInfo.setColumns(columns);
        tableInfo.setPrimaryKeyColumns(pkColumns);
        tableInfo.setLogicDeleteColumns(logicDeleteColumns);
        if (keyProperties != null && keyColumns.length > 0
                && ArrayUtils.isEmpty(tableInfo.getTableSequence().getKeyColumns())) {
            // 如果 @SequenceGenerator 注解放在 class 上，并且没有设置 keyColumns 属性，则取相应 keyProperty 中的字段名
            tableInfo.getTableSequence().setKeyColumns(keyColumns);
        }
        if (keyProperties != null && resultTypes.length > 0
                && ArrayUtils.isEmpty(tableInfo.getTableSequence().getResultType())) {
            // 如果 @SequenceGenerator 注解放在 class 上，并且没有设置 resultType 属性，则取相应 keyProperty 中的字段类型
            tableInfo.getTableSequence().setResultType(resultTypes);
        }

        // 生成实体的 BaseResultMap 对象
        tableInfo.setBaseResultMap(builderBaseResultMap(tableInfo, configuration));
        // 生成 select 查询所有列sql语句
        tableInfo.setSelectAllColumnsSql(builderSelectAllColumns(tableInfo));
        // 生成 where 主键条件sql语句
        tableInfo.setWherePrimaryKeySql(builderWherePrimaryKeySql(tableInfo, false));
        tableInfo.setWherePrimaryKeyIncludeLogicDeleteSql(builderWherePrimaryKeySql(tableInfo, true));
        // 生成 where 所有条件sql语句
        tableInfo.setWhereConditionSql(builderWhereConditionSql(tableInfo, false));
        tableInfo.setWhereConditionIncludeLogicDeleteSql(builderWhereConditionSql(tableInfo, true));
        // 生成 where 所有条件sql语句，带有参数前缀
        tableInfo
                .setWhereConditionWithParameterSql(builderWhereConditionWithParameterSql(tableInfo, false, "condition"));
        tableInfo.setWhereConditionWithParameterIncludeLogicDeleteSql(builderWhereConditionWithParameterSql(tableInfo,
                true, "condition"));
        ENTITY_TABLE_MAP.put(entityClass, tableInfo);
        return tableInfo;
    }

    /**
     * 加载数据库表"序列生成器"属性配置
     */
    private static void initTableSequence(SequenceGenerator sequenceGenerator, TableInfo tableInfo,
                                          TableColumnInfo columnInfo) {
        if (sequenceGenerator == null) {
            return;
        }
        if (tableInfo.getTableSequence() != null) {
            // 自增序列只能有一个
            throw new BindingException("more than one sequence field: [" + tableInfo.getTableSequence() + ", "
                    + columnInfo + "]");
        }

        GenerationType strategy = sequenceGenerator.strategy();
        TableSequence tableSequence = new TableSequence();
        if (strategy == GenerationType.USE_GENERATED_KEYS) {
            if (columnInfo != null) {
                columnInfo.setAutoIncrement(true);
            }
        } else if (strategy == GenerationType.SELECT_KEY) {
            SelectKey selectKey = sequenceGenerator.selectKey();
            tableSequence.setKeyProperties(selectKey.keyProperty());
            tableSequence.setKeyColumns(selectKey.keyColumn());
            if (selectKey.resultType() != Class.class) {
                tableSequence.setResultType(new Class[] { selectKey.resultType() });
            }
            tableSequence.setSql(selectKey.sql());
            tableSequence.setStatementType(selectKey.statementType());
            tableSequence.setOrder(selectKey.order());
            if (columnInfo != null) {
                if (ArrayUtils.isEmpty(tableSequence.getKeyProperties())) {
                    tableSequence.setKeyProperties(new String[] { columnInfo.getProperty() });
                }
                if (ArrayUtils.isEmpty(tableSequence.getKeyColumns())) {
                    tableSequence.setKeyColumns(new String[] { columnInfo.getColumn() });
                }
                if (ArrayUtils.isEmpty(tableSequence.getResultType())) {
                    tableSequence.setResultType(new Class[] { columnInfo.getJavaType() });
                }
            }
        } else if (strategy == GenerationType.SEQUENCE_KEY) {
            SequenceKey sequenceKey = sequenceGenerator.sequenceKey();
            tableSequence.setKeyProperties(sequenceKey.keyProperty());
            tableSequence.setKeyColumns(sequenceKey.keyColumn());
            tableSequence.setSequenceClass(sequenceKey.sequenceClass());
            tableSequence.setSequenceBeanName(sequenceKey.sequenceBeanName());
            tableSequence.setSequenceName(sequenceKey.sequenceName());
            tableSequence.setOrder(sequenceKey.order());
            if (columnInfo != null) {
                if (ArrayUtils.isEmpty(tableSequence.getKeyProperties())) {
                    tableSequence.setKeyProperties(new String[] { columnInfo.getProperty() });
                }
                if (ArrayUtils.isEmpty(tableSequence.getKeyColumns())) {
                    tableSequence.setKeyColumns(new String[] { columnInfo.getColumn() });
                }
            }
        } else {
            throw new UnsupportedOperationException("keyGenerator strategy " + strategy.getType() + " unsupported");
        }

        if (strategy != GenerationType.USE_GENERATED_KEYS && ArrayUtils.isEmpty(tableSequence.getKeyProperties())) {
            throw new BindingException("keyProperty value is blank: " + sequenceGenerator);
        }
        tableSequence.setStrategy(strategy);
        tableInfo.setTableSequence(tableSequence);
    }

    /**
     * 初始化"逻辑删除"字段
     */
    private static void initLogicDelete(TableConfig config, TableInfo tableInfo, TableColumnInfo columnInfo,
                                        Column column, Set<TableColumnInfo> logicDeleteColumns) {
        boolean isLogicDelete = false;
        String deleteValue = null;
        String unDeleteValue = null;
        if (column != null && column.logicDelete()) {
            isLogicDelete = true;
            unDeleteValue = StringUtils.defaultIfBlank(column.logicUnDeleteValue(), config.getLogicUnDeleteValue());
            deleteValue = StringUtils.defaultIfBlank(column.logicDeleteValue(), config.getLogicDeleteValue());
        } else if (config.isLogicDelete() && columnInfo.getColumn().equals(config.getLogicDeleteColumnName())) {
            isLogicDelete = true;
            deleteValue = config.getLogicDeleteValue();
            unDeleteValue = config.getLogicUnDeleteValue();
        }
        if (isLogicDelete) {
            if (StringUtils.isAnyBlank(deleteValue, unDeleteValue)) {
                throw new BindingException("logicDeleteValue or logicUnDeleteValue is blank, [logicDeleteValue="
                        + deleteValue + ", logicUnDeleteValue=" + unDeleteValue + "]");
            }
            columnInfo.setLogicDelete(true);
            columnInfo.setLogicDeleteValue(deleteValue);
            columnInfo.setLogicUnDeleteValue(unDeleteValue);
            tableInfo.setLogicDelete(true);
            logicDeleteColumns.add(columnInfo);
        }
    }

    /**
     * 获取 {@code clazz } Class 中的所有字段，排除 static, transient
     * 字段，包含父类中的字段（重写的字段只会保留一个）
     *
     * @param clazz 反射类
     * @return 所有字段信息
     */
    public static List<Field> getFieldList(Class<?> clazz) {
        Class<?> currentClass = Objects.requireNonNull(clazz);
        List<Field> allFields = new ArrayList<>();
        Map<String, Boolean> allFiledName = new HashMap<>();
        boolean isSupper = false;
        while (currentClass != null) {
            Field[] declaredFields = currentClass.getDeclaredFields();
            for (int i = 0; i < declaredFields.length; i++) {
                Field field = declaredFields[i];
                String name = field.getName();
                if (Modifier.isStatic(field.getModifiers()) || Modifier.isTransient(field.getModifiers())) {
                    // 过滤字段: static, transient
                    continue;
                }
                if (isSupper && allFiledName.containsKey(name)) {
                    // 过滤子类已经重写的字段
                    continue;
                }
                allFiledName.put(name, true);
                if (isSupper) {
                    allFields.add(i, field);
                } else {
                    allFields.add(field);
                }
            }
            isSupper = true;
            currentClass = currentClass.getSuperclass();
        }
        return allFields;
    }

    /**
     * 获取 {@code clazz } Class 中所有的 getter/setter 方法
     *
     * @param clazz 反射类
     * @return clazz 所有的 getter/setter 方法
     */
    public static Map<String, PropertyDescriptor> getPropertyDescriptorMap(Class<?> clazz) {
        BeanInfo beanInfo;
        try {
            beanInfo = Introspector.getBeanInfo(clazz);
        } catch (IntrospectionException e) {
            throw new BizRuntimeException(e);
        }
        PropertyDescriptor[] descriptors = beanInfo.getPropertyDescriptors();
        return Stream.of(descriptors).filter(s -> !"class".equals(s.getName()))
                .collect(Collectors.toMap(PropertyDescriptor::getName, Function.identity()));
    }

    /**
     * 获取数据库完整的表名
     *
     * @param tableInfo 数据库表结构信息
     * @return catalog.schema.tableName
     */
    public static String getTableName(TableInfo tableInfo) {
        return Stream.of(tableInfo.getCatalog(), tableInfo.getSchema(), tableInfo.getActualTableName())
                .filter(StringUtils::isNotBlank).collect(Collectors.joining("."));
    }

    /**
     * 生成实体的 BaseResultMap 对象，表映射结果集。类似 Mapper XML 中效果：
     *
     * <pre>
     * &lt;resultMap id=&quot;BaseResultMap&quot; type=&quot;entityClass package&quot;&gt;
     *   &lt;id column=&quot;id&quot; jdbcType=&quot;BIGINT&quot; property=&quot;id&quot; /&gt;
     * &lt;/resultMap&gt;
     * </pre>
     *
     * @param tableInfo 数据库表结构信息
     * @param configuration Mybatis configuration
     * @return BaseResultMap 对象
     */
    public static ResultMap builderBaseResultMap(TableInfo tableInfo, Configuration configuration) {
        List<ResultMapping> resultMappings = new ArrayList<>();
        for (TableColumnInfo item : tableInfo.getColumns()) {
            ResultMapping.Builder builder = new ResultMapping.Builder(configuration, item.getProperty(),
                    item.getColumn(), item.getJavaType());
            if (item.getJdbcType() != null) {
                builder.jdbcType(item.getJdbcType());
            }
            if (item.getTypeHandler() != null) {
                try {
                    TypeHandler<?> typeHandler = item.getTypeHandler().newInstance();
                    builder.typeHandler(typeHandler);
                } catch (InstantiationException | IllegalAccessException e) {
                    throw new BizRuntimeException(e);
                }
            }
            List<ResultFlag> flags = new ArrayList<>();
            if (item.isPrimaryKey()) {
                flags.add(ResultFlag.ID);
            }
            builder.flags(flags);
            resultMappings.add(builder.build());
        }
        ResultMap.Builder builder = new ResultMap.Builder(configuration, "BaseResultMap", tableInfo.getEntityClass(),
                resultMappings, true);
        return builder.build();
    }

    /**
     * 生成 select 查询所有列sql语句，示例如下：
     *
     * <pre>
     * id, name, code
     * </pre>
     *
     * @param tableInfo 数据库表结构信息
     * @return sql语句
     */
    public static String builderSelectAllColumns(TableInfo tableInfo) {
        return tableInfo.getColumns().stream().filter(TableColumnInfo::isSelectable)
                .map(TableColumnInfo::getActualColumn).collect(Collectors.joining(", "));
    }

    /**
     * 生成 where 所有条件sql语句（{@code includeLogicDelete = false}
     * ，如果表是逻辑删除，会添加逻辑删除的字段），示例如下：
     *
     * <pre>
     * &lt;where&gt;
     *  &lt;if test=&quot;id != null&quot;&gt;
     *    and id = #{id}
     *  &lt;/if&gt;
     *  AND is_deleted = 'N'
     * &lt;/where&gt;
     * </pre>
     *
     * @param tableInfo 数据库表结构信息
     * @param includeLogicDelete 是否不过滤掉已经被标记为逻辑删除（{@link Column#logicDelete}）的数据
     * @return sql语句
     */
    public static StringBuilder builderWhereConditionSql(TableInfo tableInfo, boolean includeLogicDelete) {
        StringBuilder sql = new StringBuilder(1024);
        sql.append("<where>\n");
        for (TableColumnInfo item : tableInfo.getColumns()) {
            if (!includeLogicDelete && item.isLogicDelete()) {
                continue;
            }
            sql.append("  <if test=\"").append(item.getProperty()).append(" != null\">\n");

            sql.append("    and ").append(item.getActualColumn()).append(" = #{").append(item.getProperty());
            if (item.getJdbcType() != null) {
                sql.append(",jdbcType=").append(item.getJdbcType().name());
            }
            sql.append("}\n");

            sql.append("  </if>\n");
        }
        if (!includeLogicDelete && tableInfo.isLogicDelete()) {
            for (TableColumnInfo item : tableInfo.getLogicDeleteColumns()) {
                sql.append("  and ").append(item.getActualColumn()).append(" = ");
                if (CharSequence.class.isAssignableFrom(item.getJavaType())) {
                    sql.append("'").append(item.getLogicUnDeleteValue()).append("'");
                } else {
                    sql.append(item.getLogicUnDeleteValue());
                }
                sql.append("\n");
            }
        }
        sql.append("</where>");
        return sql;
    }

    /**
     * 生成 where 所有条件sql语句（{@code includeLogicDelete = false}
     * ，如果表是逻辑删除，会添加逻辑删除的字段），带有参数前缀，示例如下：
     *
     * <pre>
     * &lt;where&gt;
     *  &lt;if test=&quot;conditionPrefix.id != null&quot;&gt;
     *    and id = #{conditionPrefix.id}
     *  &lt;/if&gt;
     *  AND is_deleted = 'N'
     * &lt;/where&gt;
     * </pre>
     *
     * @param tableInfo 数据库表结构信息
     * @param includeLogicDelete 是否不过滤掉已经被标记为逻辑删除（{@link Column#logicDelete}）的数据
     * @param conditionPrefix 参数前缀
     * @return sql语句
     */
    public static StringBuilder builderWhereConditionWithParameterSql(TableInfo tableInfo, boolean includeLogicDelete,
                                                                      String conditionPrefix) {
        String prefix = conditionPrefix + ".";
        StringBuilder sql = new StringBuilder(2048);
        sql.append("<where>\n");
        sql.append("  <if test=\"").append(conditionPrefix).append(" != null\">\n");
        for (TableColumnInfo item : tableInfo.getColumns()) {
            if (!includeLogicDelete && item.isLogicDelete()) {
                continue;
            }
            sql.append("    <if test=\"").append(prefix).append(item.getProperty()).append(" != null\">\n");

            sql.append("      and ").append(item.getActualColumn()).append(" = #{").append(prefix)
                    .append(item.getProperty());
            if (item.getJdbcType() != null) {
                sql.append(",jdbcType=").append(item.getJdbcType().name());
            }
            sql.append("}\n");

            sql.append("    </if>\n");
        }
        sql.append("  </if>\n");
        if (!includeLogicDelete && tableInfo.isLogicDelete()) {
            for (TableColumnInfo item : tableInfo.getLogicDeleteColumns()) {
                sql.append("  and ").append(item.getActualColumn()).append(" = ");
                if (CharSequence.class.isAssignableFrom(item.getJavaType())) {
                    sql.append("'").append(item.getLogicUnDeleteValue()).append("'");
                } else {
                    sql.append(item.getLogicUnDeleteValue());
                }
                sql.append("\n");
            }
        }
        sql.append("</where>");
        return sql;
    }

    /**
     * 生成 where 主键条件sql语句（{@code includeLogicDelete = false}
     * ，如果表是逻辑删除，会添加逻辑删除的字段），示例如下：
     *
     * <pre>
     * &lt;where&gt;
     *  AND id = #{id}
     *  AND is_deleted = 'N'
     * &lt;/where&gt;
     * </pre>
     *
     * @param tableInfo 数据库表结构信息
     * @param includeLogicDelete 是否不过滤掉已经被标记为逻辑删除（{@link Column#logicDelete}）的数据
     * @return sql语句
     */
    public static StringBuilder builderWherePrimaryKeySql(TableInfo tableInfo, boolean includeLogicDelete) {
        StringBuilder sql = new StringBuilder(128);
        sql.append("<where>\n");
        for (TableColumnInfo columnInfo : tableInfo.getPrimaryKeyColumns()) {
            sql.append("  AND ").append(columnInfo.getActualColumn()).append(" = ");
            sql.append("#{").append(columnInfo.getProperty());
            if (columnInfo.getJdbcType() != null) {
                sql.append(",jdbcType=BIGINT");
            }
            sql.append("}\n");
        }
        if (!includeLogicDelete && tableInfo.isLogicDelete()) {
            for (TableColumnInfo item : tableInfo.getLogicDeleteColumns()) {
                sql.append("  AND ").append(item.getActualColumn()).append(" = ");
                if (CharSequence.class.isAssignableFrom(item.getJavaType())) {
                    sql.append("'").append(item.getLogicUnDeleteValue()).append("'");
                } else {
                    sql.append(item.getLogicUnDeleteValue());
                }
                sql.append("\n");
            }
        }
        sql.append("</where>");
        return sql;
    }

}
