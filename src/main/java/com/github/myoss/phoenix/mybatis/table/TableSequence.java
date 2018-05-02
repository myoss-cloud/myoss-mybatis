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

import lombok.Data;

import org.apache.ibatis.executor.keygen.KeyGenerator;
import org.apache.ibatis.mapping.StatementType;

import com.github.myoss.phoenix.mybatis.executor.keygen.SequenceKeyGenerator;
import com.github.myoss.phoenix.mybatis.table.annotation.GenerationType;
import com.github.myoss.phoenix.mybatis.table.annotation.SequenceGenerator;
import com.github.myoss.phoenix.mybatis.table.annotation.SequenceGenerator.Order;

/**
 * 数据库表"序列生成器"属性配置
 *
 * @author Jerry.Chen 2018年4月30日 上午12:23:18
 */
@Data
public class TableSequence {
    /**
     * 序列生成策略
     */
    private GenerationType            strategy;
    /**
     * 从 {@link KeyGenerator} 中获取到的返回值，要设置到哪几个的属性字段中
     */
    private String[]                  keyProperties;
    /**
     * 从 {@link KeyGenerator} 中获取到的返回值，和 {@link #keyProperties} 的映射关系。绝大多数情况下，如果
     * {@link KeyGenerator} 返回的的值是单一的列，则不需要设置 {@code keyColumns }
     */
    private String[]                  keyColumns;
    /**
     * 从 {@link KeyGenerator} 中获取到的返回值 Java 类型，和 {@link #keyColumns} 的映射关系
     */
    private Class<?>[]                resultType;

    /**
     * /** 使用自定义 select sql 语句生成字段的值
     *
     * @see SequenceGenerator#selectKey()
     */
    private String                    sql;
    /**
     * 自定义 select sql 语句类型
     *
     * @see SequenceGenerator#selectKey()
     */
    private StatementType             statementType;

    /**
     * 使用 Java {@link Sequence} 接口实现类来生成序列值，由 {@link SequenceKeyGenerator} 触发调用
     *
     * @see GenerationType#SEQUENCE_KEY
     * @see SequenceGenerator#sequenceKey()
     */
    private Class<? extends Sequence> sequenceClass;
    /**
     * (Optional) {@code sequenceClass} 对应的 Spring Bean instance name
     *
     * @see GenerationType#SEQUENCE_KEY
     * @see SequenceGenerator#sequenceKey()
     * @see TableMetaObject#getSequenceBean(String)
     */
    private String                    sequenceBeanName;
    /**
     * (Optional) The name of the database sequence object from which to obtain
     * primary key values.
     * <p>
     * Defaults to a provider-chosen value.
     *
     * @see GenerationType#SEQUENCE_KEY
     * @see SequenceGenerator#sequenceKey()
     */
    private String                    sequenceName;

    /**
     * {@link KeyGenerator} 在 INSERT 之前/之后执行
     *
     * @see SequenceGenerator#selectKey()
     * @see SequenceGenerator#sequenceKey()
     */
    private Order                     order;
}
