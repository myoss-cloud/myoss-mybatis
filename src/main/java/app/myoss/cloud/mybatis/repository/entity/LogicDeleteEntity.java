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

import com.alibaba.fastjson.annotation.JSONField;

import app.myoss.cloud.core.constants.MyossConstants;
import app.myoss.cloud.mybatis.table.annotation.Column;
import app.myoss.cloud.mybatis.table.annotation.FillRule;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 逻辑删除实体基类
 *
 * @author Jerry.Chen
 * @since 2018年5月9日 下午2:20:26
 */
@Accessors(chain = true)
@Data
public class LogicDeleteEntity implements BaseEntity {
    private static final long  serialVersionUID          = 8520422267799966859L;
    /**
     * LogicDeleteEntity label
     */
    public static final String LABEL_LOGIC_DELETE_ENTITY = "LogicDeleteEntity";

    /**
     * Database Column Name: is_deleted
     * <p>
     * Database Column Remarks: 是否删除
     * </p>
     */
    @JSONField(label = LABEL_LOGIC_DELETE_ENTITY)
    @Column(name = "is_deleted", nullable = false, fillRule = {
            FillRule.INSERT }, logicDelete = true, logicDeleteValue = MyossConstants.Y, logicUnDeleteValue = MyossConstants.N)
    private String             isDeleted;
}
