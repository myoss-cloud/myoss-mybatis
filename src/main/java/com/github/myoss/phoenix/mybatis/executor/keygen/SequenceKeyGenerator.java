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

package com.github.myoss.phoenix.mybatis.executor.keygen;

import java.sql.Statement;

import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.binding.BindingException;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.executor.ExecutorException;
import org.apache.ibatis.executor.keygen.KeyGenerator;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.session.Configuration;

import com.github.myoss.phoenix.mybatis.table.Sequence;
import com.github.myoss.phoenix.mybatis.table.TableInfo;
import com.github.myoss.phoenix.mybatis.table.TableMetaObject;
import com.github.myoss.phoenix.mybatis.table.TableSequence;
import com.github.myoss.phoenix.mybatis.table.annotation.GenerationType;
import com.github.myoss.phoenix.mybatis.table.annotation.SequenceKey;

/**
 * 用于 {@link GenerationType#SEQUENCE_KEY} 策略，触发 {@link Sequence}
 * 接口实现类来生成序列值，并更新到实体对象中
 *
 * @author Jerry.Chen 2018年4月30日 下午5:12:53
 * @see SequenceKey
 * @see Sequence
 */
public class SequenceKeyGenerator implements KeyGenerator {
    private String[]  keyProperties;
    private String[]  keyColumns;
    private TableInfo tableInfo;
    private boolean   executeBefore;
    private Sequence  sequence;

    public SequenceKeyGenerator(TableInfo tableInfo, boolean executeBefore) {
        TableSequence tableSequence = tableInfo.getTableSequence();
        this.keyProperties = tableSequence.getKeyProperties();
        this.keyColumns = tableSequence.getKeyColumns();
        this.tableInfo = tableInfo;
        this.executeBefore = executeBefore;

        // 获取 Sequence 实例对象
        String sequenceBeanName = tableSequence.getSequenceBeanName();
        if (StringUtils.isNotBlank(sequenceBeanName)) {
            this.sequence = TableMetaObject.getSequenceBean(sequenceBeanName);
            if (this.sequence == null) {
                throw new BindingException("no instance of [" + sequenceBeanName
                        + "] in TableMetaObject, please check!");
            }
        } else {
            try {
                this.sequence = tableSequence.getSequenceClass().newInstance();
            } catch (InstantiationException | IllegalAccessException e) {
                throw new BindingException("new instance of [" + tableSequence.getSequenceClass() + "] failed", e);
            }
        }
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
                final Configuration configuration = ms.getConfiguration();
                final MetaObject metaParam = configuration.newMetaObject(parameter);

                Object value = sequence.nextValue(tableInfo, parameter);
                MetaObject metaResult = configuration.newMetaObject(value);
                if (keyProperties.length == 1) {
                    String keyProperty = keyProperties[0];
                    if (metaResult.hasGetter(keyProperty)) {
                        setValue(metaParam, keyProperty, metaResult.getValue(keyProperty));
                    } else {
                        // no getter for the property - maybe just a single value object
                        // so try that
                        setValue(metaParam, keyProperty, value);
                    }
                } else {
                    handleMultipleProperties(keyProperties, metaParam, metaResult);
                }
            }
        } catch (ExecutorException e) {
            throw e;
        } catch (Exception e) {
            throw new ExecutorException("Error selecting key or setting result to parameter object. Cause: " + e, e);
        }
    }

    private void handleMultipleProperties(String[] keyProperties, MetaObject metaParam, MetaObject metaResult) {
        if (keyColumns == null || keyColumns.length == 0) {
            // no key columns specified, just use the property names
            for (String keyProperty : keyProperties) {
                setValue(metaParam, keyProperty, metaResult.getValue(keyProperty));
            }
        } else {
            if (keyColumns.length != keyProperties.length) {
                throw new ExecutorException(
                        "If SelectKey has key columns, the number must match the number of key properties.");
            }
            for (int i = 0; i < keyProperties.length; i++) {
                setValue(metaParam, keyProperties[i], metaResult.getValue(keyColumns[i]));
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
}
