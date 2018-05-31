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

package com.github.myoss.phoenix.mybatis.type;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.TypeHandler;

import com.github.myoss.phoenix.mybatis.table.annotation.Column;

/**
 * nothing to do, just for {@link Column#typeHandler()} set default value
 *
 * @author Jerry.Chen
 * @since 2018年4月29日 下午4:01:14
 */
public class UnsupportedTypeHandler implements TypeHandler<Object> {
    @Override
    public void setParameter(PreparedStatement ps, int i, Object parameter, JdbcType jdbcType) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Object getResult(ResultSet rs, String columnName) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Object getResult(ResultSet rs, int columnIndex) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Object getResult(CallableStatement cs, int columnIndex) {
        throw new UnsupportedOperationException();
    }
}
