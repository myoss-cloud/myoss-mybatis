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

import static com.github.myoss.phoenix.mybatis.repository.utils.DbUtils.checkDBResult;

import java.io.Serializable;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.mapping.SqlCommandType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import com.github.myoss.phoenix.core.constants.PhoenixConstants;
import com.github.myoss.phoenix.core.lang.bean.BeanUtil;
import com.github.myoss.phoenix.core.lang.concurrent.CallableFunc;
import com.github.myoss.phoenix.core.lang.dto.Order;
import com.github.myoss.phoenix.core.lang.dto.Page;
import com.github.myoss.phoenix.core.lang.dto.Result;
import com.github.myoss.phoenix.core.lang.dto.Sort;
import com.github.myoss.phoenix.mybatis.constants.MybatisConstants;
import com.github.myoss.phoenix.mybatis.mapper.template.CrudMapper;
import com.github.myoss.phoenix.mybatis.repository.entity.LogicDeleteEntity;
import com.github.myoss.phoenix.mybatis.repository.entity.PrimaryKeyEntity;
import com.github.myoss.phoenix.mybatis.repository.service.CrudService;
import com.github.myoss.phoenix.mybatis.table.TableColumnInfo;
import com.github.myoss.phoenix.mybatis.table.TableInfo;
import com.github.myoss.phoenix.mybatis.table.TableMetaObject;

import lombok.extern.slf4j.Slf4j;

/**
 * 实现数据库表增、删、改、查常用操作的基类
 *
 * @param <M> "实体类"的 Mapper Interface 接口
 * @param <T> 实体类
 * @author Jerry.Chen
 * @since 2018年5月9日 下午2:09:18
 */
@Slf4j
public class BaseCrudServiceImpl<M extends CrudMapper<T>, T> implements CrudService<T> {
    protected Class<?>            mapperClass;
    protected Class<?>            entityClass;
    protected TableInfo           tableInfo;
    /**
     * 表中的所有字段，用于校验字段名，防止sql注入
     */
    protected Map<String, String> fieldColumns;
    protected M                   crudMapper;

    /**
     * 初始化实现数据库表增、删、改、查常用操作的基类
     */
    public BaseCrudServiceImpl() {
        Class<? extends BaseCrudServiceImpl> clazz = this.getClass();
        Type genType = clazz.getGenericSuperclass();
        Type[] params = ((ParameterizedType) genType).getActualTypeArguments();
        this.mapperClass = (Class) params[0];
        this.entityClass = (Class) params[1];
    }

    /**
     * 使用 Spring 自动注入"实体类"的 Mapper Interface 接口代理对象
     *
     * @param crudMapper "实体类"的 Mapper Interface 接口代理对象
     */
    @Autowired
    public void setCrudMapper(M crudMapper) {
        this.crudMapper = crudMapper;
        this.tableInfo = TableMetaObject.getTableInfo(this.entityClass);
        if (this.tableInfo != null) {
            this.fieldColumns = Collections.unmodifiableMap(this.tableInfo.getColumns().stream().collect(
                    Collectors.toMap(TableColumnInfo::getProperty, TableColumnInfo::getActualColumn)));
        } else {
            log.error("[{}] getTableInfo failed in [{}]", this.entityClass, this.getClass());
        }
    }

    /**
     * 检查待保存的记录的字段是否有null值
     *
     * @param result 执行结果
     * @param record 实体对象
     * @param optionParam 可选参数，默认为 {@code null }
     * @return true: 校验成功; false: 校验失败
     */
    protected boolean checkNull4Create(Result<?> result, T record, Object optionParam) {
        if (!result.isSuccess()) {
            return false;
        }
        if (record == null) {
            result.setSuccess(false).setErrorCode(MybatisConstants.VALUE_IS_BLANK).setErrorMsg("实体对象不能为空");
        }
        return result.isSuccess();
    }

    /**
     * 检查主键字段是否为空
     *
     * @param sqlCommandType 执行的 SQL 命令类型
     * @param result 执行结果
     * @param id 主键值
     * @return true: 校验成功; false: 校验失败
     */
    protected boolean checkPrimaryKeyIsNull(SqlCommandType sqlCommandType, Result<?> result, Serializable id) {
        if (!result.isSuccess()) {
            return false;
        }
        if (id == null) {
            result.setSuccess(false).setErrorCode(MybatisConstants.VALUE_IS_BLANK).setErrorMsg("主键字段不能为空");
        }
        return result.isSuccess();
    }

