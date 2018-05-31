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

package com.github.myoss.phoenix.mybatis.mapper.template.insert;

import com.github.myoss.phoenix.mybatis.mapper.annotation.RegisterMapper;

/**
 * 创建（Create）操作，通用 Mapper 接口
 *
 * @author Jerry.Chen
 * @since 2018年4月29日 下午5:10:59
 */
@RegisterMapper
public interface CreateMapper<T> extends InsertMapper<T>, InsertAllColumnMapper<T>, InsertBatchMapper<T> {
}
