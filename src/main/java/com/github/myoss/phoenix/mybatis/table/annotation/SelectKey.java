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

package com.github.myoss.phoenix.mybatis.table.annotation;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import org.apache.ibatis.executor.keygen.KeyGenerator;
import org.apache.ibatis.executor.keygen.SelectKeyGenerator;
import org.apache.ibatis.mapping.StatementType;

import com.github.myoss.phoenix.mybatis.table.annotation.SequenceGenerator.Order;

/**
 * 用于 {@link GenerationType#SELECT_KEY} 策略，使用 {@link SelectKeyGenerator} 来实现。
 * <p>
 * 实现 Mapper.xml 中如下的功能：
 *
 * <pre>
 * &lt;selectKey keyProperty=&quot;id&quot; keyColumn=&quot;id&quot; resultType=&quot;java.lang.Long&quot; order=&quot;BEFORE&quot;&gt;
 *   SELECT LAST_INSERT_ID()
 * &lt;/selectKey&gt;
 * </pre>
 *
 * @author Jerry.Chen
 * @since 2018年4月30日 下午10:44:30
 * @see SelectKeyGenerator
 */
@Target({ FIELD })
@Retention(RUNTIME)
@Documented
public @interface SelectKey {
    /**
     * 从 {@link KeyGenerator} 中获取到的返回值，要设置到哪几个的属性字段中
     * <ul>
     * <li>可以设置多个字段
     * <li>此字段仅用于 {@link KeyGenerator} 实现类中
     * <li>如果 {@link SequenceGenerator} 注解不是放在 Field 上面，则需要设置。
     * <li>如果 {@link SequenceGenerator} 注解放在 Field 上面则，取此字段的属性名。
     * </ul>
     *
     * @return 主键属性字段名
     */
    String[] keyProperty() default {};

    /**
     * 从 {@link KeyGenerator} 中获取到的返回值，和 {@link #keyProperty} 的映射关系。绝大多数情况下，如果
     * {@link KeyGenerator} 返回的的值是单一的列，则不需要设置 {@code keyColumn }
     * <ul>
     * <li>可以设置多个字段
     * <li>可选属性
     * <li>默认会从对应的 <code>keyProperty</code> 字段中获取"数据库字段名"
     * </ul>
     *
     * @return SELECT SQL中取哪几个字段名
     */
    String[] keyColumn() default {};

    /**
     * 从 {@link KeyGenerator} 中获取到的返回值 Java 类型，和 {@link #keyColumn} 的映射关系
     * <ul>
     * <li>可选属性
     * <li>默认会从对应的 <code>keyProperty</code> 字段中获取"Java类型"
     * </ul>
     *
     * @return 主键Java类型
     */
    Class<?> resultType() default Class.class;

    /**
     * 使用自定义 select sql 语句生成字段的值
     *
     * @return select sql
     */
    String sql();

    /**
     * 自定义 select sql 语句类型
     *
     * @return select sql statement type
     */
    StatementType statementType() default StatementType.PREPARED;

    /**
     * {@link SelectKeyGenerator} 在 INSERT 之前/之后执行
     *
     * @return 在 INSERT 之前/之后执行
     */
    Order order() default Order.BEFORE;
}