    /**
     * 检查实体和主键字段是否为空
     *
     * @param sqlCommandType 执行的 SQL 命令类型
     * @param record 实体对象
     * @param checkAll 检查所有的主键字段值是否为空（false: 只要有一个主键字段为空，则校验失败）
     * @return true: 校验成功; false: 校验失败
     */
    protected boolean checkPrimaryKeyIsNull(SqlCommandType sqlCommandType, Object record, boolean checkAll) {
        boolean isNull = record == null;
        if (!isNull) {
            int nullCount = 0;
            Set<TableColumnInfo> primaryKeyColumns = tableInfo.getPrimaryKeyColumns();
            for (TableColumnInfo columnInfo : primaryKeyColumns) {
                Object value = BeanUtil.methodInvoke(columnInfo.getPropertyDescriptor().getReadMethod(), record);
                if (value == null) {
                    nullCount++;
                } else if (value instanceof CharSequence && StringUtils.isBlank((CharSequence) value)) {
                    nullCount++;
                }
                if (nullCount > 0 && !checkAll) {
                    isNull = true;
                    break;
                }
            }
            if (checkAll && nullCount > 0 && nullCount == primaryKeyColumns.size()) {
                isNull = true;
            }
        }
        return isNull;
    }

    /**
     * 检查实体和主键字段是否为空
     *
     * @param sqlCommandType 执行的 SQL 命令类型
     * @param result 执行结果
     * @param record 实体对象
     * @return true: 校验成功; false: 校验失败
     */
    protected boolean checkPrimaryKeyIsNull(SqlCommandType sqlCommandType, Result<?> result, Object record) {
        if (!result.isSuccess()) {
            return false;
        }
        boolean isNull = checkPrimaryKeyIsNull(sqlCommandType, record, true);
        if (isNull) {
            result.setSuccess(false).setErrorCode(MybatisConstants.VALUE_IS_BLANK).setErrorMsg("主键字段不能为空");
        }
        return result.isSuccess();
    }

    /**
     * 检查通用查询条件字段是否为空，这里只检查主键id是否为空，防止全表扫描
     *
     * @param sqlCommandType 执行的 SQL 命令类型
     * @param result 执行结果
     * @param condition 查询条件
     * @param extraCondition 扩展查询条件，需要自定义
     * @return true: 校验成功; false: 校验失败
     */
    protected boolean checkCommonQueryConditionIsAllNull(SqlCommandType sqlCommandType, Result<?> result, T condition,
                                                         Map<String, Object> extraCondition) {
        if (!result.isSuccess()) {
            return false;
        }
        return checkPrimaryKeyIsNull(sqlCommandType, result, condition);
    }

    /**
     * 校验分页查询条件字段是否有空值，默认不做任何校验，子类去重写
     *
     * @param condition 分页查询条件
     * @param result 分页查询返回结果
     * @return true: 校验成功; false: 校验失败
     */
    protected boolean checkPageConditionIsAllNull(Page<T> condition, Page<T> result) {
        // 分页查询，默认不做任何校验
        return result.isSuccess();
    }

    /**
     * 检查待保存的记录的字段是否符合预期的格式
     *
     * @param result 执行结果
     * @param record 实体对象
     * @param optionParam 可选参数，默认为 {@code null }
     * @return true: 校验成功; false: 校验失败
     */
    protected boolean validFieldValue(Result<?> result, T record, Object optionParam) {
        if (!result.isSuccess()) {
            return false;
        }
        if (record == null) {
            result.setSuccess(false).setErrorCode(MybatisConstants.VALUE_IS_BLANK).setErrorMsg("实体对象不能为空");
        }
        return result.isSuccess();
    }

