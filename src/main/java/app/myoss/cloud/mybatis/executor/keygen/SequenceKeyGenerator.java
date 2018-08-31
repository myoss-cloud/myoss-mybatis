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

package app.myoss.cloud.mybatis.executor.keygen;

import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.binding.BindingException;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.executor.ExecutorException;
import org.apache.ibatis.executor.keygen.KeyGenerator;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.session.Configuration;
import org.springframework.util.CollectionUtils;

import app.myoss.cloud.mybatis.table.Sequence;
import app.myoss.cloud.mybatis.table.TableInfo;
import app.myoss.cloud.mybatis.table.TableMetaObject;
import app.myoss.cloud.mybatis.table.TableSequence;
import app.myoss.cloud.mybatis.table.annotation.GenerationType;
import app.myoss.cloud.mybatis.table.annotation.SequenceKey;

/**
 * 用于 {@link GenerationType#SEQUENCE_KEY} 策略，触发 {@link Sequence}
 * 接口实现类来生成序列值，并更新到实体对象中
 *
 * @author Jerry.Chen
 * @since 2018年4月30日 下午5:12:53
 * @see SequenceKey
 * @see Sequence
 */
public class SequenceKeyGenerator implements KeyGenerator {
    /**
     * sequence key 默认的后缀名
     */
    public static final String SEQUENCE_KEY_SUFFIX = "!sequenceKey";
    private String[]           keyProperties;
    private String[]           keyColumns;
    private boolean            executeBefore;
    private Sequence           sequence;

    /**
     * 初始化序列生成器
     *
     * @param tableInfo 数据库表结构信息
     * @param sqlId mappedStatement id
     * @param executeBefore 在 INSERT 之前/之后执行
     */
    public SequenceKeyGenerator(TableInfo tableInfo, String sqlId, boolean executeBefore) {
        TableSequence tableSequence = tableInfo.getTableSequence();
        this.keyProperties = tableSequence.getKeyProperties();
        this.keyColumns = tableSequence.getKeyColumns();
        this.executeBefore = executeBefore;

        // 获取 Sequence 实例对象
        String sequenceBeanName = tableSequence.getSequenceBeanName();
        if (StringUtils.isNotBlank(sequenceBeanName)) {
            this.sequence = TableMetaObject.getSequenceBean(sequenceBeanName);
            if (this.sequence == null) {
                throw new BindingException(
                        "no instance of [" + sequenceBeanName + "] in TableMetaObject, please check!");
            }
        } else {
            try {
                this.sequence = tableSequence.getSequenceClass().newInstance();
                String name = StringUtils.defaultString(tableSequence.getSequenceName(), SEQUENCE_KEY_SUFFIX);
                TableMetaObject.addSequenceBean(sqlId + "." + name, this.sequence);
            } catch (InstantiationException | IllegalAccessException e) {
                throw new BindingException("new instance of [" + tableSequence.getSequenceClass() + "] failed", e);
            }
        }
        this.sequence.setTableInfo(tableInfo);
    }

    @Override
    public void processBefore(Executor executor, MappedStatement ms, Statement stmt, Object parameter) {
        if (executeBefore) {
            processGeneratedKeys(ms, parameter);
        }
    }

    @Override
    public void processAfter(Executor executor, MappedStatement ms, Statement stmt, Object parameter) {
        if (!executeBefore) {
            processGeneratedKeys(ms, parameter);
        }
    }

    private void processGeneratedKeys(MappedStatement ms, Object parameter) {
        try {
            if (parameter != null && keyProperties != null && keyProperties.length > 0) {
                if (keyColumns != null && keyColumns.length != keyProperties.length) {
                    throw new ExecutorException(
                            "If SelectKey has key columns, the number must match the number of key properties.");
                }
                final Configuration configuration = ms.getConfiguration();
                final MetaObject metaParam = configuration.newMetaObject(parameter);
                List<String> keys = new ArrayList<>(keyProperties.length);
                List<String> columns = new ArrayList<>(keyProperties.length);
                for (int i = 0; i < keyProperties.length; i++) {
                    String keyProperty = keyProperties[i];
                    if (!(metaParam.hasGetter(keyProperty) && checkValueIsNotNull(metaParam, keyProperty))) {
                        keys.add(keyProperty);
                        if (keyColumns != null) {
                            columns.add(keyColumns[i]);
                        }
                    }
                }
                if (keys.size() == 0) {
                    // 主键字段已经有值，不生成
                    return;
                }

                Object value = sequence.nextValue(parameter);
                MetaObject metaResult = configuration.newMetaObject(value);
                if (keys.size() == 1) {
                    String keyProperty = keys.get(0);
                    if (metaResult.hasGetter(keyProperty)) {
                        setValue(metaParam, keyProperty, metaResult.getValue(keyProperty));
                    } else {
                        // no getter for the property - maybe just a single value object
                        // so try that
                        setValue(metaParam, keyProperty, value);
                    }
                } else {
                    handleMultipleProperties(keys, columns, metaParam, metaResult);
                }
            }
        } catch (ExecutorException e) {
            throw e;
        } catch (Exception e) {
            throw new ExecutorException("Error selecting key or setting result to parameter object. Cause: " + e, e);
        }
    }

    private void handleMultipleProperties(List<String> keyProperties, List<String> columns, MetaObject metaParam,
                                          MetaObject metaResult) {
        if (CollectionUtils.isEmpty(columns)) {
            // no key columns specified, just use the property names
            for (String keyProperty : keyProperties) {
                setValue(metaParam, keyProperty, metaResult.getValue(keyProperty));
            }
        } else {
            if (keyProperties.size() != columns.size()) {
                throw new ExecutorException(
                        "If SelectKey has key columns, the number must match the number of key properties.");
            }
            for (int i = 0; i < keyProperties.size(); i++) {
                setValue(metaParam, keyProperties.get(i), metaResult.getValue(columns.get(i)));
            }
        }
    }

    private void setValue(MetaObject metaParam, String property, Object value) {
        if (metaParam.hasSetter(property)) {
            metaParam.setValue(property, value);
        } else {
            throw new ExecutorException("No setter found for the keyProperty '" + property + "' in "
                    + metaParam.getOriginalObject().getClass().getName() + ".");
        }
    }

    private boolean checkValueIsNotNull(MetaObject metaParam, String keyProperty) {
        Object value = metaParam.getValue(keyProperty);
        if (value instanceof CharSequence && StringUtils.isBlank((CharSequence) value)) {
            return true;
        }
        return value != null;
    }
}
