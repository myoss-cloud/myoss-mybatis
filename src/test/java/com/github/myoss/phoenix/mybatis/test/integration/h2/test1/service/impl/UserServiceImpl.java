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

package com.github.myoss.phoenix.mybatis.test.integration.h2.test1.service.impl;

import org.springframework.stereotype.Service;

import com.github.myoss.phoenix.mybatis.repository.service.impl.BaseCrudServiceImpl;
import com.github.myoss.phoenix.mybatis.test.integration.h2.test1.entity.User;
import com.github.myoss.phoenix.mybatis.test.integration.h2.test1.mapper.UserMapper;
import com.github.myoss.phoenix.mybatis.test.integration.h2.test1.service.UserService;

/**
 * This service implement access the database table t_sys_user
 * <p>
 * Database Table Remarks: 系统用户信息表
 * </p>
 *
 * @author jerry
 * @since 2018年5月11日 上午10:41:47
 */
@Service
public class UserServiceImpl extends BaseCrudServiceImpl<UserMapper, User> implements UserService {
}
