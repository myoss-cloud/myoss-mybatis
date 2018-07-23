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
import com.github.myoss.phoenix.mybatis.mapper.template.select.impl.SelectIncludeLogicDeleteMapperTemplate;
import com.github.myoss.phoenix.mybatis.table.annotation.Column;

/**
 * 查询记录通用 Mapper 接口，不会过滤掉已经被标记为逻辑删除（{@link Column#logicDelete}）的数据
 *
 * @param <T> 实体类
 * @author Jerry.Chen
 * @since 2018年6月11日 下午11:01:23
 */
@RegisterMapper
public interface SelectOneIncludeLogicDeleteMapper<T> {
    /**
     * 根据条件查询匹配的实体对象，只能有一条查询结果记录，有多条查询结果则会抛出异常，不会过滤掉已经被标记为逻辑删除（
     * {@link Column#logicDelete}）的数据
     *
     * @param condition 匹配的条件
     * @return 匹配的实体对象
     * @see SelectIncludeLogicDeleteMapperTemplate#selectOneIncludeLogicDelete
     */
    @SelectProvider(type = SelectIncludeLogicDeleteMapperTemplate.class, method = "dynamicSql")
    T selectOneIncludeLogicDelete(T condition);
}
