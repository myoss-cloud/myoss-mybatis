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

import com.alibaba.fastjson.annotation.JSONField;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.github.myoss.phoenix.mybatis.table.annotation.Column;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * 审计实体基类，包含审计的字段，增加了主键id字段
 *
 * @param <I> "主键id"的类型
 * @author Jerry.Chen
 * @since 2018年5月9日 下午2:15:14
 */
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@Data
public class AuditIdEntity<I extends Serializable> extends AuditEntity implements PrimaryKeyEntity<I> {
    private static final long  serialVersionUID      = -7391784386524900465L;
    /**
     * AuditIdEntity label
     */
    public static final String LABEL_AUDIT_ID_ENTITY = "AuditIdEntity";

    /**
     * Database Column Name: id
     * <p>
     * Database Column Remarks: 主键id
     */
    @Column(primaryKey = true)
    private I                  id;

    /**
     * 获取"主键id"的值
     *
     * @return "主键id"的值
     */
    @JsonIgnore
    @JSONField(serialize = false, deserialize = false)
    @Override
    public I getPrimaryKey() {
        return id;
    }

    /**
     * 设置"主键id"的值
     *
     * @param primaryKey "主键id"的值
     */
    @Override
    public void setPrimaryKey(I primaryKey) {
        this.id = primaryKey;
    }

    /**
     * 获取"主键id"的值
     *
     * @return "主键id"的值
     */
    @JSONField(label = LABEL_AUDIT_ID_ENTITY)
    public I getId() {
        return id;
    }

    /**
     * 设置"主键id"的值
     *
     * @param id "主键id"的值
     * @return 当前实例对象
     */
    public AuditIdEntity<I> setId(I id) {
        this.id = id;
        return this;
    }
}
