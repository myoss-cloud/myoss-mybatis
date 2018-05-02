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

package com.github.myoss.phoenix.mybatis.mapper.template;

import com.github.myoss.phoenix.mybatis.mapper.annotation.RegisterMapper;
import com.github.myoss.phoenix.mybatis.mapper.template.delete.DeleteMapper;
import com.github.myoss.phoenix.mybatis.mapper.template.insert.CreateMapper;
import com.github.myoss.phoenix.mybatis.mapper.template.select.RetrieveMapper;
import com.github.myoss.phoenix.mybatis.mapper.template.update.UpdateMapper;

/**
 * 创建（Create）、更新（Update）、读取（Retrieve）和删除（Delete）操作，通用 Mapper 接口
 *
 * @author Jerry.Chen 2018年4月29日 下午4:58:48
 */
@RegisterMapper
public interface CrudMapper<T> extends CreateMapper<T>, RetrieveMapper<T>, UpdateMapper<T>, DeleteMapper<T> {
}
