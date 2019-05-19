/*
 * Copyright 2018-2019 https://github.com/myoss
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

package app.myoss.cloud.mybatis.type;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Objects;

import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;

import lombok.AllArgsConstructor;
import lombok.NonNull;

/**
 * 如果枚举class实现了 {@link EnumValue} 接口，用于映射 "数据库中字段的值" 和 "java枚举" 的关系。
 *
 * @param <E> 范型
 * @author Jerry.Chen
 * @since 2019年5月16日 下午3:25:53
 */
@AllArgsConstructor
public class EnumValueTypeHandler<E extends Enum<?> & EnumValue> extends BaseTypeHandler<EnumValue> {
    @NonNull
    private final Class<E> type;

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, EnumValue parameter, JdbcType jdbcType)
            throws SQLException {
        if (jdbcType == null) {
            ps.setObject(i, parameter.getDbValue());
        } else {
            ps.setObject(i, parameter.getDbValue(), jdbcType.TYPE_CODE);
        }
    }

    @Override
    public E getNullableResult(ResultSet rs, String columnName) throws SQLException {
        Object value = rs.getObject(columnName);
        return getEnum(type, value);
    }

    @Override
    public E getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        Object value = rs.getObject(columnIndex);
        return getEnum(type, value);
    }

    @Override
    public E getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        Object value = cs.getObject(columnIndex);
        return getEnum(type, value);
    }

    private static <E extends Enum<?> & EnumValue> E getEnum(Class<E> enumClass, Object value) {
        if (value == null) {
            return null;
        }
        E[] enumConstants = enumClass.getEnumConstants();
        for (E enumConstant : enumConstants) {
            if (Objects.equals(value, enumConstant.getDbValue())) {
                return enumConstant;
            }
        }
        return null;
    }
}
