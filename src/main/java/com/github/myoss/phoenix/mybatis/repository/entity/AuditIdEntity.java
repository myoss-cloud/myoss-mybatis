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

package com.github.myoss.phoenix.mybatis.repository.entity;

import java.io.Serializable;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 审计实体基类，包含审计的字段，增加了主键id字段
 *
 * @author Jerry.Chen 2018年5月9日 下午2:15:14
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class AuditIdEntity extends AuditEntity implements PrimaryKeyEntity {
    private static final long serialVersionUID = -7391784386524900465L;

    /**
     * Database Column Name: id
     * <p>
     * Database Column Remarks: 主键id
     */
    protected Serializable    id;

    @Override
    public Serializable getPrimaryKey() {
        return id;
    }

    @Override
    public void setPrimaryKey(Serializable primaryKey) {
        id = primaryKey;
    }
}