    /**
     * 检查待保存的记录的字段是否符合预期的格式
     *
     * @param result 执行结果
     * @param record 实体对象
     * @param optionParam 可选参数，默认为 {@code null }
     * @return true: 校验成功; false: 校验失败
     */
    protected boolean validFieldValue(Result<?> result, Map<String, Object> record, Object optionParam) {
        if (!result.isSuccess()) {
            return false;
        }
        if (record == null) {
            result.setSuccess(false).setErrorCode(MybatisConstants.VALUE_IS_BLANK).setErrorMsg("实体对象不能为空");
        }
        return result.isSuccess();
    }

    /**
     * 检查待保存的记录的字段是否符合预期的格式
     *
     * @param result 执行结果
     * @param record 实体对象
     * @param optionParam 可选参数，默认为 {@code null }
     * @return true: 校验成功; false: 校验失败
     */
    protected boolean createValidate(Result<?> result, T record, Object optionParam) {
        if (!checkNull4Create(result, record, optionParam)) {
            return false;
        }
        return validFieldValue(result, record, optionParam);
    }

    /**
     * 将 {@code Map} 中的 {@code key} 转换成数据库字段名，会校验数据库字段名，防止SQL注入
     *
     * @param record 待更新的实体对象，key：是数据库列名，value：是数据库列的值
     * @return 数据库字段列表
     */
    protected Map<String, Object> convertToUpdateUseMap(Map<String, Object> record) {
        Map<String, Object> updateMap = new HashMap<>(record.size());
        for (Entry<String, Object> entry : record.entrySet()) {
            String key = entry.getKey();
            String columnName = fieldColumns.get(key);
            if (columnName != null) {
                // 校验字段名，防止SQL注入
                updateMap.put(columnName, entry.getValue());
            } else {
                log.error("[{}] ignored invalid filed: {}", this.getClass(), key);
            }
        }
        return updateMap;
    }

    /**
     * 将实体类排序字段转换成数据库字段排序，会校验数据库字段名，防止SQL注入
     *
     * @param sort 实体类排序字段
     * @return 数据库字段排序
     */
    protected List<Order> convertToOrders(Sort sort) {
        if (sort == null || CollectionUtils.isEmpty(sort.getOrders())) {
            return null;
        }
        List<Order> orders = new ArrayList<>(sort.getOrders().size());
        for (Order item : sort.getOrders()) {
            String columnName = fieldColumns.get(item.getProperty());
            if (columnName != null) {
                // 校验字段名，防止SQL注入
                Order order = new Order(item.getDirection(), columnName);
                orders.add(order);
            } else {
                log.error("[{}] ignored invalid filed: {}", this.getClass(), item.getProperty());
            }
        }
        return orders;
    }

    /**
     * 查询存在的记录，用于"检查待保存的实体对象是否已经有存在相同的记录（幂等校验）"
     *
     * @param result 执行结果
     * @param record 待保存的实体对象
     * @return 存在的记录
     * @see #checkRecordIfExist4Create(Result, Object)
     * @see #checkRecordIfExist4Update(Result, Object)
     */
    protected List<T> findExistRecord4CheckRecord(Result<?> result, T record) {
        return null;
    }

    /**
     * 检查待保存的实体对象是否已经有存在相同的记录（幂等校验）
     *
     * @param result 执行结果
     * @param record 实体对象
     * @return true: 存在相同记录, false: 不存在相同记录
     * @see #findExistRecord4CheckRecord(Result, Object)
     */
    protected boolean checkRecordIfExist4Create(Result<?> result, T record) {
        List<T> exists = findExistRecord4CheckRecord(result, record);
        if (CollectionUtils.isEmpty(exists)) {
            return false;
        }
        result.setSuccess(false).setErrorCode(MybatisConstants.MORE_RECORDS);
        T item = exists.get(0);
        StringBuilder errorMsg = new StringBuilder();
        Iterator<TableColumnInfo> iterator = tableInfo.getPrimaryKeyColumns().iterator();
        boolean hasNext = iterator.hasNext();
        while (hasNext) {
            TableColumnInfo columnInfo = iterator.next();
            Object value = BeanUtil.methodInvoke(columnInfo.getPropertyDescriptor().getReadMethod(), item);
            errorMsg.append(columnInfo.getProperty()).append("=").append(value);
            hasNext = iterator.hasNext();
            if (hasNext) {
                errorMsg.append(", ");
            }
        }
        if (errorMsg.length() > 0) {
            errorMsg.insert(0, "已经存在相同的记录，主键值[").append("]");
            result.setErrorMsg(errorMsg.toString());
        } else {
            result.setErrorMsg("已经存在相同的记录");
        }
        return true;
    }

