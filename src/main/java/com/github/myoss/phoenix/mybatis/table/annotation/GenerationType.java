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

import org.apache.ibatis.executor.keygen.SelectKeyGenerator;

import com.github.myoss.phoenix.mybatis.executor.keygen.SequenceKeyGenerator;
import com.github.myoss.phoenix.mybatis.table.Sequence;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 生成主键id策略类型
 *
 * @author Jerry.Chen
 * @since 2018年4月30日 下午10:49:06
 */
@Getter
@AllArgsConstructor
public enum GenerationType {
    /**
     * 如果插入的表主键是自增列，则可以使用 JDBC 自动生成主键，并可将生成的主键值返回。
     *
     * <pre>
     * &lt;insert id=&quot;insert&quot; useGeneratedKeys=&quot;true&quot; parameterType=&quot;...&quot;&gt;
     *   insert into User(id, name, age) values (#{id}, #{name}, #{age})
     * &lt;/insert&gt;
     * </pre>
     */
    USE_GENERATED_KEYS("useGeneratedKeys"),

    /**
     * 配置 {@link SelectKey} 属性，使用 {@link SelectKeyGenerator} 来触发调用
     */
    SELECT_KEY("selectKey"),

    /**
     * 配置 {@link SequenceKey} 属性，使用 Java {@link Sequence} 接口实现类来生成序列值，由
     * {@link SequenceKeyGenerator} 触发调用
     */
    SEQUENCE_KEY("sequenceKey");

    private String type;
}
