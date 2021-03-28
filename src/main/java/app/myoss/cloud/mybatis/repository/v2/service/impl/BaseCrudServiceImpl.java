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

package app.myoss.cloud.mybatis.repository.v2.service.impl;

import static app.myoss.cloud.mybatis.repository.utils.DbUtils.checkDBResult;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.StringJoiner;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.mapping.SqlCommandType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import app.myoss.cloud.core.constants.MyossConstants;
import app.myoss.cloud.core.lang.bean.BeanUtil;
import app.myoss.cloud.core.lang.concurrent.CallableFunc;
import app.myoss.cloud.core.lang.dto.Order;
import app.myoss.cloud.core.lang.dto.Page;
import app.myoss.cloud.core.lang.dto.Sort;
import app.myoss.cloud.mybatis.constants.MybatisConstants;
import app.myoss.cloud.mybatis.mapper.template.CrudMapper;
import app.myoss.cloud.mybatis.repository.entity.LogicDeleteEntity;
import app.myoss.cloud.mybatis.repository.entity.PrimaryKeyEntity;
import app.myoss.cloud.mybatis.repository.utils.CrudServiceUtils;
import app.myoss.cloud.mybatis.repository.v2.service.CrudService;
import app.myoss.cloud.mybatis.repository.v2.service.exception.BizServiceException;
import app.myoss.cloud.mybatis.table.TableColumnInfo;
import app.myoss.cloud.mybatis.table.TableInfo;
import app.myoss.cloud.mybatis.table.TableMetaObject;
import lombok.extern.slf4j.Slf4j;

