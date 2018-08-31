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

package app.myoss.cloud.mybatis.table.annotation;

import java.util.Objects;

import org.apache.commons.lang3.StringUtils;

import app.myoss.cloud.mybatis.plugin.ParameterHandlerCustomizer;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 用于 SQL 语句在 INSERT/UPDATE 的时候，字段是否需要进行填充，因为生成 Mybatis SQL 语句的时候默认会使用 &lt;if
 * test=&quot;id != null&quot;&gt; 这种表达式判断，需要提前标记字段是否不需要 if 表达式进行包装，需要配合
 * {@link ParameterHandlerCustomizer} 进行设置值。
 *
 * @author Jerry.Chen
 * @since 2018年5月11日 下午5:21:26
 */
@Getter
@AllArgsConstructor
public enum FillRule {
    /**
     * INSERT/UPDATE 的时候不填充
     */
    NONE("NONE", "\"INSERT/UPDATE\"的时候不填充"),
    /**
     * INSERT 的时候填充
     */
    INSERT("INSERT", "\"INSERT\"的时候填充"),
    /**
     * UPDATE 的时候填充
     */
    UPDATE("UPDATE", "\"UPDATE\"的时候填充");

    /**
     * 填充类型
     */
    String value;
    /**
     * 填充描述
     */
    String desc;

    /**
     * 获取对应的填充枚举
     *
     * @param value 填充类型
     * @return 填充枚举
     */
    public static FillRule getEnumByValue(String value) {
        if (StringUtils.isBlank(value)) {
            return null;
        }
        for (FillRule fillRule : FillRule.values()) {
            if (Objects.equals(fillRule.getValue(), value)) {
                return fillRule;
            }
        }
        return null;
    }
}
