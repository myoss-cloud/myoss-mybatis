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

package com.github.myoss.phoenix.mybatis.repository.service.impl;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.ibatis.mapping.SqlCommandType;

import com.github.myoss.phoenix.core.lang.dto.Order;
import com.github.myoss.phoenix.core.lang.dto.Page;
import com.github.myoss.phoenix.core.lang.dto.Result;
import com.github.myoss.phoenix.core.lang.dto.Sort;
import com.github.myoss.phoenix.mybatis.mapper.template.CrudMapper;
import com.github.myoss.phoenix.mybatis.repository.service.RetrieveIncludeLogicDeleteService;
import com.github.myoss.phoenix.mybatis.table.annotation.Column;

import lombok.extern.slf4j.Slf4j;

/**
 * 实现数据库表增、删、改、查常用操作的基类，查询支持不过滤掉已经被标记为逻辑删除（{@link Column#logicDelete}）的数据
 *
 * @param <M> "实体类"的 Mapper Interface 接口
 * @param <T> 实体类
 * @author Jerry.Chen
 * @since 2018年6月11日 下午11:21:32
 */
@Slf4j
public class RetrieveIncludeLogicDeleteCrudServiceImpl<M extends CrudMapper<T>, T> extends BaseCrudServiceImpl<M, T>
        implements RetrieveIncludeLogicDeleteService<T> {
    /**
     * 标记不过滤掉已经被标记为逻辑删除（{@link Column#logicDelete}）的数据
     */
    public static final String MARK_IS_INCLUDE_LOGIC_DELETE = "isIncludeLogicDelete";

    /**
     * 为 {@code extraCondition} 添加 {@link #MARK_IS_INCLUDE_LOGIC_DELETE} 为
     * {@code true} 的记录，标记查询不需要滤掉已经被标记为逻辑删除（{@link Column#logicDelete}）的数据
     *
     * @param extraCondition 扩展查询条件，需要自定义
     * @return 如果入参 {@code extraCondition} 为 {@code null}，则创建一个新的对象；否则返回
     *         {@code extraCondition} 入参
     */
    protected Map<String, Object> markQueryIsIncludeLogicDelete(Map<String, Object> extraCondition) {
        if (extraCondition == null) {
            extraCondition = new HashMap<>(1);
        }
        extraCondition.put(MARK_IS_INCLUDE_LOGIC_DELETE, true);
        return extraCondition;
    }

    /**
     * 检查查询条件是否需要过滤已经被标记为逻辑删除（{@link Column#logicDelete}）的数据
     *
     * @param extraCondition 扩展查询条件，需要自定义
     * @return true: 不需要滤掉; false: 需要滤掉
     */
    protected boolean checkQueryIsIncludeLogicDelete(Map<String, Object> extraCondition) {
        if (extraCondition == null) {
            return false;
        }
        return BooleanUtils.toBoolean((Boolean) extraCondition.get(MARK_IS_INCLUDE_LOGIC_DELETE));
    }

    @Override
    public Result<T> findByPrimaryKeyIncludeLogicDelete(Serializable id) {
        Result<T> result = new Result<>();
        if (checkPrimaryKeyIsNull(SqlCommandType.SELECT, result, id)) {
            T entity = crudMapper.selectByPrimaryKeyIncludeLogicDelete(id);
            result.setValue(entity);
        }
        return result;
    }

    @Override
    public Result<T> findByPrimaryKeyIncludeLogicDelete(T condition) {
        Result<T> result = new Result<>();
        if (checkPrimaryKeyIsNull(SqlCommandType.SELECT, result, condition)) {
            T entity = crudMapper.selectWithPrimaryKeyIncludeLogicDelete(condition);
            result.setValue(entity);
        }
        return result;
    }

    @Override
    public Result<T> findOneIncludeLogicDelete(T condition) {
        Result<T> result = new Result<>();
        if (checkCommonQueryConditionIsAllNull(SqlCommandType.SELECT, result, condition,
                markQueryIsIncludeLogicDelete(null))) {
            T one = crudMapper.selectOneIncludeLogicDelete(condition);
            result.setValue(one);
        }
        return result;
    }

    @Override
    public Result<List<T>> findListIncludeLogicDelete(T condition) {
        Result<List<T>> result = new Result<>();
        if (checkCommonQueryConditionIsAllNull(SqlCommandType.SELECT, result, condition,
                markQueryIsIncludeLogicDelete(null))) {
            List<T> list = crudMapper.selectListIncludeLogicDelete(condition);
            result.setValue(list);
        }
        return result;
    }

    @Override
    public Result<List<T>> findListWithSortIncludeLogicDelete(Page<T> condition) {
        Result<List<T>> result = new Result<>();
        T param = condition.getParam();
        Map<String, Object> extraInfo = condition.getExtraInfo();
        if (checkCommonQueryConditionIsAllNull(SqlCommandType.SELECT, result, param,
                markQueryIsIncludeLogicDelete(extraInfo))) {
            Sort sort = condition.getSort();
            List<Order> orders = convertToOrders(sort);
            List<T> list = crudMapper.selectListWithSortIncludeLogicDelete2(param, extraInfo, orders);
            result.setValue(list);
        }
        return result;
    }

    @Override
    public Result<Integer> findCountIncludeLogicDelete(T condition) {
        Result<Integer> result = new Result<>();
        if (checkCommonQueryConditionIsAllNull(SqlCommandType.SELECT, result, condition,
                markQueryIsIncludeLogicDelete(null))) {
            int count = crudMapper.selectCountIncludeLogicDelete(condition);
            result.setValue(count);
        }
        return result;
    }

    @Override
    public Result<Integer> findCountIncludeLogicDelete(T condition, Map<String, Object> extraCondition) {
        Result<Integer> result = new Result<>();
        if (checkCommonQueryConditionIsAllNull(SqlCommandType.SELECT, result, condition,
                markQueryIsIncludeLogicDelete(extraCondition))) {
            int count = crudMapper.selectCountIncludeLogicDelete2(condition, extraCondition);
            result.setValue(count);
        }
        return result;
    }

    @Override
    public Page<T> findPageIncludeLogicDelete(Page<T> condition) {
        Page<T> result = new Page<>();
        condition.setExtraInfo(markQueryIsIncludeLogicDelete(condition.getExtraInfo()));
        if (!checkPageConditionIsAllNull(condition, result)) {
            return result;
        }

        int pageSize = condition.getPageSize();
        int pageNum = condition.getPageNum();
        int dbPageNum = Math.max(0, pageNum - 1);
        int pageStart = dbPageNum * pageSize;
        T param = condition.getParam();
        Sort sort = condition.getSort();
        List<Order> orders = convertToOrders(sort);
        Map<String, Object> extraInfo = condition.getExtraInfo();
        List<T> details = crudMapper.selectPageIncludeLogicDelete2(param, extraInfo, pageStart, pageSize, orders);
        int totalCount = crudMapper.selectCountIncludeLogicDelete2(param, extraInfo);
        // 设置额外字段
        addPageExtraInfo(condition, result);

        result.setValue(details).setTotalCount(totalCount).setPageNum(dbPageNum + 1).setPageSize(pageSize);
        return result;
    }
}