    /**
     * 检查待更新的实体对象是否已经有存在相同的记录（幂等校验）。如果实体的类型是 {@link LogicDeleteEntity}
     * ，如果数据是逻辑删除，则校验通过。
     *
     * @param result 执行结果
     * @param record 实体对象
     * @return true: 存在相同记录, false: 不存在相同记录
     * @see #findExistRecord4CheckRecord(Result, Object)
     */
    protected boolean checkRecordIfExist4Update(Result<?> result, T record) {
        if (record instanceof LogicDeleteEntity
                && StringUtils.equals(((LogicDeleteEntity) record).getIsDeleted(), PhoenixConstants.Y)) {
            return false;
        }
        List<T> exists = findExistRecord4CheckRecord(result, record);
        if (CollectionUtils.isEmpty(exists)) {
            return false;
        }
        if (record instanceof PrimaryKeyEntity) {
            // 使用主键实体类的字段比较
            Serializable id = ((PrimaryKeyEntity) record).getPrimaryKey();
            for (T item : exists) {
                Serializable id1 = ((PrimaryKeyEntity) item).getPrimaryKey();
                if (!id1.equals(id)) {
                    result.setSuccess(false).setErrorCode(MybatisConstants.MORE_RECORDS).setErrorMsg(
                            "已经存在相同的记录，主键id=" + id1);
                    return true;
                }
            }
            return false;
        }

        Set<TableColumnInfo> primaryKeyColumns = tableInfo.getPrimaryKeyColumns();
        if (!CollectionUtils.isEmpty(primaryKeyColumns)) {
            // 使用主键字段比较
            List<Object> sourceValues = new ArrayList<>(primaryKeyColumns.size());
            for (TableColumnInfo columnInfo : primaryKeyColumns) {
                Object value = BeanUtil.methodInvoke(columnInfo.getPropertyDescriptor().getReadMethod(), record);
                sourceValues.add(value);
            }
            for (T item : exists) {
                List<Object> itemValues = new ArrayList<>(primaryKeyColumns.size());
                for (TableColumnInfo columnInfo : primaryKeyColumns) {
                    Object value = BeanUtil.methodInvoke(columnInfo.getPropertyDescriptor().getReadMethod(), item);
                    itemValues.add(value);
                }
                if (!Objects.deepEquals(sourceValues, itemValues)) {
                    StringBuilder errorMsg = new StringBuilder("已经存在相同的记录，主键值[");
                    int idx = 0;
                    for (TableColumnInfo columnInfo : primaryKeyColumns) {
                        Object value = itemValues.get(idx++);
                        errorMsg.append(columnInfo.getProperty()).append("=").append(value);
                        if (idx < itemValues.size()) {
                            errorMsg.append(", ");
                        }
                    }
                    result.setErrorMsg(errorMsg.append("]").toString());
                }
            }
        } else {
            // 不支持，需要之类重写
            throw new UnsupportedOperationException();
        }
        return false;
    }

    /**
     * 检查待更新的实体对象是否已经有存在相同的记录（幂等校验）。默认未实现逻辑，请子类中重写。
     *
     * @param result 执行结果
     * @param record 实体对象
     * @return true: 存在相同记录, false: 不存在相同记录
     * @see #findExistRecord4CheckRecord(Result, Object)
     */
    protected boolean checkRecordIfExist4Update(Result<?> result, Map<String, Object> record) {
        return false;
    }

    /**
     * 保存新记录的时候，设置通用字段的值
     *
     * @param record 待保存的实体对象
     * @param optionParam 可选参数，默认为 {@code null }
     */
    protected void setValue4Create(T record, Object optionParam) {

    }

    /**
     * 更新记录的时候，设置通用字段的值
     *
     * @param record 待更新的实体对象
     * @param optionParam 可选参数，默认为 {@code null }
     */
    protected void setValue4Update(T record, Object optionParam) {

    }

