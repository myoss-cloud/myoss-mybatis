/*
 * Copyright 2018-2020 https://github.com/myoss
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

package app.myoss.cloud.mybatis.repository.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.springframework.util.CollectionUtils;

import app.myoss.cloud.core.lang.bean.BeanUtil;
import app.myoss.cloud.core.lang.dto.Order;
import app.myoss.cloud.core.lang.dto.Sort;
import app.myoss.cloud.mybatis.table.TableColumnInfo;
import app.myoss.cloud.mybatis.table.TableInfo;
import lombok.extern.slf4j.Slf4j;

/**
 * 增、删、改、查服务工具类
 *
 * @author Jerry.Chen
 * @since 2020年9月6日 下午3:33:07
 */
@Slf4j
public class CrudServiceUtils {
    public static boolean checkPrimaryKeyIsNull(TableInfo tableInfo, Object record, boolean checkAll) {
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

    public static Map<String, Object> convertToUpdateUseMap(Map<String, String> fieldColumns,
                                                            Map<String, Object> record, Class<?> clazz) {
        Map<String, Object> updateMap = new HashMap<>(record.size());
        for (Entry<String, Object> entry : record.entrySet()) {
            String key = entry.getKey();
            String columnName = fieldColumns.get(key);
            if (columnName != null) {
                // 校验字段名，防止SQL注入
                updateMap.put(columnName, entry.getValue());
            } else {
                log.error("[{}] ignored invalid filed: {}", clazz, key);
            }
        }
        return updateMap;
    }

    public static List<Order> convertToOrders(Map<String, String> fieldColumns, Sort sort, Class<?> clazz) {
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
                log.error("[{}] ignored invalid filed: {}", clazz, item.getProperty());
            }
        }
        return orders;
    }
}