/**
 * 实现数据库表增、删、改、查常用操作的基类
 *
 * @param <M> "实体类"的 Mapper Interface 接口
 * @param <T> 实体类
 * @author Jerry.Chen
 * @since 2020年9月6日 下午2:17:20
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
            this.fieldColumns = Collections.unmodifiableMap(this.tableInfo.getColumns()
                    .stream()
                    .collect(Collectors.toMap(TableColumnInfo::getProperty, TableColumnInfo::getActualColumn)));
        } else {
            log.error("[{}] getTableInfo failed in [{}]", this.entityClass, this.getClass());
        }
    }

    /**
     * 检查待保存的记录的字段是否有null值
     *
     * @param record 实体对象
     * @param optionParam 可选参数，默认为 {@code null }
     */
    protected void checkNull4Create(T record, Object optionParam) {
        if (record == null) {
            throw new BizServiceException(MybatisConstants.VALUE_IS_BLANK, "实体对象不能为空");
        }
    }

    /**
     * 检查主键字段是否为空
     *
     * @param sqlCommandType 执行的 SQL 命令类型
     * @param id 主键值
     */
    protected void checkPrimaryKeyIsNull(SqlCommandType sqlCommandType, Serializable id) {
        if (id == null) {
            throw new BizServiceException(MybatisConstants.VALUE_IS_BLANK, "主键字段不能为空");
        }
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
        return CrudServiceUtils.checkPrimaryKeyIsNull(tableInfo, record, checkAll);
    }

    /**
     * 检查实体和主键字段是否为空
     *
     * @param sqlCommandType 执行的 SQL 命令类型
     * @param record 实体对象
     */
    protected void checkPrimaryKeyIsNull(SqlCommandType sqlCommandType, Object record) {
        boolean isNull = checkPrimaryKeyIsNull(sqlCommandType, record, true);
        if (isNull) {
            throw new BizServiceException(MybatisConstants.VALUE_IS_BLANK, "主键字段不能为空");
        }
    }

    /**
     * 检查通用查询条件字段是否为空，这里只检查主键id是否为空，防止全表扫描
     *
     * @param sqlCommandType 执行的 SQL 命令类型
     * @param condition 查询条件
     * @param extraCondition 扩展查询条件，需要自定义
     */
    protected void checkCommonQueryConditionIsAllNull(SqlCommandType sqlCommandType, T condition,
                                                      Map<String, Object> extraCondition) {
        checkPrimaryKeyIsNull(sqlCommandType, condition);
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
     * @param record 实体对象
     * @param optionParam 可选参数，默认为 {@code null }
     */
    protected void validFieldValue(T record, Object optionParam) {
        if (record == null) {
            throw new BizServiceException(MybatisConstants.VALUE_IS_BLANK, "实体对象不能为空");
        }
    }

    /**
     * 检查待保存的记录的字段是否符合预期的格式
     *
     * @param record 实体对象
     * @param optionParam 可选参数，默认为 {@code null }
     */
    protected void validFieldValue(Map<String, Object> record, Object optionParam) {
        if (record == null) {
            throw new BizServiceException(MybatisConstants.VALUE_IS_BLANK, "实体对象不能为空");
        }
    }

    /**
     * 检查待保存的记录的字段是否符合预期的格式
     *
     * @param record 实体对象
     * @param optionParam 可选参数，默认为 {@code null }
     */
    protected void createValidate(T record, Object optionParam) {
        checkNull4Create(record, optionParam);
        validFieldValue(record, optionParam);
    }

    /**
     * 将 {@code Map} 中的 {@code key} 转换成数据库字段名，会校验数据库字段名，防止SQL注入
     *
     * @param record 待更新的实体对象，key：是数据库列名，value：是数据库列的值
     * @return 数据库字段列表
     */
    protected Map<String, Object> convertToUpdateUseMap(Map<String, Object> record) {
        return CrudServiceUtils.convertToUpdateUseMap(fieldColumns, record, this.getClass());
    }

    /**
     * 将实体类排序字段转换成数据库字段排序，会校验数据库字段名，防止SQL注入
     *
     * @param sort 实体类排序字段
     * @return 数据库字段排序
     */
    protected List<Order> convertToOrders(Sort sort) {
        return CrudServiceUtils.convertToOrders(fieldColumns, sort, this.getClass());
    }

    /**
     * 查询存在的记录，用于"检查待保存的实体对象是否已经有存在相同的记录（幂等校验）"
     *
     * @param record 待保存的实体对象
     * @return 存在的记录
     * @see #checkRecordIfExist4Create(Object)
     * @see #checkRecordIfExist4Update( Object)
     */
    protected List<T> findExistRecord4CheckRecord(T record) {
        return null;
    }

    /**
     * 检查待保存的实体对象是否已经有存在相同的记录（幂等校验）
     *
     * @param record 实体对象
     * @see #findExistRecord4CheckRecord( Object)
     */
    protected void checkRecordIfExist4Create(T record) {
        List<T> exists = findExistRecord4CheckRecord(record);
        if (CollectionUtils.isEmpty(exists)) {
            return;
        }
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
            throw new BizServiceException(MybatisConstants.MORE_RECORDS, errorMsg.toString());
        } else {
            throw new BizServiceException(MybatisConstants.MORE_RECORDS, "已经存在相同的记录");
        }
    }

    /**
     * 检查待更新的实体对象是否已经有存在相同的记录（幂等校验）。如果实体的类型是 {@link LogicDeleteEntity}
     * ，如果数据是逻辑删除，则校验通过。
     *
     * @param record 实体对象
     * @see #findExistRecord4CheckRecord( Object)
     */
    protected void checkRecordIfExist4Update(T record) {
        if (record instanceof LogicDeleteEntity
                && StringUtils.equals(((LogicDeleteEntity) record).getIsDeleted(), MyossConstants.Y)) {
            return;
        }
        List<T> exists = findExistRecord4CheckRecord(record);
        if (CollectionUtils.isEmpty(exists)) {
            return;
        }
        if (record instanceof PrimaryKeyEntity) {
            // 使用主键实体类的字段比较
            Serializable id = ((PrimaryKeyEntity) record).getPrimaryKey();
            for (T item : exists) {
                Serializable id1 = ((PrimaryKeyEntity) item).getPrimaryKey();
                if (!id1.equals(id)) {
                    throw new BizServiceException(MybatisConstants.MORE_RECORDS, "已经存在相同的记录，主键id=" + id1);
                }
            }
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
                    throw new BizServiceException(MybatisConstants.MORE_RECORDS, errorMsg.append("]").toString());
                }
            }
        } else {
            // 不支持，需要子类重写
            throw new UnsupportedOperationException();
        }
    }

    /**
     * 检查待更新的实体对象是否已经有存在相同的记录（幂等校验）。默认未实现逻辑，请子类中重写。
     *
     * @param record 实体对象
     * @see #findExistRecord4CheckRecord( Object)
     */
    protected void checkRecordIfExist4Update(Map<String, Object> record) {
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
    public <I> I create(T record, Object optionParam) {
        createValidate(record, optionParam);
        return createCallable(record, optionParam, () -> createInner(record, optionParam));
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public <I> I create(T record) {
        return create(record, null);
    }

    /**
     * 用于重写创建的方法，比如加锁创建
     *
     * @param record 待保存的实体对象
     * @param optionParam 可选参数，默认为 {@code null }
     * @param createCallFunc 保存方法回调类，参考：{@link #create( Object, Object)}
     * @param <I> 主键类型
     * @return 返回执行结果，默认返回的是 {@code 主键id } 参数，可以被子类覆盖重写
     */
    protected <I> I createCallable(T record, Object optionParam, CallableFunc<I> createCallFunc) {
        return createCallFunc.call();
    }

    /**
     * 创建新的记录，{@link #create(Object)} 方法的最后一步调用
     *
     * @param record 待保存的实体对象
     * @param <I> 主键类型
     * @param optionParam 可选参数，默认为 {@code null }
     * @return 返回执行结果，默认返回的是 {@code 主键id}，可以被子类覆盖重写
     */
    protected <I> I createInner(T record, Object optionParam) {
        checkRecordIfExist4Create(record);
        setValue4Create(record, optionParam);
        boolean flag = checkDBResult(crudMapper.insert(record));
        if (flag) {
            return getPrimaryKeyValue(record);
        } else {
            throw new BizServiceException(MybatisConstants.INSERT_DB_FAILED, "插入失败，请检查");
        }
    }

    @SuppressWarnings("unchecked")
    protected <I> I getPrimaryKeyValue(T record) {
        Set<TableColumnInfo> primaryKeyColumns = tableInfo.getPrimaryKeyColumns();
        int size = primaryKeyColumns.size();
        if (size == 0) {
            // 忽略没有主键字段的情况
            return null;
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
        return (I) value;
    }

    protected void setPrimaryKeyValue(T record, Object... value) {
        Set<TableColumnInfo> primaryKeyColumns = tableInfo.getPrimaryKeyColumns();
        int size = primaryKeyColumns.size();
        if (size == 0) {
            // 忽略没有主键字段的情况
            return;
        }
        int idx = 0;
        for (TableColumnInfo columnInfo : primaryKeyColumns) {
            Method writeMethod = columnInfo.getPropertyDescriptor().getWriteMethod();
            try {
                writeMethod.invoke(record, value[idx++]);
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw new BizServiceException(MybatisConstants.UPDATE_ENTITY_FIELD_FAILED,
                        "更新Entity主键失败，请检查。[" + record + ", " + Arrays.toString(value) + "]");
            }
        }
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void createBatch(List<T> records, Object optionParam) {
        if (CollectionUtils.isEmpty(records)) {
            return;
        }
        createBatchCallable(records, optionParam, () -> {
            createBatchInner(records, optionParam);
            return true;
        });
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void createBatch(List<T> records) {
        createBatch(records, null);
    }

    /**
     * 用于重写创建的方法，比如加锁创建
     *
     * @param records 待保存的实体对象
     * @param optionParam 可选参数，默认为 {@code null }
     * @param createCallFunc 保存方法回调类，参考： {@link #createBatch( List, Object)}
     */
    protected void createBatchCallable(List<T> records, Object optionParam, CallableFunc<Boolean> createCallFunc) {
        createCallFunc.call();
    }

    /**
     * 创建新的记录，{@link #createBatch(List)} 方法的最后一步调用
     *
     * @param records 待保存的实体对象
     * @param optionParam 可选参数，默认为 {@code null }
     */
    protected void createBatchInner(List<T> records, Object optionParam) {
        for (T record : records) {
            createValidate(record, optionParam);
            checkRecordIfExist4Create(record);
        }
        // 先校验完数据格式，再设置字段的值
        for (T record : records) {
            setValue4Create(record, optionParam);
            boolean flag = checkDBResult(crudMapper.insert(record));
            if (!flag) {
                throw new BizServiceException(MybatisConstants.INSERT_DB_FAILED, "插入失败，请检查。[" + record + "]");
            }
        }
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public <I> I save(T record) {
        return save(record, null);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public <I> I save(T record, Object optionParam) {
        createValidate(record, optionParam);
        return createCallable(record, optionParam, () -> saveInner(record, optionParam));
    }

    /**
     * 保存新的记录，{@link #create(Object)} 方法的最后一步调用
     *
     * @param record 待保存的实体对象
     * @param <I> 主键类型
     * @param optionParam 可选参数，默认为 {@code null }
     * @return 返回执行结果，默认返回的是 {@code 主键id } 参数，可以被子类覆盖重写
     */
    protected <I> I saveInner(T record, Object optionParam) {
        List<T> exists = findExistRecord4CheckRecord(record);
        if (CollectionUtils.isEmpty(exists)) {
            setValue4Create(record, optionParam);
            boolean flag = checkDBResult(crudMapper.insert(record));
            if (flag) {
                return getPrimaryKeyValue(record);
            } else {
                throw new BizServiceException(MybatisConstants.INSERT_DB_FAILED, "插入失败，请检查");
            }
        }
        return getPrimaryKeyValue(exists.get(0));
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public <I> I saveOrUpdate(T record) {
        return saveOrUpdate(record, null);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public <I> I saveOrUpdate(T record, Object optionParam) {
        I primaryKeyValue = getPrimaryKeyValue(record);
        if (primaryKeyValue == null) {
            List<T> exists = findExistRecord4CheckRecord(record);
            if (CollectionUtils.isEmpty(exists)) {
                return create(record, optionParam);
            }
            T exist = exists.get(0);
            if (exist != null) {
                primaryKeyValue = getPrimaryKeyValue(exist);
                setPrimaryKeyValue(record, primaryKeyValue);
                updateByPrimaryKey(record, optionParam);
                return primaryKeyValue;
            } else {
                return create(record, optionParam);
            }
        } else {
            updateByPrimaryKey(record, optionParam);
            return getPrimaryKeyValue(record);
        }
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void updateByPrimaryKey(T record, Object optionParam) {
        checkPrimaryKeyIsNull(SqlCommandType.UPDATE, record);
        validFieldValue(record, optionParam);
        updateByPrimaryKeyCallable(record, optionParam, () -> {
            updateByPrimaryKeyInner(record, optionParam);
            return true;
        });
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void updateByPrimaryKey(T record) {
        updateByPrimaryKey(record, null);
    }

    /**
     * 用于重写更新的方法，比如加锁更新
     *
     * @param record 待更新的实体对象
     * @param optionParam 可选参数，默认为 {@code null }
     * @param updateCallFunc 更新方法回调类，参考：
     *            {@link #updateByPrimaryKey( Object, Object)}
     */
    protected void updateByPrimaryKeyCallable(T record, Object optionParam, CallableFunc<Boolean> updateCallFunc) {
        updateCallFunc.call();
    }

    /**
     * 更新记录，{@link #updateByPrimaryKey(Object)} 方法的最后一步调用
     *
     * @param record 待更新的实体对象
     * @param optionParam 可选参数，默认为 {@code null }
     */
    protected void updateByPrimaryKeyInner(T record, Object optionParam) {
        checkRecordIfExist4Update(record);
        setValue4Update(record, optionParam);
        boolean flag = checkDBResult(crudMapper.updateByPrimaryKey(record));
        if (!flag) {
            throw new BizServiceException(MybatisConstants.NOT_MATCH_RECORDS, "更新失败，未匹配到相应的记录");
        }
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void updateByCondition(T record, T condition, Object optionParam) {
        validFieldValue(record, optionParam);
        checkCommonQueryConditionIsAllNull(SqlCommandType.UPDATE, condition, null);
        updateByConditionCallable(record, condition, optionParam, () -> {
            updateByConditionInner(record, condition, optionParam);
            return true;
        });
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void updateByCondition(T record, T condition) {
        updateByCondition(record, condition, null);
    }

    /**
     * 用于重写更新的方法，比如加锁更新
     *
     * @param record 待更新的实体对象
     * @param condition 匹配的条件
     * @param optionParam 可选参数，默认为 {@code null }
     * @param updateCallFunc 更新方法回调类，参考：
     *            {@link #updateByCondition( Object, Object, Object)}
     */
    protected void updateByConditionCallable(T record, T condition, Object optionParam,
                                             CallableFunc<Boolean> updateCallFunc) {
        updateCallFunc.call();
    }

    /**
     * 更新记录，{@link #updateByCondition(Object, Object)} 方法的最后一步调用
     *
     * @param record 待更新的实体对象
     * @param condition 匹配的条件
     * @param optionParam 可选参数，默认为 {@code null }
     */
    protected void updateByConditionInner(T record, T condition, Object optionParam) {
        checkRecordIfExist4Update(record);
        setValue4Update(record, optionParam);
        boolean flag = checkDBResult(crudMapper.updateByCondition(record, condition));
        if (!flag) {
            throw new BizServiceException(MybatisConstants.NOT_MATCH_RECORDS, "更新失败，未匹配到相应的记录");
        }
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void updateUseMapByCondition(Map<String, Object> record, T condition, Object optionParam) {
        validFieldValue(record, optionParam);
        checkCommonQueryConditionIsAllNull(SqlCommandType.UPDATE, condition, null);
        updateUseMapByConditionCallable(record, condition, optionParam, () -> {
            updateUseMapByConditionInner(record, condition, optionParam);
            return true;
        });
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void updateUseMapByCondition(Map<String, Object> record, T condition) {
        updateUseMapByCondition(record, condition, null);
    }

    /**
     * 用于重写更新的方法，比如加锁更新
     *
     * @param record 待更新的实体对象，key：是数据库列名，value：是数据库列的值
     * @param condition 匹配的条件
     * @param optionParam 可选参数，默认为 {@code null }
     * @param updateCallFunc 更新方法回调类，参考：
     *            {@link #updateUseMapByCondition( Map, Object, Object)}
     */
    protected void updateUseMapByConditionCallable(Map<String, Object> record, T condition, Object optionParam,
                                                   CallableFunc<Boolean> updateCallFunc) {
        updateCallFunc.call();
    }

    /**
     * 更新记录，{@link #updateByCondition(Object, Object)} 方法的最后一步调用
     *
     * @param record 待更新的实体对象，key：是数据库列名，value：是数据库列的值
     * @param condition 匹配的条件
     * @param optionParam 可选参数，默认为 {@code null }
     */
    protected void updateUseMapByConditionInner(Map<String, Object> record, T condition, Object optionParam) {
        checkRecordIfExist4Update(record);
        setValue4Update(record, optionParam);
        Map<String, Object> updateMap = convertToUpdateUseMap(record);
        boolean flag = checkDBResult(crudMapper.updateUseMapByCondition(updateMap, condition));
        if (!flag) {
            throw new BizServiceException(MybatisConstants.NOT_MATCH_RECORDS, "更新失败，未匹配到相应的记录");
        }
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void deleteByPrimaryKey(T condition, Object optionParam) {
        checkPrimaryKeyIsNull(SqlCommandType.DELETE, condition);
        boolean flag = checkDBResult(crudMapper.deleteWithPrimaryKey(condition));
        if (!flag) {
            throw new BizServiceException(MybatisConstants.NOT_MATCH_RECORDS, "更新失败，未匹配到相应的记录");
        }
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void deleteByPrimaryKey(T condition) {
        deleteByPrimaryKey(condition, null);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void deleteByCondition(T condition, Object optionParam) {
        checkCommonQueryConditionIsAllNull(SqlCommandType.DELETE, condition, null);
        boolean flag = checkDBResult(crudMapper.deleteByCondition(condition));
        if (!flag) {
            throw new BizServiceException(MybatisConstants.NOT_MATCH_RECORDS, "更新失败，未匹配到相应的记录");
        }
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void deleteByCondition(T condition) {
        deleteByCondition(condition, null);
    }

    @Override
    public T findByPrimaryKey(Serializable id) {
        checkPrimaryKeyIsNull(SqlCommandType.SELECT, id);
        return crudMapper.selectByPrimaryKey(id);
    }

    @Override
    public T findByPrimaryKey(T condition) {
        checkPrimaryKeyIsNull(SqlCommandType.SELECT, condition);
        return crudMapper.selectWithPrimaryKey(condition);
    }

    @Override
    public T findOne(T condition) {
        checkCommonQueryConditionIsAllNull(SqlCommandType.SELECT, condition, null);
        return crudMapper.selectOne(condition);
    }

    @Override
    public List<T> findList(T condition) {
        checkCommonQueryConditionIsAllNull(SqlCommandType.SELECT, condition, null);
        return crudMapper.selectList(condition);
    }

    @Override
    public List<T> findListWithSort(Page<T> condition) {
        T param = condition.getParam();
        Map<String, Object> extraInfo = condition.getExtraInfo();
        checkCommonQueryConditionIsAllNull(SqlCommandType.SELECT, param, extraInfo);
        Sort sort = condition.getSort();
        List<Order> orders = convertToOrders(sort);
        return crudMapper.selectListWithSort2(param, extraInfo, orders);
    }

    @Override
    public Integer findCount(T condition) {
        checkCommonQueryConditionIsAllNull(SqlCommandType.SELECT, condition, null);
        return crudMapper.selectCount(condition);
    }

    @Override
    public Integer findCount(T condition, Map<String, Object> extraCondition) {
        checkCommonQueryConditionIsAllNull(SqlCommandType.SELECT, condition, extraCondition);
        return crudMapper.selectCount2(condition, extraCondition);
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
        pageQuery(result, param, extraInfo, pageStart, pageSize, orders);
        result.setPageNum(dbPageNum + 1).setPageSize(pageSize);
        // 设置额外字段
        addPageExtraInfo(condition, result);
        return result;
    }

    protected void pageQuery(Page<T> result, T param, Map<String, Object> extraInfo, int pageStart, int pageSize,
                             List<Order> orders) {
        List<T> details = crudMapper.selectPage2(param, extraInfo, pageStart, pageSize, orders);
        int totalCount = crudMapper.selectCount2(param, extraInfo);
        result.setValue(details).setTotalCount(totalCount).setPageSize(pageSize);
    }

    @Override
    public <DTO> Page<DTO> findPageByHelper(Page<DTO> condition) {
        List<Order> orders = convertToOrders(condition.getSort());
        String orderBy = null;
        if (!CollectionUtils.isEmpty(orders)) {
            StringJoiner stringJoiner = new StringJoiner(", ");
            for (Order order : orders) {
                stringJoiner.add(order.getProperty() + " " + order.getDirection().name());
            }
            orderBy = stringJoiner.toString();
        }
        int pageNum = condition.getPageNum();
        int pageSize = condition.getPageSize();
        com.github.pagehelper.Page<DTO> page = com.github.pagehelper.PageHelper.startPage(pageNum, pageSize, orderBy)
                .doSelectPage(() -> pageHelperQuery(condition.getParam(), condition));
        Page<DTO> pageResult = new Page<>();
        pageResult.setTotalCount(Math.toIntExact(page.getTotal()))
                .setPageNum(pageNum)
                .setPageSize(pageSize)
                .setValue(page.getResult());
        return pageResult;
    }

    protected <DTO> List<DTO> pageHelperQuery(Object param, Page<DTO> condition) {
        return null;
    }
}
