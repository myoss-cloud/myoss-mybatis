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

package com.github.myoss.phoenix.mybatis.repository.service.impl;

import com.github.myoss.phoenix.core.cache.lock.LockService;
import com.github.myoss.phoenix.mybatis.mapper.template.CrudMapper;

/**
 * 实现数据库表增、删、改、查常用操作的基类，使用了缓存锁进行创建、删除、更新
 *
 * @author Jerry.Chen
 * @since 2018年5月10日 上午12:20:12
 */
public class LockCrudServiceImpl<M extends CrudMapper<T>, T> extends BaseCrudServiceImpl<M, T> {
    protected LockService lockService;

    /**
     * 设置缓存锁服务接口
     *
     * @param lockService 缓存锁服务接口
     */
    public void setLockService(LockService lockService) {
        this.lockService = lockService;
    }
}
