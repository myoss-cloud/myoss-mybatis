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

package app.myoss.cloud.mybatis.test.integration.h2.test2.constants;

import java.io.Serializable;

import app.myoss.cloud.mybatis.type.EnumValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 账户状态枚举
 *
 * @author jerry
 * @since 2019年5月16日 下午10:19:56
 * @see app.myoss.cloud.mybatis.test.integration.h2.test2.entity.UserHistory#status
 */
@Getter
@AllArgsConstructor
public enum AccountStatusEnum implements EnumValue {
    /**
     * 正常
     */
    NORMAL("N", "正常"),
    /**
     * 已锁定
     */
    LOCKED("L", "锁定");

    private String code;
    private String name;

    @Override
    public Serializable getDbValue() {
        return this.code;
    }
}
