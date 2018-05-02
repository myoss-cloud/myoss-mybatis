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

package com.github.myoss.phoenix.mybatis.mapper.template.select;

import org.apache.ibatis.annotations.SelectProvider;

import com.github.myoss.phoenix.mybatis.mapper.annotation.RegisterMapper;
import com.github.myoss.phoenix.mybatis.mapper.template.select.impl.SelectMapperTemplate;

/**
 * 查询记录通用 Mapper 接口
 *
 * @author Jerry.Chen 2018年4月27日 下午5:23:03
 */
@RegisterMapper
public interface SelectOneMapper<T> {
    /**
     * 根据条件查询匹配的实体对象，只能有一条查询结果记录，有多条查询结果则会抛出异常
     *
     * @param condition 匹配的条件
     * @return 匹配的实体对象
     * @see SelectMapperTemplate#selectOne
     */
    @SelectProvider(type = SelectMapperTemplate.class, method = "dynamicSql")
    T selectOne(T condition);
}
