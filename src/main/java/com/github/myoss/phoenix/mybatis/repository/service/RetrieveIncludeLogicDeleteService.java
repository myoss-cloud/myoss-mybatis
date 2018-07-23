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

package com.github.myoss.phoenix.mybatis.repository.service;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import com.github.myoss.phoenix.core.lang.dto.Page;
import com.github.myoss.phoenix.core.lang.dto.Result;
import com.github.myoss.phoenix.mybatis.table.annotation.Column;

/**
 * 封装数据库查询常用操作，不会过滤掉已经被标记为逻辑删除（{@link Column#logicDelete}）的数据
 *
 * @param <T> 实体类
 * @author Jerry.Chen
 * @since 2018年6月12日 上午12:10:29
 */
public interface RetrieveIncludeLogicDeleteService<T> {
    /**
     * 根据主键查询实体对象，不会过滤掉已经被标记为逻辑删除（{@link Column#logicDelete}）的数据
     *
     * @param id 主键
     * @return 对应的实体对象
     */
    Result<T> findByPrimaryKeyIncludeLogicDelete(Serializable id);

    /**
     * 根据主键查询实体对象，不会过滤掉已经被标记为逻辑删除（{@link Column#logicDelete}）的数据
     *
     * @param condition 匹配的条件，主键有值的实体对象
     * @return 对应的实体对象
     */
    Result<T> findByPrimaryKeyIncludeLogicDelete(T condition);

    /**
     * 根据条件查询匹配的实体对象，不会过滤掉已经被标记为逻辑删除（{@link Column#logicDelete}）的数据
     *
     * @param condition 匹配的条件
     * @return 匹配的实体对象
     */
    Result<T> findOneIncludeLogicDelete(T condition);

    /**
     * 根据条件查询匹配的实体对象，不会过滤掉已经被标记为逻辑删除（{@link Column#logicDelete}）的数据
     *
     * @param condition 匹配的条件
     * @return 匹配的实体对象
     */
    Result<List<T>> findListIncludeLogicDelete(T condition);

    /**
     * 根据条件查询匹配的实体对象，不会过滤掉已经被标记为逻辑删除（{@link Column#logicDelete}）的数据，并支持字段排序
     *
     * @param condition 匹配的条件和排序字段
     * @return 匹配的实体对象
     */
    Result<List<T>> findListWithSortIncludeLogicDelete(Page<T> condition);

    /**
     * 根据条件查询匹配的实体对象总记录数，不会过滤掉已经被标记为逻辑删除（{@link Column#logicDelete}）的数据
     *
     * @param condition 匹配的条件
     * @return 匹配的实体对象总记录数
     */
    Result<Integer> findCountIncludeLogicDelete(T condition);

    /**
     * 根据条件查询匹配的实体对象总记录数，不会过滤掉已经被标记为逻辑删除（{@link Column#logicDelete}）的数据
     *
     * @param condition 匹配的条件
     * @param extraCondition 扩展查询条件，需要自定义
     * @return 匹配的实体对象总记录数
     */
    Result<Integer> findCountIncludeLogicDelete(T condition, Map<String, Object> extraCondition);

    /**
     * 根据条件查询匹配的实体对象，不会过滤掉已经被标记为逻辑删除（{@link Column#logicDelete}）的数据，并进行分页
     *
     * @param condition 匹配的条件
     * @return 匹配的实体对象
     */
    Page<T> findPageIncludeLogicDelete(Page<T> condition);
}
