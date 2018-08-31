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

package app.myoss.cloud.mybatis.mapper.template.update;

import java.util.Map;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.UpdateProvider;

import app.myoss.cloud.mybatis.mapper.annotation.RegisterMapper;
import app.myoss.cloud.mybatis.mapper.template.update.impl.UpdateMapperTemplate;

/**
 * 更新（update）操作，通用 Mapper 接口
 *
 * @param <T> 实体类
 * @author Jerry.Chen
 * @since 2018年6月11日 上午12:49:23
 */
@RegisterMapper
public interface UpdateUseMapByConditionMapper<T> {
    /**
     * 根据条件更新记录（根据 {@code record} 中的 {@code key} 来更新数据库列的值）
     *
     * @param record 待更新的实体对象，key：是数据库列名，value：是数据库列的值
     * @param condition 匹配的条件
     * @return SQL执行成功之后，影响的行数
     * @see UpdateMapperTemplate#updateUseMapByCondition
     */
    @UpdateProvider(type = UpdateMapperTemplate.class, method = "dynamicSql")
    int updateUseMapByCondition(@Param("record") Map<String, Object> record, @Param("condition") T condition);
}
