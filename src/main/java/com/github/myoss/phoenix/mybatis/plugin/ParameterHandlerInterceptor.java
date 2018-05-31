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

package com.github.myoss.phoenix.mybatis.plugin;

import java.sql.PreparedStatement;
import java.util.Properties;

import org.apache.ibatis.executor.parameter.ParameterHandler;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.plugin.Intercepts;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.plugin.Plugin;
import org.apache.ibatis.plugin.Signature;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.SystemMetaObject;
import org.apache.ibatis.scripting.defaults.DefaultParameterHandler;

import lombok.Getter;

/**
 * ParameterHandler 拦截器，通用 SQL SELECT statements 处理 Parameter 逻辑
 *
 * @author Jerry.Chen
 * @since 2018年4月30日 下午12:55:35
 * @see ParameterHandlerCustomizer
 */
@Intercepts({ @Signature(type = ParameterHandler.class, method = "setParameters", args = { PreparedStatement.class }) })
public class ParameterHandlerInterceptor implements Interceptor {
    @Getter
    private ParameterHandlerCustomizer parameterHandlerCustomizer;

    public ParameterHandlerInterceptor(ParameterHandlerCustomizer parameterHandlerCustomizer) {
        this.parameterHandlerCustomizer = parameterHandlerCustomizer;
    }

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        DefaultParameterHandler target = (DefaultParameterHandler) invocation.getTarget();
        Object parameterObject = target.getParameterObject();
        MetaObject metaObject = SystemMetaObject.forObject(target);
        MappedStatement mappedStatement = (MappedStatement) metaObject.getValue("mappedStatement");
        BoundSql boundSql = (BoundSql) metaObject.getValue("boundSql");
        SqlCommandType sqlCommandType = mappedStatement.getSqlCommandType();
        switch (sqlCommandType) {
            case SELECT:
                parameterHandlerCustomizer.handlerSelect(mappedStatement, boundSql, parameterObject);
                break;
            case INSERT:
                parameterHandlerCustomizer.handlerInsert(mappedStatement, boundSql, parameterObject);
                break;
            case UPDATE:
                parameterHandlerCustomizer.handlerUpdate(mappedStatement, boundSql, parameterObject);
                break;
            case DELETE:
                parameterHandlerCustomizer.handlerDelete(mappedStatement, boundSql, parameterObject);
                break;
            case FLUSH:
                parameterHandlerCustomizer.handlerFlush(mappedStatement, boundSql, parameterObject);
                break;
            case UNKNOWN:
            default:
                parameterHandlerCustomizer.handlerUnknown(mappedStatement, boundSql, parameterObject);
                break;
        }
        return invocation.proceed();
    }

    @Override
    public Object plugin(Object target) {
        if (target instanceof ParameterHandler) {
            return Plugin.wrap(target, this);
        }
        return target;
    }

    @Override
    public void setProperties(Properties properties) {
    }
}
