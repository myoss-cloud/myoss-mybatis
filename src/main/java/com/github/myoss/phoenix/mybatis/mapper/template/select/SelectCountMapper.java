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

import java.util.Map;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.SelectProvider;

import com.github.myoss.phoenix.mybatis.mapper.annotation.RegisterMapper;
import com.github.myoss.phoenix.mybatis.mapper.template.select.impl.SelectMapperTemplate;

/**
 * 查询记录通用 Mapper 接口
 *
 * @author Jerry.Chen 2018年5月10日 上午10:00:17
 */
@RegisterMapper
public interface SelectCountMapper<T> {
    /**
     * 根据条件查询匹配的实体对象总记录数
     *
     * @param condition 匹配的条件
     * @return 匹配的实体对象
     * @see SelectMapperTemplate#selectCount
     */
    @SelectProvider(type = SelectMapperTemplate.class, method = "dynamicSql")
    int selectCount(T condition);

    /**
     * 根据条件查询匹配的实体对象总记录数
     *
     * @param condition 匹配的条件
     * @param extraCondition 扩展可选查询条件，需要自定义
     * @return 匹配的实体对象
     * @see SelectMapperTemplate#selectCount2
     */
    @SelectProvider(type = SelectMapperTemplate.class, method = "dynamicSql")
    int selectCount2(@Param("condition") T condition, @Param("extraCondition") Map<String, Object> extraCondition);
}
