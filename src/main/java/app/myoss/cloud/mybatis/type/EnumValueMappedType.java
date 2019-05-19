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

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * 用于获取枚举，保存在数据库中字段的值。特别注意：同一个枚举中，此注解只能使用一次，会进行唯一性校验。
 *
 * <pre>
 * &#64;Getter
 * &#64;AllArgsConstructor
 * public enum AccountStatusEnum {
 *     NORMAL(&quot;N&quot;, &quot;正常&quot;),
 *     LOCKED(&quot;L&quot;, &quot;锁定&quot;);
 *
 *     &#64;EnumValueMappedType
 *     private String code;
 *     private String name;
 * }
 * </pre>
 *
 * @author Jerry.Chen
 * @since 2019年5月19日 上午10:44:43
 */
@Target(FIELD)
@Retention(RUNTIME)
@Documented
public @interface EnumValueMappedType {
}
