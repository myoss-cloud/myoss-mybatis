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

package com.github.myoss.phoenix.mybatis.mapper.template.insert;

import org.apache.ibatis.annotations.InsertProvider;

import com.github.myoss.phoenix.mybatis.mapper.annotation.RegisterMapper;
import com.github.myoss.phoenix.mybatis.mapper.template.insert.impl.InsertMapperTemplate;

/**
 * 创建新记录通用 Mapper 接口
 *
 * @author Jerry.Chen 2018年4月29日 下午4:39:55
 */
@RegisterMapper
public interface InsertMapper<T> {
    /**
     * 创建新的记录，字段的值为 null 也会插入（不会使用数据库字段的默认值）
     *
     * @param record 待保存的实体对象
     * @return SQL执行成功之后，影响的行数
     * @see InsertMapperTemplate#insert
     */
    @InsertProvider(type = InsertMapperTemplate.class, method = "dynamicSql")
    int insert(T record);
}
