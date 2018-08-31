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

package app.myoss.cloud.mybatis.mapper.template.select;

import java.util.Map;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.SelectProvider;

import app.myoss.cloud.mybatis.mapper.annotation.RegisterMapper;
import app.myoss.cloud.mybatis.mapper.template.select.impl.SelectIncludeLogicDeleteMapperTemplate;
import app.myoss.cloud.mybatis.table.annotation.Column;

/**
 * 查询记录通用 Mapper 接口，不会过滤掉已经被标记为逻辑删除（{@link Column#logicDelete}）的数据
 *
 * @param <T> 实体类
 * @author Jerry.Chen
 * @since 2018年6月11日 下午10:54:56
 */
@RegisterMapper
public interface SelectCountIncludeLogicDeleteMapper<T> {
    /**
     * 根据条件查询匹配的实体对象总记录数，不会过滤掉已经被标记为逻辑删除（{@link Column#logicDelete}）的数据
     *
     * @param condition 匹配的条件
     * @return 匹配的实体对象
     * @see SelectIncludeLogicDeleteMapperTemplate#selectCountIncludeLogicDelete
     */
    @SelectProvider(type = SelectIncludeLogicDeleteMapperTemplate.class, method = "dynamicSql")
    int selectCountIncludeLogicDelete(T condition);

    /**
     * 根据条件查询匹配的实体对象总记录数，不会过滤掉已经被标记为逻辑删除（{@link Column#logicDelete}）的数据
     *
     * @param condition 匹配的条件
     * @param extraCondition 扩展可选查询条件，需要自定义
     * @return 匹配的实体对象
     * @see SelectIncludeLogicDeleteMapperTemplate#selectCountIncludeLogicDelete2
     */
    @SelectProvider(type = SelectIncludeLogicDeleteMapperTemplate.class, method = "dynamicSql")
    int selectCountIncludeLogicDelete2(@Param("condition") T condition,
                                       @Param("extraCondition") Map<String, Object> extraCondition);
}
