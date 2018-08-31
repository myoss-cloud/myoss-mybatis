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

package app.myoss.cloud.mybatis.plugin;

import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;

/**
 * 通用 SQL SELECT statements 处理 Parameter 逻辑
 *
 * @author Jerry.Chen
 * @since 2018年5月1日 上午1:32:51
 * @see ParameterHandlerInterceptor
 */
public interface ParameterHandlerCustomizer {
    /**
     * 通用 SQL SELECT statements 处理 Parameter 逻辑
     *
     * @param mappedStatement Mapped Statement
     * @param boundSql Bound Sql
     * @param parameterObject 参数对象
     */
    default void handlerSelect(MappedStatement mappedStatement, BoundSql boundSql, Object parameterObject) {
        //  default do nothing
    }

    /**
     * 通用 SQL INSERT statements 处理 Parameter 逻辑
     *
     * @param mappedStatement Mapped Statement
     * @param boundSql Bound Sql
     * @param parameterObject 参数对象
     */
    default void handlerInsert(MappedStatement mappedStatement, BoundSql boundSql, Object parameterObject) {
        //  default do nothing
    }

    /**
     * 通用 SQL UPDATE statements 处理 Parameter 逻辑
     *
     * @param mappedStatement Mapped Statement
     * @param boundSql Bound Sql
     * @param parameterObject 参数对象
     */
    default void handlerUpdate(MappedStatement mappedStatement, BoundSql boundSql, Object parameterObject) {
        //  default do nothing
    }

    /**
     * 执行通用 SQL DELETE statements 处理 Parameter 逻辑
     *
     * @param mappedStatement Mapped Statement
     * @param boundSql Bound Sql
     * @param parameterObject 参数对象
     */
    default void handlerDelete(MappedStatement mappedStatement, BoundSql boundSql, Object parameterObject) {
        //  default do nothing
    }

    /**
     * 通用 SQL FLUSH statements 处理 Parameter 逻辑
     *
     * @param mappedStatement Mapped Statement
     * @param boundSql Bound Sql
     * @param parameterObject 参数对象
     */
    default void handlerFlush(MappedStatement mappedStatement, BoundSql boundSql, Object parameterObject) {
        //  default do nothing
    }

    /**
     * 通用 SQL UNKNOWN statements 处理 Parameter 逻辑
     *
     * @param mappedStatement Mapped Statement
     * @param boundSql Bound Sql
     * @param parameterObject 参数对象
     */
    default void handlerUnknown(MappedStatement mappedStatement, BoundSql boundSql, Object parameterObject) {
        //  default do nothing
    }
}
