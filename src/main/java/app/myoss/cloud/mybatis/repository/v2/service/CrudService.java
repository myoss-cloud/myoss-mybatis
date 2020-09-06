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

package app.myoss.cloud.mybatis.repository.v2.service;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import app.myoss.cloud.core.lang.dto.Page;

/**
 * 封装数据库表增、删、改、查常用操作
 *
 * @param <T> 实体类
 * @author Jerry.Chen
 * @since 2020年9月6日 下午2:17:20
 */
public interface CrudService<T> {
    /**
     * 创建新的记录
     *
     * @param record 待保存的实体对象
     * @param <I> 主键类型
     * @param optionParam 可选参数，默认为 {@code null }
     * @return 创建结果
     */
    <I> I create(T record, Object optionParam);

    /**
     * 创建新的记录
     *
     * @param record 待保存的实体对象
     * @param <I> 主键类型
     * @return 创建结果
     */
    <I> I create(T record);

    /**
     * 批量创建新的记录
     *
     * @param records 待保存的实体对象
     * @param optionParam 可选参数，默认为 {@code null }
     */
    void createBatch(List<T> records, Object optionParam);

    /**
     * 批量创建新的记录
     *
     * @param records 待保存的实体对象
     */
    void createBatch(List<T> records);

    /**
     * 先校验记录是否已经存在，如果不存在则创建新的记录，如果已经存在返回原记录的主键信息
     *
     * @param record 待保存的实体对象
     * @param <I> 主键类型
     * @return 创建结果
     */
    <I> I save(T record);

    /**
     * 先校验记录是否已经存在，如果不存在则创建新的记录，如果已经存在返回原记录的主键信息
     *
     * @param record 待保存的实体对象
     * @param optionParam 可选参数，默认为 {@code null }
     * @param <I> 主键类型
     * @return 创建结果
     */
    <I> I save(T record, Object optionParam);

    /**
     * 先校验记录是否已经存在，如果不存在则创建新的记录，如果已经存在则更新原有的记录
     *
     * @param record 待保存的实体对象
     * @param <I> 主键类型
     * @return 创建结果
     */
    <I> I saveOrUpdate(T record);

    /**
     * 先校验记录是否已经存在，如果不存在则创建新的记录，如果已经存在则更新原有的记录
     *
     * @param record 待保存的实体对象
     * @param optionParam 可选参数，默认为 {@code null }
     * @param <I> 主键类型
     * @return 创建结果
     */
    <I> I saveOrUpdate(T record, Object optionParam);

    /**
     * 根据主键更新记录
     *
     * @param record 待更新的实体对象
     * @param optionParam 可选参数，默认为 {@code null }
     */
    void updateByPrimaryKey(T record, Object optionParam);

    /**
     * 根据主键更新记录
     *
     * @param record 待更新的实体对象
     */
    void updateByPrimaryKey(T record);

    /**
     * 根据条件更新记录
     *
     * @param record 待更新的实体对象
     * @param condition 匹配的条件
     * @param optionParam 可选参数，默认为 {@code null }
     */
    void updateByCondition(T record, T condition, Object optionParam);

    /**
     * 根据条件更新记录
     *
     * @param record 待更新的实体对象
     * @param condition 匹配的条件
     */
    void updateByCondition(T record, T condition);

    /**
     * 根据条件更新记录
     *
     * @param record 待更新的实体对象，key：是实体类的属性名，value：是属性的值
     * @param condition 匹配的条件
     * @param optionParam 可选参数，默认为 {@code null }
     */
    void updateUseMapByCondition(Map<String, Object> record, T condition, Object optionParam);

    /**
     * 根据条件更新记录
     *
     * @param record 待更新的实体对象，key：是实体类的属性名，value：是属性的值
     * @param condition 匹配的条件
     */
    void updateUseMapByCondition(Map<String, Object> record, T condition);

    /**
     * 根据主键删除记录，如果是逻辑删除的实体，使用逻辑删除，而不是物理删除
     *
     * @param condition 匹配的条件，主键有值的实体对象
     * @param optionParam 可选参数，默认为 {@code null }
     */
    void deleteByPrimaryKey(T condition, Object optionParam);

    /**
     * 根据主键删除记录，如果是逻辑删除的实体，使用逻辑删除，而不是物理删除
     *
     * @param condition 匹配的条件，主键有值的实体对象
     */
    void deleteByPrimaryKey(T condition);

    /**
     * 根据条件删除记录，如果是审计字段的实体，使用逻辑删除，而不是物理删除
     *
     * @param condition 匹配的条件
     * @param optionParam 可选参数，默认为 {@code null }
     */
    void deleteByCondition(T condition, Object optionParam);

    /**
     * 根据条件删除记录，如果是审计字段的实体，使用逻辑删除，而不是物理删除
     *
     * @param condition 匹配的条件
     */
    void deleteByCondition(T condition);

    /**
     * 根据主键查询实体对象
     *
     * @param id 主键
     * @return 对应的实体对象
     */
    T findByPrimaryKey(Serializable id);

    /**
     * 根据主键查询实体对象
     *
     * @param condition 匹配的条件，主键有值的实体对象
     * @return 对应的实体对象
     */
    T findByPrimaryKey(T condition);

    /**
     * 根据条件查询匹配的实体对象
     *
     * @param condition 匹配的条件
     * @return 匹配的实体对象
     */
    T findOne(T condition);

    /**
     * 根据条件查询匹配的实体对象
     *
     * @param condition 匹配的条件
     * @return 匹配的实体对象
     */
    List<T> findList(T condition);

    /**
     * 根据条件查询匹配的实体对象，并支持字段排序
     *
     * @param condition 匹配的条件和排序字段
     * @return 匹配的实体对象
     */
    List<T> findListWithSort(Page<T> condition);

    /**
     * 根据条件查询匹配的实体对象总记录数
     *
     * @param condition 匹配的条件
     * @return 匹配的实体对象总记录数
     */
    Integer findCount(T condition);

    /**
     * 根据条件查询匹配的实体对象总记录数
     *
     * @param condition 匹配的条件
     * @param extraCondition 扩展查询条件，需要自定义
     * @return 匹配的实体对象总记录数
     */
    Integer findCount(T condition, Map<String, Object> extraCondition);

    /**
     * 根据条件查询匹配的实体对象，并进行分页
     *
     * @param condition 匹配的条件
     * @return 匹配的实体对象
     */
    Page<T> findPage(Page<T> condition);

    /**
     * 使用 PageHelper 分页插件，根据条件查询匹配的实体对象，并进行分页
     *
     * @param condition 匹配的条件
     * @param <DTO> 范型类型
     * @return 匹配的实体对象
     */
    <DTO> Page<DTO> findPageByHelper(Page<DTO> condition);
}
