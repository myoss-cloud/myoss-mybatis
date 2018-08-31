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

package app.myoss.cloud.mybatis.plugin.impl;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.session.defaults.DefaultSqlSession;

import app.myoss.cloud.core.constants.MyossConstants;
import app.myoss.cloud.mybatis.mapper.template.insert.InsertBatchMapper;
import app.myoss.cloud.mybatis.mapper.template.update.UpdateByConditionMapper;
import app.myoss.cloud.mybatis.plugin.ParameterHandlerCustomizer;
import app.myoss.cloud.mybatis.repository.entity.AuditIdEntity;

/**
 * 默认的 Mybatis 参数处理自动配置
 *
 * @author Jerry.Chen
 * @since 2018年5月20日 下午4:49:45
 */
public class DefaultParameterHandlerCustomizer implements ParameterHandlerCustomizer {
    /**
     * 执行 {@link InsertBatchMapper#insertBatch(List)} 的时候，在
     * {@link DefaultSqlSession#update(java.lang.String, java.lang.Object)}
     * 对参数使用了 {@link Map} 进行了包装
     */
    public static final String COLLECTION = "collection";
    /**
     * 执行 {@link UpdateByConditionMapper#updateByCondition(Object, Object)}
     * 的时候，处理 {@code @Param("record") T record} 参数
     */
    public static final String RECORD     = "record";

    /**
     * 设置审计字段信息
     *
     * @param o 参数对象
     * @param isInsert 是否为插入
     */
    public static void setAuditInfo(Object o, boolean isInsert) {
        if (o instanceof AuditIdEntity) {
            AuditIdEntity entity = (AuditIdEntity) o;
            if (isInsert) {
                if (StringUtils.isBlank(entity.getIsDeleted())) {
                    entity.setIsDeleted(MyossConstants.N);
                }
                if (StringUtils.isBlank(entity.getCreator())) {
                    entity.setCreator(MyossConstants.SYSTEM);
                }
                if (entity.getGmtCreated() == null) {
                    entity.setGmtCreated(new Date());
                }
            }
            if (StringUtils.isBlank(entity.getModifier())) {
                entity.setModifier(MyossConstants.SYSTEM);
            }
            entity.setGmtModified(new Date());
        }
    }

    /**
     * 设置通用字段信息
     *
     * @param parameterObject 参数对象
     * @param isInsert 是否为插入
     */
    public static void setCommonParameter(Object parameterObject, boolean isInsert) {
        if (parameterObject instanceof AuditIdEntity) {
            AuditIdEntity entity = (AuditIdEntity) parameterObject;
            setAuditInfo(entity, isInsert);
        } else if (parameterObject instanceof Map) {
            Map map = (Map) parameterObject;
            if (map.containsKey(COLLECTION)) {
                Object collection = map.get(COLLECTION);
                if (collection instanceof Collection) {
                    Collection value = (Collection) collection;
                    for (Object entity : value) {
                        setAuditInfo(entity, isInsert);
                    }
                }
            } else if (map.containsKey(RECORD)) {
                Object record = map.get(RECORD);
                setAuditInfo(record, isInsert);
            }
        }
    }

    @Override
    public void handlerInsert(MappedStatement mappedStatement, BoundSql boundSql, Object parameterObject) {
        setCommonParameter(parameterObject, true);
    }

    @Override
    public void handlerUpdate(MappedStatement mappedStatement, BoundSql boundSql, Object parameterObject) {
        setCommonParameter(parameterObject, false);
    }
}
