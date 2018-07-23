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

import org.apache.ibatis.annotations.UpdateProvider;

import com.github.myoss.phoenix.mybatis.mapper.annotation.RegisterMapper;
import com.github.myoss.phoenix.mybatis.mapper.template.update.impl.UpdateMapperTemplate;

/**
 * 更新记录通用 Mapper 接口
 *
 * @param <T> 实体类
 * @author Jerry.Chen
 * @since 2018年4月29日 下午10:05:55
 */
@RegisterMapper
public interface UpdateByPrimaryKeyMapper<T> {
    /**
     * 根据主键id更新记录，字段的值为 null 不会更新
     *
     * @param record 待更新的实体对象
     * @return SQL执行成功之后，影响的行数
     * @see UpdateMapperTemplate#updateByPrimaryKey
     */
    @UpdateProvider(type = UpdateMapperTemplate.class, method = "dynamicSql")
    int updateByPrimaryKey(T record);
}
