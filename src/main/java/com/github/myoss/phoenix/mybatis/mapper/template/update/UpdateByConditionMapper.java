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

package com.github.myoss.phoenix.mybatis.mapper.template.update;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.UpdateProvider;

import com.github.myoss.phoenix.mybatis.mapper.annotation.RegisterMapper;
import com.github.myoss.phoenix.mybatis.mapper.template.update.impl.UpdateMapperTemplate;

/**
 * 更新记录通用 Mapper 接口
 *
 * @author Jerry.Chen 2018年4月29日 下午9:15:55
 */
@RegisterMapper
public interface UpdateByConditionMapper<T> {
    /**
     * 根据条件更新记录（只会更新有值的字段）
     *
     * @param record 待更新的实体对象
     * @param condition 匹配的条件
     * @return SQL执行成功之后，影响的行数
     * @see UpdateMapperTemplate#updateByCondition
     */
    @UpdateProvider(type = UpdateMapperTemplate.class, method = "dynamicSql")
    int updateByCondition(@Param("record") T record, @Param("condition") T condition);
}
