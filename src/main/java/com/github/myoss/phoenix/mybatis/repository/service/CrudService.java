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

/**
 * 封装数据库表增、删、改、查常用操作
 *
 * @author Jerry.Chen
 * @since 2018年5月9日 下午2:09:05
 */
public interface CrudService<T> {
    /**
     * 创建新的记录
     *
     * @param record 待保存的实体对象
     * @param <I> 主键类型
     * @return 创建结果
     */
    <I> Result<I> create(T record);

    /**
     * 批量创建新的记录
     *
     * @param records 待保存的实体对象
     * @return 创建结果
     */
    Result<Boolean> createBatch(List<T> records);

    /**
     * 根据主键更新记录
     *
     * @param record 待更新的实体对象
     * @return 更新结果
     */
    Result<Boolean> updateByPrimaryKey(T record);

    /**
     * 根据条件更新记录
     *
     * @param record 待更新的实体对象
     * @param condition 匹配的条件
     * @return 更新结果
     */
    Result<Boolean> updateByCondition(T record, T condition);

    /**
     * 根据条件更新记录
     *
     * @param record 待更新的实体对象，key：是数据库列名，value：是数据库列的值
     * @param condition 匹配的条件
     * @return 更新结果
     */
    Result<Boolean> updateUseMapByCondition(Map<String, Object> record, T condition);

    /**
     * 根据主键删除记录，如果是逻辑删除的实体，使用逻辑删除，而不是物理删除
     *
     * @param condition 匹配的条件，主键有值的实体对象
     * @return 影响的行数
     */
    Result<Boolean> deleteByPrimaryKey(T condition);

    /**
     * 根据条件删除记录，如果是审计字段的实体，使用逻辑删除，而不是物理删除
     *
     * @param condition 匹配的条件
     * @return 是否操作成功
     */
    Result<Boolean> deleteByCondition(T condition);

    /**
     * 根据主键查询实体对象
     *
     * @param id 主键
     * @return 对应的实体对象
     */
    Result<T> findByPrimaryKey(Serializable id);

    /**
     * 根据主键查询实体对象
     *
     * @param condition 匹配的条件，主键有值的实体对象
     * @return 对应的实体对象
     */
    Result<T> findByPrimaryKey(T condition);

    /**
     * 根据条件查询匹配的实体对象
     *
     * @param condition 匹配的条件
     * @return 匹配的实体对象
     */
    Result<T> findOne(T condition);

    /**
     * 根据条件查询匹配的实体对象
     *
     * @param condition 匹配的条件
     * @return 匹配的实体对象
     */
    Result<List<T>> findList(T condition);

    /**
     * 根据条件查询匹配的实体对象，并支持字段排序
     *
     * @param condition 匹配的条件和排序字段
     * @return 匹配的实体对象
     */
    Result<List<T>> findListWithSort(Page<T> condition);

    /**
     * 根据条件查询匹配的实体对象总记录数
     *
     * @param condition 匹配的条件
     * @return 匹配的实体对象总记录数
     */
    Result<Integer> findCount(T condition);

    /**
     * 根据条件查询匹配的实体对象总记录数
     *
     * @param condition 匹配的条件
     * @param extraCondition 扩展查询条件，需要自定义
     * @return 匹配的实体对象总记录数
     */
    Result<Integer> findCount(T condition, Map<String, Object> extraCondition);

    /**
     * 根据条件查询匹配的实体对象，并进行分页
     *
     * @param condition 匹配的条件
     * @return 匹配的实体对象
     */
    Page<T> findPage(Page<T> condition);
}
