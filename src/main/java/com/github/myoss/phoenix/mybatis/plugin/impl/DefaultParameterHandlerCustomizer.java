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

package com.github.myoss.phoenix.mybatis.plugin.impl;

import java.util.Collection;
import java.util.Date;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;

import com.github.myoss.phoenix.core.constants.PhoenixConstants;
import com.github.myoss.phoenix.mybatis.plugin.ParameterHandlerCustomizer;
import com.github.myoss.phoenix.mybatis.repository.entity.AuditIdEntity;

/**
 * 默认的 Mybatis 参数处理自动配置
 *
 * @author Jerry.Chen 2018年5月20日 下午4:49:45
 */
public class DefaultParameterHandlerCustomizer implements ParameterHandlerCustomizer {
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
                    entity.setIsDeleted(PhoenixConstants.N);
                }
                if (StringUtils.isBlank(entity.getCreator())) {
                    entity.setCreator(PhoenixConstants.SYSTEM);
                }
                if (entity.getGmtCreated() == null) {
                    entity.setGmtCreated(new Date());
                }
            }
            if (StringUtils.isBlank(entity.getModifier())) {
                entity.setModifier(PhoenixConstants.SYSTEM);
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
            Object collection = map.get("collection");
            if (collection instanceof Collection) {
                Collection value = (Collection) collection;
                for (Object entity : value) {
                    setAuditInfo(entity, isInsert);
                }
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
