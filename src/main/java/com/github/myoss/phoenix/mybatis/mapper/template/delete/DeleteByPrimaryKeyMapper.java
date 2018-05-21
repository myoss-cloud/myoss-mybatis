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

package com.github.myoss.phoenix.mybatis.mapper.template.delete;

import java.io.Serializable;

import org.apache.ibatis.annotations.DeleteProvider;

import com.github.myoss.phoenix.mybatis.mapper.annotation.RegisterMapper;
import com.github.myoss.phoenix.mybatis.mapper.template.delete.impl.DeleteMapperTemplate;

/**
 * 删除记录通用 Mapper 接口
 *
 * @author Jerry.Chen 2018年4月29日 下午4:39:55
 */
@RegisterMapper
public interface DeleteByPrimaryKeyMapper<T> {
    /**
     * 根据主键id删除记录；如果数据库表支持"逻辑删除"，则会生成 update 语句。
     *
     * @param id 主键id
     * @return SQL执行成功之后，影响的行数
     * @see DeleteMapperTemplate#deleteByPrimaryKey
     */
    @DeleteProvider(type = DeleteMapperTemplate.class, method = "dynamicSql")
    int deleteByPrimaryKey(Serializable id);

    /**
     * 根据主键id删除记录；如果数据库表支持"逻辑删除"，则会生成 update 语句。
     *
     * @param condition 主键id
     * @return SQL执行成功之后，影响的行数
     * @see DeleteMapperTemplate#deleteWithPrimaryKey
     */
    @DeleteProvider(type = DeleteMapperTemplate.class, method = "dynamicSql")
    int deleteWithPrimaryKey(T condition);
}
