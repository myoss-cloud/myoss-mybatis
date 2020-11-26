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

import java.lang.reflect.Field;
import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;

import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

/**
 * 如果枚举字段使用了 {@link EnumValueMappedType} 注解，用于映射 "数据库中字段的值" 和 "java枚举" 的关系。
 *
 * @param <E> 范型
 * @author Jerry.Chen
 * @since 2019年5月16日 下午3:25:53
 */
@Slf4j
@AllArgsConstructor
public class EnumValueAnnotationTypeHandler<E extends Enum<E>> extends BaseTypeHandler<E> {
    private static final Map<Class<?>, Field> ENUM_FIELDS = new ConcurrentHashMap<>();
    @NonNull
    private final Class<E>                    type;

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, E parameter, JdbcType jdbcType) throws SQLException {
        Field field = ENUM_FIELDS.get(type);
        try {
            Object value = field.get(parameter);
            if (jdbcType == null) {
                ps.setObject(i, value);
            } else {
                ps.setObject(i, value, jdbcType.TYPE_CODE);
            }
        } catch (IllegalAccessException ex) {
            throw new IllegalArgumentException(
                    "Cannot convert " + i + " to " + type.getSimpleName() + " by ordinal value.", ex);
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

    /**
     * 注册枚举字段
     *
     * @param clazz 枚举 class
     * @param field 字段上有 {@link EnumValueMappedType} 注解
     */
    public static void registryEnumField(Class<?> clazz, Field field) {
        field.setAccessible(true);
        Field absent = ENUM_FIELDS.putIfAbsent(clazz, field);
        if (absent == null) {
            return;
        }
        boolean flag = absent.getDeclaringClass() == field.getDeclaringClass()
                && Objects.equals(absent.getName(), field.getName());
        if (!flag) {
            throw new IllegalArgumentException(
                    clazz + " contains multiple annotations '" + EnumValueMappedType.class.getCanonicalName()
                            + "', please check field: " + absent.getName() + ", " + field.getName());
        }
    }

    private static <E extends Enum<E>> E getEnum(Class<E> enumClass, Object value) {
        if (value == null) {
            return null;
        }
        Field field = ENUM_FIELDS.get(enumClass);
        E[] enumConstants = enumClass.getEnumConstants();
        for (E enumConstant : enumConstants) {
            try {
                if (Objects.equals(field.get(enumConstant), value)) {
                    return enumConstant;
                }
            } catch (IllegalAccessException ex) {
                throw new IllegalArgumentException("Cannot invoke a method [" + field.getName() + "] for the " + value,
                        ex);
            }
        }
        return null;
    }
}