    /**
     * 更新记录的时候，设置通用字段的值
     *
     * @param record 待更新的实体对象
     * @param optionParam 可选参数，默认为 {@code null }
     */
    protected void setValue4Update(Map<String, Object> record, Object optionParam) {

    }

    /**
     * 设置分页查询返回值需要的额外字段，子类去重写
     *
     * @param condition 分页查询条件
     * @param result 分页查询返回结果
     */
    protected void addPageExtraInfo(Page<T> condition, Page<T> result) {

    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public <I> Result<I> create(T record, Object optionParam) {
        Result<I> result = new Result<>();
        boolean validate = createValidate(result, record, optionParam);
        if (!validate) {
            return result;
        }
        return createCallable(result, record, optionParam, () -> create(result, record, optionParam));
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public <I> Result<I> create(T record) {
        return create(record, null);
    }

    /**
     * 用于重写创建的方法，比如加锁创建
     *
     * @param result 保存的结果
     * @param record 待保存的实体对象
     * @param optionParam 可选参数，默认为 {@code null }
     * @param createCallFunc 保存方法回调类，参考：{@link #create(Result, Object, Object)}
     * @param <I> 主键类型
     * @return 返回执行结果，默认返回的是 {@code result } 参数，可以被子类覆盖重写
     */
    protected <I> Result<I> createCallable(Result<I> result, T record, Object optionParam,
                                           CallableFunc<Result<I>> createCallFunc) {
        return createCallFunc.call();
    }

    /**
     * 创建新的记录，{@link #create(Object)} 方法的最后一步调用
     *
     * @param result 保存的结果
     * @param record 待保存的实体对象
     * @param <I> 主键类型
     * @param optionParam 可选参数，默认为 {@code null }
     * @return 返回执行结果，默认返回的是 {@code result } 参数，可以被子类覆盖重写
     */
    @SuppressWarnings("unchecked")
    protected <I> Result<I> create(Result<I> result, T record, Object optionParam) {
        boolean ifExist = checkRecordIfExist4Create(result, record);
        if (!ifExist && result.isSuccess()) {
            setValue4Create(record, optionParam);
            boolean flag = checkDBResult(crudMapper.insert(record));
            if (flag) {
                Set<TableColumnInfo> primaryKeyColumns = tableInfo.getPrimaryKeyColumns();
                int size = primaryKeyColumns.size();
                if (size == 0) {
                    // 忽略没有主键字段的情况
                    return result;
                }
                Object value = (size == 1 ? null : new Object[size]);
                int idx = 0;
                for (TableColumnInfo columnInfo : primaryKeyColumns) {
                    I tmp = BeanUtil.methodInvoke(columnInfo.getPropertyDescriptor().getReadMethod(), record);
                    if (size > 1) {
                        // 如果有多个主键字段，使用数组返回
                        ((Object[]) value)[idx++] = tmp;
                    } else {
                        value = tmp;
                    }
                }
                result.setValue((I) value);
            } else {
                result.setSuccess(false).setErrorCode(MybatisConstants.INSERT_DB_FAILED).setErrorMsg("插入失败，请检查");
            }
        }
        return result;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public Result<Boolean> createBatch(List<T> records, Object optionParam) {
        Result<Boolean> result = new Result<>(false);
        if (CollectionUtils.isEmpty(records)) {
            return result.setValue(true);
        }
        return createBatchCallable(result, records, optionParam, () -> createBatch(result, records, optionParam));
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public Result<Boolean> createBatch(List<T> records) {
        return createBatch(records, null);
    }

    /**
     * 用于重写创建的方法，比如加锁创建
     *
     * @param result 保存的结果
     * @param records 待保存的实体对象
     * @param optionParam 可选参数，默认为 {@code null }
     * @param createCallFunc 保存方法回调类，参考：
     *            {@link #createBatch(Result, List, Object)}
     * @return 返回执行结果，默认返回的是 {@code result } 参数，可以被子类覆盖重写
     */
    protected Result<Boolean> createBatchCallable(Result<Boolean> result, List<T> records, Object optionParam,
                                                  CallableFunc<Result<Boolean>> createCallFunc) {
        return createCallFunc.call();
    }

    /**
     * 创建新的记录，{@link #createBatch(List)} 方法的最后一步调用
     *
     * @param result 保存的结果
     * @param records 待保存的实体对象
     * @param optionParam 可选参数，默认为 {@code null }
     * @return 返回执行结果，默认返回的是 {@code result } 参数，可以被子类覆盖重写
     */
    protected Result<Boolean> createBatch(Result<Boolean> result, List<T> records, Object optionParam) {
        for (T record : records) {
            if (!createValidate(result, record, optionParam)) {
                return result;
            }
            boolean ifExist = checkRecordIfExist4Create(result, record);
            if (ifExist || !result.isSuccess()) {
                return result;
            }
        }
        // 先校验完数据格式，再设置字段的值
        for (T record : records) {
            setValue4Create(record, optionParam);
            boolean flag = checkDBResult(crudMapper.insert(record));
            if (!flag) {
                return result.setSuccess(false).setErrorCode(MybatisConstants.INSERT_DB_FAILED).setErrorMsg(
                        "插入失败，请检查。[" + record + "]");
            }
        }
        return result.setValue(true);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public Result<Boolean> updateByPrimaryKey(T record, Object optionParam) {
        Result<Boolean> result = new Result<>(false);
        checkPrimaryKeyIsNull(SqlCommandType.UPDATE, result, record);
        validFieldValue(result, record, optionParam);
        if (!result.isSuccess()) {
            return result;
        }
        return updateByPrimaryKeyCallable(result, record, optionParam,
                () -> updateByPrimaryKey(result, record, optionParam));
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public Result<Boolean> updateByPrimaryKey(T record) {
        return updateByPrimaryKey(record, null);
    }

    /**
     * 用于重写更新的方法，比如加锁更新
     *
     * @param record 待更新的实体对象
     * @param result 更新的结果
     * @param optionParam 可选参数，默认为 {@code null }
     * @param updateCallFunc 更新方法回调类，参考：
     *            {@link #updateByPrimaryKey(Result, Object, Object)}
     * @return 返回执行结果，默认返回的是 {@code result } 参数，可以被子类覆盖重写
     */
    protected Result<Boolean> updateByPrimaryKeyCallable(Result<Boolean> result, T record, Object optionParam,
                                                         CallableFunc<Result<Boolean>> updateCallFunc) {
        return updateCallFunc.call();
    }

    /**
     * 更新记录，{@link #updateByPrimaryKey(Object)} 方法的最后一步调用
     *
     * @param result 更新的结果
     * @param record 待更新的实体对象
     * @param optionParam 可选参数，默认为 {@code null }
     * @return 返回执行结果，默认返回的是 {@code result } 参数，可以被子类覆盖重写
     */
    protected Result<Boolean> updateByPrimaryKey(Result<Boolean> result, T record, Object optionParam) {
        boolean ifExist = checkRecordIfExist4Update(result, record);
        if (!ifExist && result.isSuccess()) {
            setValue4Update(record, optionParam);
            boolean flag = checkDBResult(crudMapper.updateByPrimaryKey(record));
            if (!flag) {
                result.setSuccess(false).setErrorCode(MybatisConstants.NOT_MATCH_RECORDS).setErrorMsg("更新失败，未匹配到相应的记录");
            } else {
                result.setValue(true);
            }
        }
        return result;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public Result<Boolean> updateByCondition(T record, T condition, Object optionParam) {
        Result<Boolean> result = new Result<>(false);
        validFieldValue(result, record, optionParam);
        checkCommonQueryConditionIsAllNull(SqlCommandType.UPDATE, result, condition, null);
        if (!result.isSuccess()) {
            return result;
        }
        return updateByConditionCallable(result, record, condition, optionParam,
                () -> updateByCondition(result, record, condition, optionParam));
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public Result<Boolean> updateByCondition(T record, T condition) {
        return updateByCondition(record, condition, null);
    }

    /**
     * 用于重写更新的方法，比如加锁更新
     *
     * @param result 更新的结果
     * @param record 待更新的实体对象
     * @param condition 匹配的条件
     * @param optionParam 可选参数，默认为 {@code null }
     * @param updateCallFunc 更新方法回调类，参考：
     *            {@link #updateByCondition(Result, Object, Object, Object)}
     * @return 返回执行结果，默认返回的是 {@code result } 参数，可以被子类覆盖重写
     */
    protected Result<Boolean> updateByConditionCallable(Result<Boolean> result, T record, T condition,
                                                        Object optionParam,
                                                        CallableFunc<Result<Boolean>> updateCallFunc) {
        return updateCallFunc.call();
    }

    /**
     * 更新记录，{@link #updateByCondition(Object, Object)} 方法的最后一步调用
     *
     * @param result 更新的结果
     * @param record 待更新的实体对象
     * @param condition 匹配的条件
     * @param optionParam 可选参数，默认为 {@code null }
     * @return 返回执行结果，默认返回的是 {@code result } 参数，可以被子类覆盖重写
     */
    protected Result<Boolean> updateByCondition(Result<Boolean> result, T record, T condition, Object optionParam) {
        boolean ifExist = checkRecordIfExist4Update(result, record);
        if (!ifExist && result.isSuccess()) {
            setValue4Update(record, optionParam);
            boolean flag = checkDBResult(crudMapper.updateByCondition(record, condition));
            if (!flag) {
                result.setSuccess(false).setErrorCode(MybatisConstants.NOT_MATCH_RECORDS).setErrorMsg("更新失败，未匹配到相应的记录");
            } else {
                result.setValue(true);
            }
        }
        return result;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public Result<Boolean> updateUseMapByCondition(Map<String, Object> record, T condition, Object optionParam) {
        Result<Boolean> result = new Result<>(false);
        if (!validFieldValue(result, record, optionParam)) {
            return result;
        }
        checkCommonQueryConditionIsAllNull(SqlCommandType.UPDATE, result, condition, null);
        if (!result.isSuccess()) {
            return result;
        }
        return updateUseMapByConditionCallable(result, record, condition, optionParam,
                () -> updateUseMapByCondition(result, record, condition, optionParam));
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public Result<Boolean> updateUseMapByCondition(Map<String, Object> record, T condition) {
        return updateUseMapByCondition(record, condition, null);
    }

    /**
     * 用于重写更新的方法，比如加锁更新
     *
     * @param result 更新的结果
     * @param record 待更新的实体对象，key：是数据库列名，value：是数据库列的值
     * @param condition 匹配的条件
     * @param optionParam 可选参数，默认为 {@code null }
     * @param updateCallFunc 更新方法回调类，参考：
     *            {@link #updateUseMapByCondition(Result, Map, Object, Object)}
     * @return 返回执行结果，默认返回的是 {@code result } 参数，可以被子类覆盖重写
     */
    protected Result<Boolean> updateUseMapByConditionCallable(Result<Boolean> result, Map<String, Object> record,
                                                              T condition, Object optionParam,
                                                              CallableFunc<Result<Boolean>> updateCallFunc) {
        return updateCallFunc.call();
    }

    /**
     * 更新记录，{@link #updateByCondition(Object, Object)} 方法的最后一步调用
     *
     * @param result 更新的结果
     * @param record 待更新的实体对象，key：是数据库列名，value：是数据库列的值
     * @param condition 匹配的条件
     * @param optionParam 可选参数，默认为 {@code null }
     * @return 返回执行结果，默认返回的是 {@code result } 参数，可以被子类覆盖重写
     */
    protected Result<Boolean> updateUseMapByCondition(Result<Boolean> result, Map<String, Object> record, T condition,
                                                      Object optionParam) {
        boolean ifExist = checkRecordIfExist4Update(result, record);
        if (!ifExist && result.isSuccess()) {
            setValue4Update(record, optionParam);
            Map<String, Object> updateMap = convertToUpdateUseMap(record);
            boolean flag = checkDBResult(crudMapper.updateUseMapByCondition(updateMap, condition));
            if (!flag) {
                result.setSuccess(false).setErrorCode(MybatisConstants.NOT_MATCH_RECORDS).setErrorMsg("更新失败，未匹配到相应的记录");
            } else {
                result.setValue(true);
            }
        }
        return result;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public Result<Boolean> deleteByPrimaryKey(T condition, Object optionParam) {
        Result<Boolean> result = new Result<>(false);
        if (checkPrimaryKeyIsNull(SqlCommandType.DELETE, result, condition)) {
            boolean flag = checkDBResult(crudMapper.deleteWithPrimaryKey(condition));
            if (!flag) {
                result.setSuccess(false).setErrorCode(MybatisConstants.NOT_MATCH_RECORDS).setErrorMsg("更新失败，未匹配到相应的记录");
            } else {
                result.setValue(true);
            }
        }
        return result;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public Result<Boolean> deleteByPrimaryKey(T condition) {
        return deleteByPrimaryKey(condition, null);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public Result<Boolean> deleteByCondition(T condition, Object optionParam) {
        Result<Boolean> result = new Result<>(false);
        if (checkCommonQueryConditionIsAllNull(SqlCommandType.DELETE, result, condition, null)) {
            boolean flag = checkDBResult(crudMapper.deleteByCondition(condition));
            if (!flag) {
                result.setSuccess(false).setErrorCode(MybatisConstants.NOT_MATCH_RECORDS).setErrorMsg("更新失败，未匹配到相应的记录");
            } else {
                result.setValue(true);
            }
        }
        return result;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public Result<Boolean> deleteByCondition(T condition) {
        return deleteByCondition(condition, null);
    }

    @Override
    public Result<T> findByPrimaryKey(Serializable id) {
        Result<T> result = new Result<>();
        if (checkPrimaryKeyIsNull(SqlCommandType.SELECT, result, id)) {
            T entity = crudMapper.selectByPrimaryKey(id);
            result.setValue(entity);
        }
        return result;
    }

    @Override
    public Result<T> findByPrimaryKey(T condition) {
        Result<T> result = new Result<>();
        if (checkPrimaryKeyIsNull(SqlCommandType.SELECT, result, condition)) {
            T entity = crudMapper.selectWithPrimaryKey(condition);
            result.setValue(entity);
        }
        return result;
    }

    @Override
    public Result<T> findOne(T condition) {
        Result<T> result = new Result<>();
        if (checkCommonQueryConditionIsAllNull(SqlCommandType.SELECT, result, condition, null)) {
            T one = crudMapper.selectOne(condition);
            result.setValue(one);
        }
        return result;
    }

    @Override
    public Result<List<T>> findList(T condition) {
        Result<List<T>> result = new Result<>();
        if (checkCommonQueryConditionIsAllNull(SqlCommandType.SELECT, result, condition, null)) {
            List<T> list = crudMapper.selectList(condition);
            result.setValue(list);
        }
        return result;
    }

    @Override
    public Result<List<T>> findListWithSort(Page<T> condition) {
        Result<List<T>> result = new Result<>();
        T param = condition.getParam();
        Map<String, Object> extraInfo = condition.getExtraInfo();
        if (checkCommonQueryConditionIsAllNull(SqlCommandType.SELECT, result, param, extraInfo)) {
            Sort sort = condition.getSort();
            List<Order> orders = convertToOrders(sort);
            List<T> list = crudMapper.selectListWithSort2(param, extraInfo, orders);
            result.setValue(list);
        }
        return result;
    }

    @Override
    public Result<Integer> findCount(T condition) {
        Result<Integer> result = new Result<>();
        if (checkCommonQueryConditionIsAllNull(SqlCommandType.SELECT, result, condition, null)) {
            int count = crudMapper.selectCount(condition);
            result.setValue(count);
        }
        return result;
    }

    @Override
    public Result<Integer> findCount(T condition, Map<String, Object> extraCondition) {
        Result<Integer> result = new Result<>();
        if (checkCommonQueryConditionIsAllNull(SqlCommandType.SELECT, result, condition, extraCondition)) {
            int count = crudMapper.selectCount2(condition, extraCondition);
            result.setValue(count);
        }
        return result;
    }

    @Override
    public Page<T> findPage(Page<T> condition) {
        Page<T> result = new Page<>();
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
        List<T> details = crudMapper.selectPage2(param, extraInfo, pageStart, pageSize, orders);
        int totalCount = crudMapper.selectCount2(param, extraInfo);
        // 设置额外字段
        addPageExtraInfo(condition, result);

        result.setValue(details).setTotalCount(totalCount).setPageNum(dbPageNum + 1).setPageSize(pageSize);
        return result;
    }
}
