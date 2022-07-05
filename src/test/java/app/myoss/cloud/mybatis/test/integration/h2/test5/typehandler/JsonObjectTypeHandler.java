/*
 * Copyright 2018-2022 https://github.com/myoss
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

package app.myoss.cloud.mybatis.test.integration.h2.test5.typehandler;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import app.myoss.cloud.core.lang.json.JsonApi;
import app.myoss.cloud.core.lang.json.JsonObject;

/**
 * JsonObject TypeHandler
 *
 * @author Jerry.Chen
 * @since 2022年7月5日 上午10:11:22
 */
public class JsonObjectTypeHandler extends BaseTypeHandler<JsonObject> {
    private final static Logger log = LoggerFactory.getLogger(JsonObjectTypeHandler.class);

    public JsonObjectTypeHandler(Class<JsonObject> type) {
        if (log.isDebugEnabled()) {
            log.debug("JsonObjectTypeHandler(" + type + ")");
        }
        if (type == null) {
            throw new IllegalArgumentException("Type argument cannot be null");
        }
    }

    private JsonObject parse(String json) {
        if (json == null || json.length() == 0) {
            return null;
        }
        return JsonApi.fromJson(json);
    }

    private String toJsonString(Object obj) {
        return JsonApi.toJson(obj);
    }

    @Override
    public JsonObject getNullableResult(ResultSet rs, String columnName) throws SQLException {
        return parse(rs.getString(columnName));
    }

    @Override
    public JsonObject getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        return parse(rs.getString(columnIndex));
    }

    @Override
    public JsonObject getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        return parse(cs.getString(columnIndex));
    }

    @Override
    public void setNonNullParameter(PreparedStatement ps, int columnIndex, JsonObject parameter, JdbcType jdbcType)
            throws SQLException {
        ps.setString(columnIndex, toJsonString(parameter));
    }
}
