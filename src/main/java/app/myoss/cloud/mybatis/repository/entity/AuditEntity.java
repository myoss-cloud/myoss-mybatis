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

package app.myoss.cloud.mybatis.repository.entity;

import java.util.Date;

import app.myoss.cloud.mybatis.table.annotation.Column;
import app.myoss.cloud.mybatis.table.annotation.FillRule;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * 审计实体基类，包含审计的字段
 *
 * @author Jerry.Chen
 * @since 2018年5月9日 下午2:10:02
 */
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@Data
public class AuditEntity extends LogicDeleteEntity {
    private static final long  serialVersionUID   = -4818037441910790468L;
    /**
     * AuditEntity label
     */
    public static final String LABEL_AUDIT_ENTITY = "AuditEntity";

    /**
     * Database Column Name: creator
     * <p>
     * Database Column Remarks: 创建者
     * </p>
     */
    @com.alibaba.fastjson.annotation.JSONField(label = LABEL_AUDIT_ENTITY)
    @Column(name = "creator", nullable = false, fillRule = { FillRule.INSERT })
    private String             creator;

    /**
     * Database Column Name: modifier
     * <p>
     * Database Column Remarks: 修改者
     * </p>
     */
    @com.alibaba.fastjson.annotation.JSONField(label = LABEL_AUDIT_ENTITY)
    @Column(name = "modifier", nullable = false, fillRule = { FillRule.INSERT, FillRule.UPDATE })
    private String             modifier;

    /**
     * Database Column Name: gmt_created
     * <p>
     * Database Column Remarks: 创建时间
     * </p>
     */
    @com.alibaba.fastjson.annotation.JSONField(label = LABEL_AUDIT_ENTITY)
    @Column(name = "gmt_created", nullable = false, fillRule = { FillRule.INSERT })
    private Date               gmtCreated;

    /**
     * Database Column Name: gmt_modified
     * <p>
     * Database Column Remarks: 修改时间
     * </p>
     */
    @com.alibaba.fastjson.annotation.JSONField(label = LABEL_AUDIT_ENTITY)
    @Column(name = "gmt_modified", nullable = false, fillRule = { FillRule.INSERT, FillRule.UPDATE })
    private Date               gmtModified;
}
