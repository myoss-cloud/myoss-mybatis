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

import java.io.Serializable;

/**
 * 用于获取枚举，保存在数据库中字段的值
 *
 * <pre>
 * &#64;Getter
 * &#64;AllArgsConstructor
 * public enum AccountStatusEnum implements EnumValue {
 *     NORMAL(&quot;N&quot;, &quot;正常&quot;),
 *     LOCKED(&quot;L&quot;, &quot;锁定&quot;);
 *
 *     private String code;
 *     private String name;
 *
 *     &#64;Override
 *     public Serializable getDbValue() {
 *         return this.code;
 *     }
 * }
 * </pre>
 *
 * @param <T> 范型
 * @author Jerry.Chen
 * @since 2019年5月16日 下午3:37:45
 */
public interface EnumValue<T extends Serializable> {
    /**
     * 获取当前枚举，保存在数据库中字段的值
     *
     * @return 保存在数据库中字段的值
     */
    T getDbValue();
}
