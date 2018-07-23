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

/**
 * 主键实体基类
 *
 * @param <I> "主键id"的类型
 * @author Jerry.Chen
 * @since 2018年5月9日 下午6:23:17
 */
public interface PrimaryKeyEntity<I extends Serializable> {
    /**
     * 获取主键的值
     *
     * @return 主键的值
     */
    I getPrimaryKey();

    /**
     * 设置主键的值
     *
     * @param primaryKey 主键的值
     */
    void setPrimaryKey(I primaryKey);
}
