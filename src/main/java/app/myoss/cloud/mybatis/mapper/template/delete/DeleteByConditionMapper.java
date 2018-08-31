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

package app.myoss.cloud.mybatis.mapper.template.delete;

import org.apache.ibatis.annotations.DeleteProvider;
import org.apache.ibatis.annotations.Param;

import app.myoss.cloud.mybatis.mapper.annotation.RegisterMapper;
import app.myoss.cloud.mybatis.mapper.template.delete.impl.DeleteMapperTemplate;

/**
 * 删除记录通用 Mapper 接口
 *
 * @param <T> 实体类
 * @author Jerry.Chen
 * @since 2018年4月29日 下午9:00:55
 */
@RegisterMapper
public interface DeleteByConditionMapper<T> {
    /**
     * 根据条件删除记录
     *
     * @param condition 匹配的条件
     * @return SQL执行成功之后，影响的行数
     * @see DeleteMapperTemplate#deleteByCondition
     */
    @DeleteProvider(type = DeleteMapperTemplate.class, method = "dynamicSql")
    int deleteByCondition(T condition);

    /**
     * 根据条件删除记录，同时支持更新其它字段（只有逻辑删除，才能支持更新逻辑）
     *
     * @param record 待更新的实体对象
     * @param condition 匹配的条件
     * @return SQL执行成功之后，影响的行数
     * @see DeleteMapperTemplate#deleteByConditionAndUpdate
     */
    @DeleteProvider(type = DeleteMapperTemplate.class, method = "dynamicSql")
    int deleteByConditionAndUpdate(@Param("record") T record, @Param("condition") T condition);
}
