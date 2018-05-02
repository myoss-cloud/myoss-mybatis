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

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import com.github.myoss.phoenix.core.utils.NameStyle;

/**
 * This annotation specifies the primary table for the annotated entity.
 * <p>
 * If no <code>Table</code> annotation is specified for an entity class, the
 * default values apply.
 *
 * <pre>
 *    Example:
 * 
 *    &#064;Table(name="CUST", schema="RECORDS")
 *    public class Customer { ... }
 * </pre>
 *
 * @author Jerry.Chen 2018年4月26日 上午11:55:45
 */
@Target(TYPE)
@Retention(RUNTIME)
@Documented
public @interface Table {
    /**
     * 数据库表名
     * <p>
     * (Optional) The name of the table.
     * <p>
     * Defaults to the entity name.
     */
    String name() default "";

    /**
     * 数据库中的catalog，如果设置了此属性，将在表名前面加上catalog指定的值
     * <p>
     * (Optional) The catalog of the table.
     * <p>
     * Defaults to the default catalog.
     */
    String catalog() default "";

    /**
     * 数据库中的schema
     * <p>
     * (Optional) The schema of the table.
     * <p>
     * Defaults to the default schema for user.
     */
    String schema() default "";

    /**
     * 数据库表名、数据库字段命名风格
     * <p>
     * table name or column name style
     * <p>
     * Defaults to snake_case.
     */
    NameStyle nameStyle() default NameStyle.SNAKE_CASE;
}
